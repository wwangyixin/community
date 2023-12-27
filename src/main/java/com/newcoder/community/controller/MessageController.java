package com.newcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.newcoder.community.annotation.LoginRequired;
import com.newcoder.community.entity.Message;
import com.newcoder.community.entity.Page;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.MessageService;
import com.newcoder.community.service.UserService;
import com.newcoder.community.util.CommunityConstant;
import com.newcoder.community.util.CommunityUtil;
import com.newcoder.community.util.HostHolder;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    // 私信列表
    @LoginRequired
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {

        User user = hostHolder.getUser();
        int userId = user.getId();

        // 分页信息
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));
        page.setPath("/letter/list");

        List<Map<String, Object>> conversations = new ArrayList<>();
        List<Message> conversationList = messageService.findConversations(userId, page.getOffset(), page.getLimit());
        for (Message message : conversationList) {
            String conversationId = message.getConversationId();

            Map<String, Object> map = new HashMap<>();
            map.put("target", getLetterTarget(conversationId));
            map.put("letterCount", messageService.findLetterCount(conversationId));
            map.put("conversation", message);
            map.put("unreadCount", messageService.findLetterUnreadCount(userId, conversationId));

            conversations.add(map);
        }
        model.addAttribute("conversations", conversations);
        // 查询未读消息数量
        model.addAttribute("letterUnreadCount",
                messageService.findLetterUnreadCount(userId, null));
        model.addAttribute("noticeUnreadCount",
                messageService.findNoticeUnreadCount(userId, null));
        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {

        User user = hostHolder.getUser();
        int userId = user.getId();

        // 分页信息
        page.setLimit(5);
        page.setRows(messageService.findLetterCount(conversationId));
        page.setPath("/letter/detail/" + conversationId);

        // 私信列表
        List<Map<String, Object>> letters = new ArrayList<>();
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        for (Message message : letterList) {
            Map<String, Object> map = new HashMap<>();
            map.put("letter", message);
            map.put("fromUser", userService.findUserById(message.getFromId()));
            letters.add(map);
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getUnreadLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }
        return "/site/letter-detail";
    }


    private User getLetterTarget(String conversationId) {
        User user = hostHolder.getUser();
        int userId = user.getId();
        int small = Integer.parseInt(conversationId.substring(0, conversationId.lastIndexOf("_")));
        int big = Integer.parseInt(conversationId.substring(conversationId.lastIndexOf("_") + 1));
        int targetId = userId == small ? big : small;
        return userService.findUserById(targetId);
    }

    private List<Integer> getUnreadLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0)
                ids.add(message.getId());
            }
        }
        return ids;
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {

        User user = hostHolder.getUser();
        int fromId = user.getId();
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        int toId =target.getId();

        String conversationId = Math.min(fromId, toId) + "_" + Math.max(fromId, toId);

        Message message = new Message();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setConversationId(conversationId);
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());

        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET) 
    public String getNoticeList(Model model) {
        
        User user = hostHolder.getUser();

        // 查看评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);

        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            System.out.println(message.getContent());
            String content = HtmlUtils.htmlUnescape(message.getContent());
            System.out.println(content);
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
            model.addAttribute("commentNotice",messageVO);
        }


        // 查看点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);

        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
            model.addAttribute("likeNotice",messageVO);
        }


        // 查看关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);

        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
            model.addAttribute("followNotice",messageVO);
        }


        // 查询未读消息数量
        model.addAttribute("letterUnreadCount",
                messageService.findLetterUnreadCount(user.getId(), null));
        model.addAttribute("noticeUnreadCount",
                messageService.findNoticeUnreadCount(user.getId(), null));
        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic")String topic, Page page, Model model) {

        User user = hostHolder.getUser();

        // 分页处理
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        System.out.println("==================================flag1==================================");

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        System.out.println("==================================flag2==================================");
        List<Map<String, Object>> messageVOList = new ArrayList<>();
        if (noticeList != null) {
            // 封装数据
            for (Message message : noticeList) {
                Map<String, Object> messageVO = new HashMap<>();
                messageVO.put("notice", message);
                String content = HtmlUtils.htmlUnescape(message.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
                messageVO.put("entityType", data.get("entityType"));
                messageVO.put("entityId", data.get("entityId"));
                messageVO.put("postId", data.get("postId"));
                messageVO.put("fromUser", userService.findUserById(message.getFromId()));
                messageVOList.add(messageVO);
            }
        }
        model.addAttribute("notices", messageVOList);

        // 设置已读
        messageService.readMessage(getUnreadLetterIds(noticeList));

        return "/site/notice-detail";
    }

}
