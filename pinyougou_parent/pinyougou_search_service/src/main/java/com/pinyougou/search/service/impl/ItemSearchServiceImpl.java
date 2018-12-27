package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品搜索业务逻辑实现
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2018-11-28
 */
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {
        //结果集
        Map map = new HashMap();
        //1、关键字搜索商品列表
        searchList(searchMap, map);

        //2、分组查询商品分类列表
        searchCategoryList(searchMap,map);

        //3、查询品牌与规格列表
        List<String> categoryList = map.get("categoryList") == null ? new ArrayList() : (List<String>) map.get("categoryList");
        if(categoryList.size() > 0){
            String category = searchMap.get("category") == null ? "" : searchMap.get("category").toString();
            //如果用户选择了商品分类，查询相应的规格与品牌列表
            if(StringUtils.isNotBlank(category)){
                searchBrandAndSpecList(category,map);
            }else{
                searchBrandAndSpecList(categoryList.get(0),map);
            }
        }
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(Long[] goodsIdList) {
        //组装删除条件
        SolrDataQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        //删除索引库
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询品牌与规格列表
     * @param category 商品分类名称
     * @param map 包装的结果集
     */
    private void searchBrandAndSpecList(String category, Map map) {
        //先跟据商品分类名称查询模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCats").get(category);

        //查询品牌列表
        List<Map> brandIds = (List<Map>) redisTemplate.boundHashOps("brandIds").get(typeId);
        map.put("brandIds", brandIds);
        //查询规格列表
        List<Map> specIds = (List<Map>) redisTemplate.boundHashOps("specIds").get(typeId);
        map.put("specIds", specIds);
    }

    /**
     * 分组查询商品分类列表
     * @param searchMap 查询条件
     * @param map 包装的结果
     */
    private void searchCategoryList(Map searchMap,Map map){
        //商品分类列表
        List<String> categoryList = new ArrayList();

        //1.创建查询条件对象query = new SimpleQuery()
        Query query = new SimpleQuery();
        //2.复制之前的Criteria组装查询条件的代码
        if(searchMap != null) {
            String keywords = searchMap.get("keywords") == null ? null : searchMap.get("keywords").toString();
            //"  ".isNotBlank() == true  "  ".isNotEmpty() == false
            if(StringUtils.isNotBlank(keywords)){
                Criteria criteria = new Criteria("item_keywords").is(keywords);
                query.addCriteria(criteria);
            }
        }
        //3.创建分组选项对象new GroupOptions().addGroupByField(域名)
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //4.设置分组对象query.setGroupOptions
        query.setGroupOptions(groupOptions);
        //5.得到分组页对象page = solrTemplate.queryForGroupPage
        GroupPage<SolrItem> page = solrTemplate.queryForGroupPage(query, SolrItem.class);
        //6.得到分组结果集groupResult = page.getGroupResult(域名)
        GroupResult<SolrItem> groupResult = page.getGroupResult("item_category");
        //7.得到分组结果入口groupEntries = groupResult.getGroupEntries()
        Page<GroupEntry<SolrItem>> groupEntries = groupResult.getGroupEntries();
        //8.得到分组入口集合content = groupEntries.getContent()
        List<GroupEntry<SolrItem>> content = groupEntries.getContent();
        //9.遍历分组入口集合content.for(entry)，记录结果entry.getGroupValue()
        for (GroupEntry<SolrItem> entry : content) {
            categoryList.add(entry.getGroupValue());
        }
        //返回商品分类列表
        map.put("categoryList", categoryList);
    }

    /**
     * 关键字搜索商品列表
     * @param searchMap
     * @param map
     */
    private void searchList(Map searchMap, Map map) {
        //2.构建query高亮查询对象new SimpleHighlightQuery
        HighlightQuery query = new SimpleHighlightQuery();
        //3.复制之前的Criteria组装查询条件的代码
        if(searchMap != null) {
            //3.1 关键字过滤
            String keywords = searchMap.get("keywords") == null ? null : searchMap.get("keywords").toString();
            //"  ".isNotBlank() == true  "  ".isNotEmpty() == false
            if(StringUtils.isNotBlank(keywords)){
                //处理多余的空格
                keywords = keywords.replaceAll(" ", "");
                searchMap.put("keywords", keywords);
                Criteria criteria = new Criteria("item_keywords").is(keywords);
                query.addCriteria(criteria);
            }

            //3.2商品分类过滤查询
            String category = searchMap.get("category") == null ? null : searchMap.get("category").toString();
            if(StringUtils.isNotBlank(category)){
                Criteria criteria = new Criteria("item_category").is(category);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }

            //3.3品牌过滤查询
            String brand = searchMap.get("brand") == null ? null : searchMap.get("brand").toString();
            if(StringUtils.isNotBlank(brand)){
                Criteria criteria = new Criteria("item_brand").is(brand);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                query.addFilterQuery(filterQuery);
            }

            //3.4规格过滤查询
            String specStr = searchMap.get("spec") == null ? "" : searchMap.get("spec").toString();
            if (StringUtils.isNotBlank(specStr)) {
                Map<String,String> specMap = JSON.parseObject(specStr, Map.class);
                for (String key : specMap.keySet()) {
                    Criteria criteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                    FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                    query.addFilterQuery(filterQuery);
                }
            }

            //3.5价格区间查询 0-500 500-1000....3000-*
            String price = searchMap.get("price") == null ? "" : searchMap.get("price").toString();
            if (StringUtils.isNotBlank(price)) {
                String[] split = price.split("-");

                //between遇到*时，会报语法错误
                //Criteria criteria = new Criteria("item_price").between(split[0],split[1]);

                //价格大于等于传入参数
                if(!split[0].equals("0")){
                    Criteria criteria = new Criteria("item_price").greaterThanEqual(split[0]);
                    FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                    query.addFilterQuery(filterQuery);
                }
                //价格小于等于传入参数
                if(!split[1].equals("*")){
                    Criteria criteria = new Criteria("item_price").lessThanEqual(split[1]);
                    FilterQuery filterQuery = new SimpleFilterQuery(criteria);
                    query.addFilterQuery(filterQuery);
                }
            }

            //3.6分页查询条件
            //当前页,默认第1页
            Integer pageNo = searchMap.get("pageNo") == null ? 1 : new Integer(searchMap.get("pageNo").toString());
            //每页查询的记录数,默认20条
            Integer pageSize = searchMap.get("pageSize") == null ? 20 : new Integer(searchMap.get("pageSize").toString());

            //设置分页条件
            query.setOffset((pageNo - 1) * pageSize);
            query.setRows(pageSize);

            //3.7排序查询
            //排序方式：//ASC  DESC
            String sortValue= searchMap.get("sort") == null ? "" : searchMap.get("sort").toString().toUpperCase();
            //排序的业务域，
            String sortField= searchMap.get("sortField") == null ? "" : searchMap.get("sortField").toString();//排序字段
            if("ASC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.ASC,"item_" + sortField);
                query.addSort(sort);
            }
            if("DESC".equals(sortValue)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_" + sortField);
                query.addSort(sort);
            }

        }
        //4.调用query.setHighlightOptions()方法，构建高亮数据三步曲：
        // new HighlightOptions().addField(高亮业务域)，.setSimpleP..(前缀)，.setSimpleP..(后缀)
        HighlightOptions hOptions = new HighlightOptions();
        hOptions.addField("item_title");
        hOptions.setSimplePrefix("<em style=\"color:red;\">");
        hOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(hOptions);
        //1.调用solrTemplate.queryForHighlightPage(query,class)方法，高亮查询数据
        // 5.接收solrTemplate.queryForHighlightPage的返回数据，定义page变量
        HighlightPage<SolrItem> page = solrTemplate.queryForHighlightPage(query, SolrItem.class);
        //6.遍历解析page对象，page.getHighlighted().for，item = h.getEntity()，
        // item.setTitle(h.getHighlights().get(0).getSnipplets().get(0))，在设置高亮之前最好判断一下;
        for (HighlightEntry<SolrItem> highlightEntry : page.getHighlighted()) {
            SolrItem item = highlightEntry.getEntity();
            //判断是否有高亮数据
            if(highlightEntry.getHighlights().size() > 0 &&
                    highlightEntry.getHighlights().get(0).getSnipplets().size() > 0){
                //设置高亮数据
                item.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        //7.在循环完成外map.put("rows", page.getContent())返回数据列表
        map.put("rows", page.getContent());
        map.put("total", page.getTotalElements());   //返回总记录数
        map.put("totalPage", page.getTotalPages());  //返回总页数
    }
}
