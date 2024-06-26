package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory3;
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

	@GuiguLogin
	@GetMapping("/getBaseCategoryList")
	public Result getBaseCategoryList() {
		return Result.ok(baseCategoryService.getBaseCategoryList());
	}

	@GuiguLogin
	@GetMapping("/findAttribute/{category1Id}")
	public Result<List<BaseAttribute>> findAttribute(@PathVariable("category1Id") Long category1Id) {
		return Result.ok(baseCategoryService.findBaseAttributeBycategory1Id(category1Id));
	}

	@GuiguLogin
	@GetMapping("/findTopBaseCategory3/{category1Id}")
	public Result<List<BaseCategory3>> findTopBaseCategory3(@PathVariable("category1Id") Long category1Id) {
		return Result.ok(baseCategoryService.findTopBaseCategory3(category1Id));
	}

	@GuiguLogin
	@GetMapping("/getBaseCategoryList/{category1Id}")
	public Result<List<BaseCategory3>> getBaseCategoryList(@PathVariable("category1Id") Long category1Id) {
		return Result.ok(baseCategoryService.getBaseCategoryListById(category1Id));
	}
}

