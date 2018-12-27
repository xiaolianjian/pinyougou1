package com.pinyougou.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 认证信息扩展类
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.shop.service
 * @date 2018-11-21
 */
public class UserDetailsServiceImpl implements UserDetailsService {
    @Reference
    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //添加角色
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //查询数据库，验证商家信息
        TbSeller seller = sellerService.findOne(username);
        //查找到商家并且状态是已审核
        if(seller != null && "1".equals(seller.getStatus())){
            return new User(username,seller.getPassword(),authorities);
        }
        //返回空，代表验证不通过
        return null;
    }
}
