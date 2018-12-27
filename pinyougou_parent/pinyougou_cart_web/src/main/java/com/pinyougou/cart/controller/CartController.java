package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.utils.CookieUtil;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.controller
 * @date 2018-12-9
 */
@RestController
@RequestMapping("cart")
public class CartController {
    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    //查询购物车列表
    @RequestMapping("findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Cart> cartList = new ArrayList<>();
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", true);
        //如果查找到了购物车列表
        if(StringUtils.isNotBlank(cartListStr)){
            cartList = JSON.parseArray(cartListStr, Cart.class);
        }
        //如果没有登录
        if("anonymousUser".equals(username)){
            System.out.println("从cookie中获取了购物车数据...");
        }else{   //用户已登录，查询Redis
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);

            //如果cookies中有购物车数据，合并购物车....
            if(cartList.size() > 0){
                if(cartListFromRedis == null){
                    cartListFromRedis = new ArrayList<>();
                }
                System.out.println("合并了cookie与redis的购物车数据...");
                cartList = cartService.mergeCartList(cartList, cartListFromRedis);
                //把购物车重新保存到Redis中
                cartService.saveCartListToRedis(username, cartList);
                //清空coodies购物车
                CookieUtil.deleteCookie(request,response,"cartList");
            }else{  //如果没有合并购物车需求，直接返回数据
                cartList = cartListFromRedis;
            }
            if(cartListFromRedis != null && cartListFromRedis.size() > 0){
                System.out.println("从redis中获取了购物车数据...");
                //cartList = cartListFromRedis;
            }
        }
        return cartList;
    }

    /**
     * 添加购物车逻辑
     * @param itemId 当前要添加的商品
     * @param num 添加的数量
     * @return
     */
    @RequestMapping("addGoodsToCartList")
    //allowCredentials可以省略，默认情况下就是true
    @CrossOrigin(origins = "http://localhost:8085",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId,Integer num){
        try {
            //设置可以访问的域，值设置为*时，允许所有域
            //response.setHeader("Access-Control-Allow-Origin", "http://localhost:8085");
            //如果需要操作cookies，必须加上此配置，标识服务端可以写cookies，
            // 并且Access-Control-Allow-Origin不能设置为*，因为cookies操作需要域名
            //response.setHeader("Access-Control-Allow-Credentials", "true");

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Cart> cartList = this.findCartList();
            //注意，这里不是内部方法调用，是远程调用，所以，这里一定要重新接收返回值
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //如果没有登录
            if("anonymousUser".equals(username)) {
                System.out.println("设置了cookie的购物车数据...");
                //保存到cookie中
                String jsonString = JSON.toJSONString(cartList);
                //这里暂且设置有效期为一天，实现情况跟业务需求而定
                CookieUtil.setCookie(request, response, "cartList", jsonString, 60 * 60 * 24, true);
            }else{
                System.out.println("设置了Redis的购物车数据...");
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加购物车成功！");
        }catch (RuntimeException e){
            return new Result(false, e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "添加购物车失败");
    }

}
