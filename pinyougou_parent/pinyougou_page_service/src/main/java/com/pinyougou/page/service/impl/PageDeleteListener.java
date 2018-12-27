package com.pinyougou.page.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.File;
import java.io.Serializable;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.page.service.impl
 * @date 2018-12-4
 */
@Component
public class PageDeleteListener implements MessageListener {

    Logger logger = Logger.getLogger(PageDeleteListener.class);

    @Value("${PAGE_DIR}")
    private String PAGE_DIR;
    @Override
    public void onMessage(Message message) {

        try {
            //1接收消息
            ObjectMessage msg = (ObjectMessage) message;
            //2转换消息为Long[]
            Long[] ids = (Long[]) msg.getObject();
            //3删除文件
            for (Long id : ids) {
                File beDelete = new File(PAGE_DIR + id + ".html");
                //如果文件存在
                if (beDelete.exists()) {
                    //删除文件
                    boolean flag = beDelete.delete();
                    logger.info("删除了id为:" + id + "的商品详情页,删除结果为："+flag);
                }
            }
        } catch (Exception e) {
            logger.error("生成商品静态页时，发生了一个异常",e);
        }
    }
}
