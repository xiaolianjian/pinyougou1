package com.pinyougou.user.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证扩展
 * @author Steven
 * @version 1.0
 * @description com.itheima.demo.service
 * @date 2018-12-7
 */
public class UserDetailServiceImpl implements UserDetailsService{
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(username + "进入了loadUserByUsername...");
        //构建角色列表
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        //注意跟cas整合后，这里的密码要返回一个""，因为真的认证交给CAS管理
        return new User(username,"",authorities);
    }
}
