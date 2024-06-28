package com.atguigu.tingshu.search.api;

import com.atguigu.tingshu.search.service.ItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "专辑详情管理")
@RestController
@RequestMapping("/api/search/albumInfo")
@SuppressWarnings({"unchecked", "rawtypes"})
public class ItemApiController {

	@Autowired
	private ItemService itemService;

	@GetMapping("/add/{albumId}")
	public String add(@PathVariable("albumId") Long albumId) {
		itemService.addAlbumFromDbToEs(albumId);
		return "success";
	}

	@DeleteMapping("/remove/{albumId}")
	public String remove(@PathVariable("albumId") Long albumId) {
		itemService.removeAlbumFromEs(albumId);
		return "success";
	}

	@GetMapping("/addAll")
	public String add() {
		for (Long albumId = 1L; albumId < 1577; albumId++) {
			itemService.addAlbumFromDbToEs(albumId);
		}
		return "success";
	}
}

