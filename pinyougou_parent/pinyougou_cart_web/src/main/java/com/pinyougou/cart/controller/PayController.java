package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.utils.IdWorker;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.cart.controller
 * @date 2018-12-12
 */
@RestController
@RequestMapping("pay")
public class PayController {
    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private OrderService orderService;

    //生成二维码
    @RequestMapping("createNative")
    public Map createNative(){
        /*IdWorker worker = new IdWorker();
        String out_trade_no = worker.nextId() + "";*/
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = weixinPayService.searchPayLogFromRedis(userId);
        return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
    }

    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        int i = 1;
        //一直查询订单支付状态
        while (true){
            Map map = weixinPayService.queryPayStatus(out_trade_no);
            if(map.size() < 1){
                result = new Result(false,"支付失败！");
                break;
            }
            //如果订单支付成功
            if("SUCCESS".equals(map.get("trade_state"))){
                result = new Result(true,"支付成功！");
                //支付成功后，更新订单状态与支付日志，清空缓存
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id").toString());
                break;
            }

            //如果超过5(100)分钟没支付，提示超时
            if(i > 100){
                result = new Result(false,"支付超时");
                break;
            }
            try {
                //休息3秒，再发起一次请求
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        return result;
    }
}
