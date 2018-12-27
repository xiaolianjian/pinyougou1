package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 更新索引库监听器
 *
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2018-12-4
 */
@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        try {
            //1、接收消息
            TextMessage msg = (TextMessage) message;
            //2、把json转换为List
            List<TbItem> itemList = JSON.parseArray(msg.getText(), TbItem.class);
            //3、导入索引库
            List<SolrItem> solrItemList = new ArrayList<>();
            SolrItem solrItem = null;
            //导入数据到索引库
            for (TbItem item : itemList) {
                solrItem = new SolrItem();
                //把属性同步过来，使用深克隆，把所有属性加载过来
                BeanUtils.copyProperties(item, solrItem);

                //设置规格数据
                Map sepcMap = JSON.parseObject(item.getSpec(), Map.class);
                solrItem.setSpecMap(sepcMap);

                solrItemList.add(solrItem);
            }
            //更新索引库
            itemSearchService.importList(solrItemList);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
