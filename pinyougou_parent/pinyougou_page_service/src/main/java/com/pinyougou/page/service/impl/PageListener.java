package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.page.service.impl
 * @date 2018-12-4
 */
@Component
public class PageListener implements MessageListener{
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {

        try {
            //1接收消息
            ObjectMessage msg = (ObjectMessage) message;
            //2转换消息为Long[]
            Long[] ids = (Long[]) msg.getObject();
            //3调用生成页面服务
            for (Long id : ids) {
                itemPageService.genItemHtml(id);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
