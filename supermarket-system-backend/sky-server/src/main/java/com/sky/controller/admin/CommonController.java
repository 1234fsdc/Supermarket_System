package com.sky.controller.admin;

import com.sky.utils.AliOssUtil;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.ObjectUtils.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;
    /**
     * 上传文件到OSS的接口
     *
     * @param file
     * @return 文件的请求路径
     */
    @ApiOperation(value = "文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("上传文件：{}", file);
        try {
            // 原始文件名
            String fileName = file.getOriginalFilename();
            //截取原始文件名的后缀 如ddfdsd.png
            String extension = fileName.substring(fileName.lastIndexOf("."));
            // 生成随机文件名
            String objectName = UUID.randomUUID().toString() + extension;
            
            //文件的请求路径
            String filePath = aliOssUtil.upload(file.getBytes(),fileName);   
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
