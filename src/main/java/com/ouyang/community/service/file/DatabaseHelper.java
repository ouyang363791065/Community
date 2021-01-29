package com.ouyang.community.service.file;

import com.ouyang.community.entity.LogoImage;
import com.ouyang.community.mapper.LogoImageMapper;
import com.ouyang.community.utils.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 16:02
 */
@Slf4j
@Service("databaseHelper")
public class DatabaseHelper implements IFileService {
    @Resource
    LogoImageMapper logoImageMapper;

    @Override
    public String uploadFile(Part file) {
        return null;
    }

    @Override
    public String uploadFile(byte[] uploadBytes, String fileName) {
        if (uploadBytes.length > 50 * 1024) {
            // 需要进行压缩图片
            ByteArrayInputStream input = new ByteArrayInputStream(uploadBytes);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                Thumbnails.of(input).scale(1f).outputQuality(0.25f).toOutputStream(os);
                uploadBytes = os.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        LogoImage logoImage = new LogoImage();
        logoImage.setFilename(fileName);
        logoImage.setContent(uploadBytes);
        logoImage.setSize((long) uploadBytes.length);
        logoImage.setCreateTime(System.currentTimeMillis());
        logoImage.setUpdateTime(System.currentTimeMillis());
        logoImageMapper.insertLogoImage(logoImage);
        return fileName;
    }


    @Override
    public String uploadFile(Part file, String fileName) throws IOException {
        String originalFilename = file.getSubmittedFileName();
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 当存储pic_开头的文件名为数据库存储
        String fixFileName = "pic_" + fileName + "_" + UUID.randomUUID().toString().replace("-", "");
        fileName = fixFileName + suffixName;
        return uploadFile(StreamUtil.toByteArray(file.getInputStream()), fileName);
    }
}
