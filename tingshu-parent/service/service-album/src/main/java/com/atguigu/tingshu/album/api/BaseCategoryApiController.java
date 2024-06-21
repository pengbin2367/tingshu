package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value="/api/album/category")
@SuppressWarnings({"unchecked", "rawtypes"})
public class BaseCategoryApiController {
	
	@Autowired
	private BaseCategoryService baseCategoryService;

	@GetMapping("/getBaseCategoryList")
	public Result getBaseCategoryList() {
		return Result.ok(baseCategoryService.getBaseCategoryList());
	}

	@GetMapping("/findAttribute/{category1Id}")
	public Result<List<BaseAttribute>> findAttribute(@PathVariable("category1Id") Long category1Id) {
		return Result.ok(baseCategoryService.findBaseAttributeBycategory1Id(category1Id));
	}
}

