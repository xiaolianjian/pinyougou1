package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.solr
 * @date 2018-11-28
 */
@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 把数据库的数据导入到索引库
     */
    public void importItemData(){
        //把所有商品查询出来
        TbItem where = new TbItem();
        where.setStatus("1");  //只导入启用的数据
        List<TbItem> itemList = itemMapper.select(where);
        System.out.println("共查询到 " + itemList.size() + " 条数据，正在导入索引库...");

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
        //导入索引库
        solrTemplate.saveBeans(solrItemList);
        solrTemplate.commit();
        System.out.println("已成功导入" + solrItemList.size() + "条数据!");
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        SolrUtil solrUtil = context.getBean(SolrUtil.class);

        solrUtil.importItemData();
    }
}
