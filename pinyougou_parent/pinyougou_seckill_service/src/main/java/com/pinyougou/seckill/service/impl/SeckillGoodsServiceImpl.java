package com.pinyougou.seckill.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pinyougou.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		
		PageResult<TbSeckillGoods> result = new PageResult<TbSeckillGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbSeckillGoods> list = seckillGoodsMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillGoods> info = new PageInfo<TbSeckillGoods>(list);
        result.setTotal(info.getTotal());
		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insertSelective(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        seckillGoodsMapper.deleteByExample(example);
	}
	
	
	@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageResult<TbSeckillGoods> result = new PageResult<TbSeckillGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(seckillGoods!=null){			
						//如果字段不为空
			if (seckillGoods.getTitle()!=null && seckillGoods.getTitle().length()>0) {
				criteria.andLike("title", "%" + seckillGoods.getTitle() + "%");
			}
			//如果字段不为空
			if (seckillGoods.getSmallPic()!=null && seckillGoods.getSmallPic().length()>0) {
				criteria.andLike("smallPic", "%" + seckillGoods.getSmallPic() + "%");
			}
			//如果字段不为空
			if (seckillGoods.getSellerId()!=null && seckillGoods.getSellerId().length()>0) {
				criteria.andLike("sellerId", "%" + seckillGoods.getSellerId() + "%");
			}
			//如果字段不为空
			if (seckillGoods.getStatus()!=null && seckillGoods.getStatus().length()>0) {
				criteria.andLike("status", "%" + seckillGoods.getStatus() + "%");
			}
			//如果字段不为空
			if (seckillGoods.getIntroduction()!=null && seckillGoods.getIntroduction().length()>0) {
				criteria.andLike("introduction", "%" + seckillGoods.getIntroduction() + "%");
			}
	
		}

        //查询数据
        List<TbSeckillGoods> list = seckillGoodsMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillGoods> info = new PageInfo<TbSeckillGoods>(list);
        result.setTotal(info.getTotal());
		
		return result;
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<TbSeckillGoods> findList() {

		List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
		if(seckillGoods == null || seckillGoods.size() < 1) {
			Example example = new Example(TbSeckillGoods.class);
			Example.Criteria criteria = example.createCriteria();
			criteria.andEqualTo("status", "1");  //只查询审核对过的商品信息
			criteria.andGreaterThan("stockCount", 0);  //只查询有库存的商品
			Date now = new Date();  //当前时间
			criteria.andLessThanOrEqualTo("startTime", now);  //开始时间，小于等于当前时间
			criteria.andGreaterThanOrEqualTo("endTime", now);  //结束时间大于等于当前时间
			seckillGoods = seckillGoodsMapper.selectByExample(example);

			//把商品列表放入redis
			for (TbSeckillGoods seckillGood : seckillGoods) {
				redisTemplate.boundHashOps("seckillGoods").put(seckillGood.getId(), seckillGood);
			}
		}else{
			System.out.println("从缓存中加载了商品列表...");
		}
		return seckillGoods;
	}

	@Override
	public TbSeckillGoods findOneFromRedis(Long id) {
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
		return seckillGoods;
	}

}
