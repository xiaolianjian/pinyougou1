package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.utils.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付业务逻辑实现
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.pay.service
 * @date 2018-12-12
 */
@Service(timeout = 5000)
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${notifyurl}")
    private String notifyurl;
    @Value("${partnerkey}")
    private String partnerkey;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        Map map = new HashMap();
        try {
            //1、组装微信接口需要的入参
            Map param = new HashMap();
            param.put("appid",appid);  //公众号id
            param.put("mch_id",partner);  //商户号id
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字段串
            param.put("body", "品优购");  //body，用户在支付时能看到的信息
            param.put("out_trade_no", out_trade_no);  //商户订单号
            param.put("total_fee",total_fee); //支付金额
            param.put("spbill_create_ip", "127.0.0.1"); //终端ip
            param.put("notify_url", notifyurl);  //回调url
            /*JSAPI -JSAPI支付
            NATIVE -Native支付
            APP -APP支付*/
            param.put("trade_type", "NATIVE");  //支付类型
            //生成带签名的xml
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("正在发起统一下单请求,请求参数为：\n"+paramXml);
            //2、发起http请求，传入参数，得到结果
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);  //是否使用安全协议
            httpClient.setXmlParam(paramXml);  //设置请求参数
            httpClient.post();  //发起post请求
            String resultXml = httpClient.getContent();
            System.out.println("统一下单请求成功，返回参数为：\n"+resultXml);
            //3、解释结果
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            //返回需要的参数
            map.put("code_url", resultMap.get("code_url"));
            map.put("out_trade_no", out_trade_no);  //商户订单号
            map.put("total_fee", total_fee);  //支付总金额(分)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map map = new HashMap();
        try {
            //1、组装微信接口需要的入参
            Map param = new HashMap();
            param.put("appid",appid);  //公众号id
            param.put("mch_id",partner);  //商户号id
            param.put("out_trade_no", out_trade_no);  //商户订单号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字段串

            //生成带签名的xml
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("正在发起查询订单请求,请求参数为：\n"+paramXml);
            //2、发起http请求，传入参数，得到结果
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);  //是否使用安全协议
            httpClient.setXmlParam(paramXml);  //设置请求参数
            httpClient.post();  //发起post请求
            String resultXml = httpClient.getContent();
            System.out.println("查询订单请求成功，返回参数为：\\n"+resultXml);
            //3、解释结果
            map = WXPayUtil.xmlToMap(resultXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLogs").get(userId);
        return payLog;
    }

    @Override
    public Map closePay(String out_trade_no) {
        Map map = new HashMap();
        try {
            //1、组装微信接口需要的入参
            Map param = new HashMap();
            param.put("appid",appid);  //公众号id
            param.put("mch_id",partner);  //商户号id
            param.put("out_trade_no", out_trade_no);  //商户订单号
            param.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字段串

            //生成带签名的xml
            String paramXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("正在发起取消订单请求,请求参数为：\n"+paramXml);
            //2、发起http请求，传入参数，得到结果
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);  //是否使用安全协议
            httpClient.setXmlParam(paramXml);  //设置请求参数
            httpClient.post();  //发起post请求
            String resultXml = httpClient.getContent();
            System.out.println("取消订单请求成功，返回参数为：\\n"+resultXml);
            //3、解释结果
            map = WXPayUtil.xmlToMap(resultXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
