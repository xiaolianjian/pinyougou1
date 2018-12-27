package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
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
 * @description com.pinyougou.search.service.impl
 * @date 2018-12-4
 */
@Component
public class ItemDeleteSearchListener implements MessageListener{
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {

        try {
            //1、接收消息
            ObjectMessage msg = (ObjectMessage) message;
            //2、把消息接收并转换为Long[]
            Long[] ids = (Long[]) msg.getObject();
            //3、删除索引库
            itemSearchService.deleteByGoodsIds(ids);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
