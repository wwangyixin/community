
package com.newcoder.community.conifg;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

// 由于不能在第三方对象源码中加入注解@Component
// 因而使用配置文件来引用第三方对象
@Configuration // 配置类
public class AlphaConfig {

    @Bean // 返回的对象将被装配到bean容器中
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

}


