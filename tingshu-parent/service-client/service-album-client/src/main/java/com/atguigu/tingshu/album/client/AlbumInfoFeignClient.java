package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-album", path = "/client/album/albumInfo", contextId = "albumInfoFeignClient")
public interface AlbumInfoFeignClient {

    @GetMapping("/getAlbumInfo/{albumId}")
    public AlbumInfo getAlbumInfo(@PathVariable(value = "albumId") Long albumId);

    @GetMapping("/getAlbumStatInfo/{albumId}")
    public Map<String, Integer> getAlbumStatInfo(@PathVariable(value = "albumId") Long albumId);

    @GetMapping("/getAlbumAttributeValue/{albumId}")
    public List<AlbumAttributeValue> getAlbumAttributeValue(@PathVariable(value = "albumId") Long albumId);
}