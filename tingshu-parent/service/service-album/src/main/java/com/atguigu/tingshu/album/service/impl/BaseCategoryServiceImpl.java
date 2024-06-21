package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.*;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {

	@Autowired
	private BaseCategory1Mapper baseCategory1Mapper;

	@Autowired
	private BaseCategory2Mapper baseCategory2Mapper;

	@Autowired
	private BaseCategory3Mapper baseCategory3Mapper;

	@Autowired
	private BaseCategoryViewMapper baseCategoryViewMapper;

	@Autowired
	private BaseAttributeMapper baseAttributeMapper;

	@Override
	public List<JSONObject> getBaseCategoryList() {
		// 获取base_category_view视图中所有数据
		List<BaseCategoryView> c1List = baseCategoryViewMapper.selectList(null);
		// 根据一级分类进行分组，以一级分类的id为key
		Map<Long, List<BaseCategoryView>> c1Map = c1List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
		return c1Map.entrySet().stream().map(c1 -> {
			JSONObject c1Json = new JSONObject();
			c1Json.put("categoryId", c1.getKey());
			List<BaseCategoryView> c2List = c1.getValue();
			c1Json.put("categoryName", c2List.get(0).getCategory1Name());
			// 根据二级分类进行分组，以二级分类的id为key
			Map<Long, List<BaseCategoryView>> c2Map = c2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
			List<JSONObject> c2Collect = c2Map.entrySet().stream().map(c2 -> {
				JSONObject c2Json = new JSONObject();
				c2Json.put("categoryId", c2.getKey());
				List<BaseCategoryView> c3List = c2.getValue();
				c2Json.put("categoryName", c3List.get(0).getCategory2Name());
				// 遍历以二级分类为分组的数据，取出其中的三级分类
				List<JSONObject> c3Collect = c3List.stream().map(c3 -> {
					JSONObject c3Json = new JSONObject();
					c3Json.put("categoryId", c3.getCategory3Id());
					c3Json.put("categoryName", c3.getCategory3Name());
					return c3Json;
				}).collect(Collectors.toList());
				c2Json.put("categoryChild", c3Collect);
				return c2Json;
			}).collect(Collectors.toList());
			c1Json.put("categoryChild", c2Collect);
			return c1Json;
		}).collect(Collectors.toList());
	}

	@Override
	public List<BaseAttribute> findBaseAttributeBycategory1Id(Long category1Id) {
		return baseAttributeMapper.selectBaseAttributeBycategory1Id(category1Id);
	}
}
