package com.atguigu.tingshu.album.task;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlbumStatTask {

    @Autowired
    private AlbumInfoService albumInfoService;

    @Scheduled(cron = "10/20 * * * * *")
    public void albumStatTask() {
        albumInfoService.getAlbumInfoByStat(SystemConstant.ALBUM_STAT_PLAY);
        albumInfoService.getAlbumInfoByStat(SystemConstant.ALBUM_STAT_SUBSCRIBE);
        albumInfoService.getAlbumInfoByStat(SystemConstant.ALBUM_STAT_BROWSE);
        albumInfoService.getAlbumInfoByStat(SystemConstant.ALBUM_STAT_COMMENT);
    }
}
