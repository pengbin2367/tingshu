package com.atguigu.tingshu.album.client;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.cache.GuiguCache;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client/album/albumInfo")
public class AlbumInfoClientController {

    @Autowired
    private AlbumInfoService albumInfoService;

    @Autowired
    private TrackInfoService trackInfoService;

    @GuiguCache(prefix = "getAlbumInfo:")
    @GetMapping("/getAlbumInfo/{albumId}")
    public AlbumInfo getAlbumInfo(@PathVariable(value = "albumId") Long albumId) {
        return albumInfoService.getById(albumId);
    }

    @GuiguCache(prefix = "getAlbumStatInfo:")
    @GetMapping("/getAlbumStatInfo/{albumId}")
    public Map<String, Integer> getAlbumStatInfo(@PathVariable(value = "albumId") Long albumId) {
        return albumInfoService.getAlbumStatInfo(albumId);
    }

    @GuiguCache(prefix = "getAlbumAttributeValue:")
    @GetMapping("/getAlbumAttributeValue/{albumId}")
    public List<AlbumAttributeValue> getAlbumAttributeValue(@PathVariable(value = "albumId") Long albumId) {
        return albumInfoService.getAlbumAttributeValue(albumId);
    }

    @GetMapping("/getAlbumInfoByTrackId/{trackId}")
    public AlbumInfo getAlbumInfoByTrackId(@PathVariable(value = "trackId") Long trackId) {
        TrackInfo trackInfo = trackInfoService.getById(trackId);
        return albumInfoService.getById(trackInfo.getAlbumId());
    }
}
