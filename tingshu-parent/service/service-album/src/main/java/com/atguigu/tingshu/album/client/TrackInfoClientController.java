package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.model.album.TrackInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/client/album/trackInfo")
public class TrackInfoClientController {

    @Autowired
    private TrackInfoService trackInfoService;

    @GetMapping("/getTrackPaidList/{trackId}/{trackCount}")
    public List<TrackInfo> getTrackPaidList(@PathVariable("trackId") Long trackId, @PathVariable Integer trackCount) {
        return trackInfoService.getTrackPaidList(trackId, trackCount);
    }
}
