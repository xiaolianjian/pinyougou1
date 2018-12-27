package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbUser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品详情页业务逻辑实现
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.page.service.impl
 * @date 2018-12-3
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Value("${PAGE_DIR}")
    private String PAGE_DIR;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //获取Freemarker核心配置类
            Configuration cfg = freeMarkerConfigurer.getConfiguration();
            //获取模板
            Template template = cfg.getTemplate("item.ftl");
            //构建数据模型
            Map map = new HashMap();
            //组装数据模型
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods", goods);
            //商品扩展信息
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc", goodsDesc);
            //商品分类三级面包屑
            String category1Name = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            map.put("category1Name", category1Name);
            String category2Name = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            map.put("category2Name", category2Name);
            String category3Name = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            map.put("category3Name", category3Name);
            //查询商品sku列表
            Example example = new Example(TbItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("goodsId", goodsId);
            criteria.andEqualTo("status", "1");
            //以默认商品排序，便于后续逻辑中，默认sku商品的勾选
            example.setOrderByClause("isDefault DESC");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            map.put("itemList", itemList);

            Writer out = new FileWriter(PAGE_DIR + goodsId + ".html");
            //输出文档
            template.process(map, out);

            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
