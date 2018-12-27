package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service(interfaceClass = GoodsService.class)
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		
		PageResult<TbGoods> result = new PageResult<TbGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbGoods> list = goodsMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(list);
        result.setTotal(info.getTotal());
		return result;
	}

	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemMapper itemMapper;

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//保存商品基本信息
		goods.getGoods().setAuditStatus("0");  //未审核状态
		goodsMapper.insertSelective(goods.getGoods());

		int i = 1 / 0;
		//保存商品扩展信息
		//设置spu-id
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insertSelective(goods.getGoodsDesc());

		//是否启用规格
		saveItemList(goods);
	}

	/**
	 * 保存商品sku列表详情
	 * @param goods
	 */
	private void saveItemList(Goods goods) {
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			//保存商品sku信息
			for (TbItem item : goods.getItemList()) {
				//设置标题
				String title = goods.getGoods().getGoodsName();
				Map<String,String> map = JSON.parseObject(item.getSpec(), Map.class);
				for (String val : map.values()) {
					title += " " + val;
				}
				item.setTitle(title);
				//设置sku详细信息
				setItemValus(goods, item);
				//保存商品sku信息
				itemMapper.insertSelective(item);
			}
		}else{
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态,是否启用
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			//设置sku详细信息
			setItemValus(goods,item);
			itemMapper.insertSelective(item);
		}
	}

	/**
	 * 设置sku详细信息
	 * @param goods
	 * @param item
	 */
	private void setItemValus(Goods goods, TbItem item) {
		//卖点
		item.setSellPoint(goods.getGoods().getCaption());
		//商品图片
		List<Map> imgs = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if(imgs != null && imgs.size() > 0){
            item.setImage(imgs.get(0).get("url").toString());
        }
		//商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCategoryid());
		item.setCategory(itemCat.getName());
		//创建日期
		item.setCreateTime(new Date());
		//更新日期
		item.setUpdateTime(item.getCreateTime());
		//所属SPU-id
		item.setGoodsId(goods.getGoods().getId());
		//所属商家
		item.setSellerId(goods.getGoods().getSellerId());
		TbSeller seller = sellerMapper.selectByPrimaryKey(item.getSellerId());
		item.setSeller(seller.getNickName());
		//品牌信息
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//更新商品基本信息
		goods.getGoods().setAuditStatus("0");  //修改商品需要重新审核
		goodsMapper.updateByPrimaryKeySelective(goods.getGoods());
		//更新商品扩展信息
		goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());

		//更新商品sku列表信息
		//保存前先删除所有的sku列表
		TbItem where = new TbItem();
		where.setGoodsId(goods.getGoods().getId());
		itemMapper.delete(where);
		//再重新保存商品sku列表
		saveItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//商品基本信息
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//查询商品扩展信息
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);

		//查询商品sku信息
		TbItem where = new TbItem();
		where.setGoodsId(id);
		List<TbItem> items = itemMapper.select(where);
		goods.setItemList(items);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        //goodsMapper.deleteByExample(example);

		//修改的结果
		TbGoods record = new TbGoods();
		record.setIsDelete("1");  //标识已删除
		goodsMapper.updateByExampleSelective(record, example);
	}
	
	
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageResult<TbGoods> result = new PageResult<TbGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						//如果字段不为空
			if (goods.getSellerId()!=null && goods.getSellerId().length()>0) {
				criteria.andLike("sellerId", "%" + goods.getSellerId() + "%");
			}
			//如果字段不为空
			if (goods.getGoodsName()!=null && goods.getGoodsName().length()>0) {
				criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
			}
			//如果字段不为空
			if (goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0) {
				criteria.andLike("auditStatus", "%" + goods.getAuditStatus() + "%");
			}
			//如果字段不为空
			if (goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0) {
				criteria.andLike("isMarketable", "%" + goods.getIsMarketable() + "%");
			}
			//如果字段不为空
			if (goods.getCaption()!=null && goods.getCaption().length()>0) {
				criteria.andLike("caption", "%" + goods.getCaption() + "%");
			}
			//如果字段不为空
			if (goods.getSmallPic()!=null && goods.getSmallPic().length()>0) {
				criteria.andLike("smallPic", "%" + goods.getSmallPic() + "%");
			}
			//如果字段不为空
			if (goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0) {
				criteria.andLike("isEnableSpec", "%" + goods.getIsEnableSpec() + "%");
			}
			//如果字段不为空
			/*if (goods.getIsDelete()!=null && goods.getIsDelete().length()>0) {
				criteria.andLike("isDelete", "%" + goods.getIsDelete() + "%");
			}*/
			//删除的数据不应该再查询出来
			//criteria.andNotEqualTo("isDelete", "1");
			criteria.andIsNull("isDelete");
	
		}

        //查询数据
        List<TbGoods> list = goodsMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(list);
        result.setTotal(info.getTotal());
		
		return result;
	}

    @Override
    public void updateStatus(Long[] ids, String status) {
		/* 方式一：
		for (Long id : ids) {
			//goodsMapper.up
		}*/

		//方式二：
		//组装更新条件
		Example example = new Example(TbGoods.class);
		Example.Criteria criteria = example.createCriteria();
		List longs = Arrays.asList(ids);
		criteria.andIn("id", longs);
		//更新的结果
		TbGoods record = new TbGoods();
		record.setAuditStatus(status);
		goodsMapper.updateByExampleSelective(record,example);
	}

    @Override
    public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] goodsIds, String status) {
		Example example = new Example(TbItem.class);
		Example.Criteria criteria = example.createCriteria();
		//只查询启用的sku
		criteria.andEqualTo("status", status);
		//组装商品spu-id条件
		List longs = Arrays.asList(goodsIds);
		criteria.andIn("goodsId", longs);
		//开始查询
		List<TbItem> itemList = itemMapper.selectByExample(example);
		return itemList;
    }

}
