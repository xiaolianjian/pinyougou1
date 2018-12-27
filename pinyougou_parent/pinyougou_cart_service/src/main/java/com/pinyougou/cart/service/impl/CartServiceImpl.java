package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.service.impl
 * @date 2018-12-9
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item == null){
            throw new RuntimeException("添加的商品信息不存在！");
        }
        //如果商品不是正常状态
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品不存在或者商品已下架！");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList,sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            //创建商品对象
            TbOrderItem orderItem = createOrderItem(num, item);

            //构建商品详情对象
            List<TbOrderItem> orderItemList = new ArrayList<>();
            orderItemList.add(orderItem);
            //4.2 将新建的购物车对象添加到购物车列表
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        //5.如果购物车列表中存在该商家的购物车
        }else{
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            //5.1. 如果没有，新增购物车明细
            if(orderItem == null){
                orderItem = createOrderItem(num, item);
                //追加到商品列表中
                cart.getOrderItemList().add(orderItem);
            }else{
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                //重新计算总价
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));

                //修改数量后，如果当前商品购买数量少于1，说明用户不要了
                if(orderItem.getNum() < 1){
                    cart.getOrderItemList().remove(orderItem);
                }

                //删除商品后，如果当前商家已经没有要买的商品
                if(cart.getOrderItemList().size() < 1){
                    //删除整个购物车节点
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                this.addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    /**
     * 验证当前商家中有没有添加过用户传入的商品
     * @param orderItemList 当前商家的商品列表
     * @param itemId 用户购买的商品
     * @return 查找到的结果
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            //查找到相应的商品
            if(orderItem.getItemId().longValue() == itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 验证当前商家有没有添加过购物车
     * @param cartList 当前的购物列表
     * @param sellerId 商家的id
     * @return 查找到的购物车结果
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId + "")){
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建购物车商品对象
     * @param num 购买的数量
     * @param item 查询的结果sku
     */
    private TbOrderItem createOrderItem(Integer num, TbItem item) {
        if(num < 1){
            throw new RuntimeException("请输入正确的商品数量");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());  //spu-id
        orderItem.setNum(num);  //购买数量
        orderItem.setPrice(item.getPrice());  //单价
        double totalFee = item.getPrice().doubleValue() * num;  //总价格
        orderItem.setTotalFee(new BigDecimal(totalFee));
        orderItem.setTitle(item.getTitle());
        orderItem.setPicPath(item.getImage());
        orderItem.setItemId(item.getId());
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }
}
