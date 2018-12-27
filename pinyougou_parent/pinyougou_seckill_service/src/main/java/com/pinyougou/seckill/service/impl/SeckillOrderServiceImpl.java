package com.pinyougou.seckill.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		
		PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setTotal(info.getTotal());
		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insertSelective(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKeySelective(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        seckillOrderMapper.deleteByExample(example);
	}
	
	
	@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageResult<TbSeckillOrder> result = new PageResult<TbSeckillOrder>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						//如果字段不为空
			if (seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0) {
				criteria.andLike("userId", "%" + seckillOrder.getUserId() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0) {
				criteria.andLike("sellerId", "%" + seckillOrder.getSellerId() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0) {
				criteria.andLike("status", "%" + seckillOrder.getStatus() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0) {
				criteria.andLike("receiverAddress", "%" + seckillOrder.getReceiverAddress() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0) {
				criteria.andLike("receiverMobile", "%" + seckillOrder.getReceiverMobile() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0) {
				criteria.andLike("receiver", "%" + seckillOrder.getReceiver() + "%");
			}
			//如果字段不为空
			if (seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0) {
				criteria.andLike("transactionId", "%" + seckillOrder.getTransactionId() + "%");
			}
	
		}

        //查询数据
        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(list);
        result.setTotal(info.getTotal());
		
		return result;
	}

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private IdWorker idWorker;

    @Override
	//synchronized-加一把方法，一次只能有一个人进来
    public synchronized void submitOrder(final Long seckillId, final String userId) {
		final TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if(seckillGoods == null){
			throw new RuntimeException("抢购的商品不存在或者商品已被抢购一空！");
		}
		if(seckillGoods.getStockCount() < 1){
			throw new RuntimeException("抱歉你来晚了，商品已被抢购一空！");
		}
		//先预占库存
		seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
		//最后一个人把商品抢了
		if(seckillGoods.getStockCount() == 0){
			//启动新的线程去完成以下逻辑
			new Thread(){
				@Override
				public void run() {
					//清空商品缓存
					redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
					//把商品信息更新到数据库
					seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
					super.run();
				}
			}.start();
		}

		//启动新的线程去完成以下逻辑
		new Thread(){
			@Override
			public void run() {
				//在支付之前，保存订单到redis
				long orderId = idWorker.nextId();
				TbSeckillOrder seckillOrder = new TbSeckillOrder();
				seckillOrder.setId(orderId);
				seckillOrder.setCreateTime(new Date());
				seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
				seckillOrder.setSeckillId(seckillId);
				seckillOrder.setSellerId(seckillGoods.getSellerId());
				seckillOrder.setUserId(userId);//设置用户ID
				seckillOrder.setStatus("0");//状态，未支付状态
				//把订单信息保存到Redis
				redisTemplate.boundHashOps("seckillOrders").put(userId, seckillOrder);
				super.run();
			}
		}.start();

	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrders").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		//1、把用户的订单查询出来
		TbSeckillOrder seckillOrders = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrders").get(userId);
		//2、判断用户订单号是与当前订单号一样
		if(seckillOrders == null){
			throw new RuntimeException("支付的订单不存在!");
		}
		if(seckillOrders.getId().longValue() != orderId.longValue()){
			throw new RuntimeException("支付的订单有异常!");
		}
		//3、保存订单与清订单缓存
		seckillOrders.setTransactionId(transactionId);//交易流水号
		seckillOrders.setPayTime(new Date());//支付时间
		seckillOrders.setStatus("1");//状态，改为已支付
		seckillOrderMapper.insertSelective(seckillOrders);//保存到数据库
		redisTemplate.boundHashOps("seckillOrder").delete(userId);//从redis中清除
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		//1、把用户的订单查询出来
		TbSeckillOrder seckillOrders = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrders").get(userId);
		if(seckillOrders != null && seckillOrders.getId().longValue() == orderId.longValue()){
			//删除缓存订单
			redisTemplate.boundHashOps("seckillOrders").delete(userId);

			//还原库存
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrders.getSeckillId());
			seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
		}
	}

}
