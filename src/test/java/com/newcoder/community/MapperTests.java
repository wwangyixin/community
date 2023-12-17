package com.newcoder.community;

import com.newcoder.community.dao.AlphaDao;
import com.newcoder.community.dao.DiscussPostMapper;
import com.newcoder.community.dao.UserMapper;
import com.newcoder.community.entity.DiscussPost;
import com.newcoder.community.entity.User;
import com.newcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)


public class MapperTests {


    @Autowired
    private UserMapper userMapper;


    //    User selectById(int id);
    //    User selectByName(String username);
    //    User selectByEmail(String email);
    //    int insertUser(User user);
    //    int updateStatus(int id, int status);
    //    int UpdateHeader(int id, String headerUrl);
    //    int updatePassword(int id, String password);

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(11);
        System.out.println(user);

        user = userMapper.selectByName("guanyu");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder115@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("1230221");
        user.setSalt("abc");
        user.setEmail("text@qq.com");
        user.setHeaderUrl("http://images.nowcoder.com/head/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser() {
//        int updateStatus(int id, int status);
//
//        int UpdateHeader(int id, String headerUrl);
//
//        int updatePassword(int id, String password);
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.UpdateHeader(150, "http://images.nowcoder.com/head/101t.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "88888");
        System.out.println(rows);

    }

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectDiscussPost() {
        int count = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(count);

        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 200);
        System.out.println(list.size());

        System.out.println(discussPostMapper.selectDiscussPosts(149, 0, 50));
    }



}
