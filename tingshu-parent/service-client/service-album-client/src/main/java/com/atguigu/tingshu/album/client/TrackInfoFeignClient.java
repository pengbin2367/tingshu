package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.model.album.TrackInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author qy
 */
@FeignClient(value = "service-album", path = "/client/album/trackInfo", contextId = "trackInfoFeignClient")
public interface TrackInfoFeignClient {

    @GetMapping("/getTrackPaidList/{trackId}/{trackCount}")
    public List<TrackInfo> getTrackPaidList(@PathVariable("trackId") Long trackId, @PathVariable Integer trackCount);
}