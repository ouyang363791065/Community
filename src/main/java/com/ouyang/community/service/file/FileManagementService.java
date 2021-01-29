package com.ouyang.community.service.file;

import com.ouyang.community.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 16:04
 */
@Slf4j
@Service
public class FileManagementService {

    @Resource
    private Map<String, IFileService> fileServiceMap;

    /**
     * 上传文件
     *
     * @param companyId 公司ID
     * @param file      文件
     * @return
     * @throws IOException 异常
     */
    public String uploadFile(Long companyId, Part file, String fileName) throws IOException {
        IFileService fileService = getFileService();

        if (fileService == null) {
            log.info("Cannot find the target file system: " + companyId);
            return "";
        }

        return fileService.uploadFile(file, fileName);
    }

    /**
     * 上传字节流
     *
     * @param fileBytes
     * @param fileName
     * @return
     */
    public String uploadFile(byte[] fileBytes, String fileName) {
        IFileService fileService = getFileService();

        if (fileService == null) {
            log.info("Cannot find the target file system: " + fileSystem);
            return "";
        }

        return fileService.uploadFile(fileBytes, fileName);
    }

    @Value("${community.fileSystem}")
    String fileSystem;

    /**
     * 获取公司的文件系统
     * <p>
     *
     * @return 文件系统对象
     */
    private IFileService getFileService() {
        if (Objects.isNull(fileSystem)) {
            fileSystem = "";
        }
        switch (fileSystem) {
            case Constant.FILE_SYSTEM_LOCAL:
                return fileServiceMap.get("localHelper");
            case Constant.FILE_SYSTEM_DATABASE:
                return fileServiceMap.get("databaseHelper");
            default:
                return fileServiceMap.get("localHelper");
        }
    }
}
