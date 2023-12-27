package com.newcoder.community.util;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    // 将一个敏感词添加到前缀词中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i ++) {
            char c = keyword.charAt(i);
            if (tempNode.getSubNode(c) == null) {
                tempNode.addSubNode(c, new TrieNode());
            }
            tempNode = tempNode.getSubNode(c);
        }
        tempNode.setKeywordEnd(true);
    }

    /**
     * 过滤敏感词
     *
     * @param 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        TrieNode tempNode = rootNode;
        int begin = 0, position = 0;
        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若树指针处于根节点，将此符号计入结果
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin ++;
                }
                position ++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                position = ++ begin;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd) {
                // 以begin开头，position结尾的字符串是敏感词
                sb.append(REPLACEMENT);
                begin = ++ position;
                tempNode = rootNode;
            } else {
                // 以begin开头，position结尾的字符串可能是敏感词
                position++;
            }
        }
        // 将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80 - 0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    @PostConstruct
    public void init() {
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }

    }


    // 前缀树
    private class TrieNode {
        // 关键词结束标志
        private boolean isKeywordEnd = false;
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

        @Override
        public String toString() {
            return "TrieNode{" +
                    "isKeywordEnd=" + isKeywordEnd +
                    ", subNodes=" + subNodes +
                    '}';
        }
    }
}
