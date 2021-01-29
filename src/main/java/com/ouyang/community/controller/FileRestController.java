package com.ouyang.community.controller;

import com.ouyang.community.entity.LogoImage;
import com.ouyang.community.service.LogoImageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 16:15
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileRestController {
    @Autowired
    private LogoImageService logoImageService;

    @RequestMapping(value = "/{type}/header/{fileId:.+}")
    public void view(@PathVariable(value = "fileId") String fileId,@PathVariable String type, HttpServletResponse httpServletResponse) throws IOException {
        if(StringUtils.isEmpty(fileId)){
            return;
        }
        List<String> reqParams = Arrays.asList( "user", "company", "product","default","system");
        if(!reqParams.contains(type)){
            return;
        }

        String[] picSplit = fileId.split("_");
        String picType = picSplit[0];
        if (fileId.startsWith("pic_")) {
            picType = picSplit[1];
        }
        if(!picType.equals(type)){
            return;
        }

        FileInputStream inputStream = null;
        byte[] data;
        try {
            if (fileId.startsWith("pic_")) {
                // 说明是数据库存储的图片
                LogoImage logoImage = logoImageService.getLogoImage(fileId);
                data = logoImage.getContent();
            } else {
                String filePath = "G://";
                if (!filePath.endsWith("/")) {
                    filePath = filePath + "/";
                }
                filePath = filePath + fileId;
                File file = new File(filePath);
                inputStream = new FileInputStream(file);
                int length = inputStream.available();
                data = new byte[length];
                inputStream.read(data);
            }
            httpServletResponse.setContentLength(data.length);
            String fileType = fileId.substring(fileId.lastIndexOf(".") + 1).toLowerCase();
            httpServletResponse.setContentType(getContentType(fileType));
            OutputStream toClient = httpServletResponse.getOutputStream();
            toClient.write(data);
            toClient.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private String getContentType(String fileType) {
        String contentType = null;
        fileType = fileType.toLowerCase();
        switch (fileType) {
            case "jpg":
                contentType = "image/jpeg";
                break;
            case "jpeg":
                contentType = "image/jpeg";
                break;
            case "png":
                contentType = "image/png";
                break;
            case "tif":
                contentType = "image/tif";
            case "tiff":
                contentType = "image/tiff";
                break;
            case "ico":
                contentType = "image/x-icon";
                break;
            case "bmp":
                contentType = "image/bmp";
                break;
            case "gif":
                contentType = "image/gif";
                break;
            default:
                contentType = "image/jpeg";
                break;
        }
        return contentType;
    }
}
