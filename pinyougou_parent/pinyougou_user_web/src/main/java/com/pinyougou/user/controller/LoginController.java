package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.user.controller
 * @date 2018-12-7
 */
@RestController
@RequestMapping("login")
public class LoginController {
    @RequestMapping("name")
    public Map showUserInfo(){
        Map map = new HashMap();
        //获取登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName", username);
        return map;
    }
}
