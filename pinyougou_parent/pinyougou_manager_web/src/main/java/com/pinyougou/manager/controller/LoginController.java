package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.manager.controller
 * @date 2018-11-21
 */
@RestController
@RequestMapping("login")
public class LoginController {

    @RequestMapping("name")
    public Map name(){
        Map result = new HashMap();
        //获取登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        result.put("loginName", username);
        return result;
    }
}
