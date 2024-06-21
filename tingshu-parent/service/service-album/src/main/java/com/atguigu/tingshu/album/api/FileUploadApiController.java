package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.config.MinioConstantProperties;
import com.atguigu.tingshu.album.util.FileUtil;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("/api/album")
public class FileUploadApiController {

    @Autowired
    private MinioConstantProperties minioConstantProperties;
    @Autowired
    private FileUtil fileUtil;

    @SneakyThrows
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) {
        return Result.ok(fileUtil.upload(file));
    }
}
