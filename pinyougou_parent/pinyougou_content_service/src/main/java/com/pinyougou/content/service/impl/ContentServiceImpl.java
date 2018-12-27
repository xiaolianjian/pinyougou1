package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * 业务逻辑实现
 * @author Steven
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private RedisTemplate redisTemplate;

    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
		//先查询缓存中有没有相关的广告
		List<TbContent> contents = (List<TbContent>) redisTemplate.boundHashOps("contents").get(categoryId);
		//如果缓存中没有,查询数据
		if(contents == null || contents.size() < 1) {
			//查询条件组装
			Example example = new Example(TbContent.class);
			Example.Criteria criteria = example.createCriteria();
			criteria.andEqualTo("categoryId", categoryId);
			criteria.andEqualTo("status", "1");

			//排序字段设置:字段名 排序方式(asc|desc)，多个字段通过","分隔
			example.setOrderByClause("sortOrder asc");
			//查询数据
			contents = contentMapper.selectByExample(example);
			//把数据放入缓存
			redisTemplate.boundHashOps("contents").put(categoryId,contents);
		}else {
			System.out.println("从缓存中加载了广告内容");
		}
		return contents;
    }

    /**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.select(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		
		PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbContent> list = contentMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());
		return result;
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insertSelective(content);

		//清除缓存
		redisTemplate.boundHashOps("contents").delete(content.getCategoryId());
	}

	/*public static void main(String[] args) {
		Long a = 128L;
		Long b = 128L;
		System.out.println(a.longValue() == b.longValue());
	}*/

	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//识别类型有没有被更改，在更新之前，先跟据id查询出原来的内容对象
		TbContent beUpdate = contentMapper.selectByPrimaryKey(content.getId());
		if(content.getCategoryId().longValue() != beUpdate.getCategoryId().longValue()){
			//清除原来分类的缓存
			redisTemplate.boundHashOps("contents").delete(beUpdate.getCategoryId());
		}
		//更新数据
		contentMapper.updateByPrimaryKeySelective(content);
		//清除缓存
		redisTemplate.boundHashOps("contents").delete(content.getCategoryId());
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

		//在删除之前，查询将要删除的所有对象的categoryid
		List<TbContent> contents = contentMapper.selectByExample(example);
		for (TbContent content : contents) {
			//清除缓存
			redisTemplate.boundHashOps("contents").delete(content.getCategoryId());
		}

		//跟据查询条件删除数据
        contentMapper.deleteByExample(example);
	}
	
	
	@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						//如果字段不为空
			if (content.getTitle()!=null && content.getTitle().length()>0) {
				criteria.andLike("title", "%" + content.getTitle() + "%");
			}
			//如果字段不为空
			if (content.getUrl()!=null && content.getUrl().length()>0) {
				criteria.andLike("url", "%" + content.getUrl() + "%");
			}
			//如果字段不为空
			if (content.getPic()!=null && content.getPic().length()>0) {
				criteria.andLike("pic", "%" + content.getPic() + "%");
			}
			//如果字段不为空
			if (content.getStatus()!=null && content.getStatus().length()>0) {
				criteria.andLike("status", "%" + content.getStatus() + "%");
			}
	
		}

        //查询数据
        List<TbContent> list = contentMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());
		
		return result;
	}
	
}
