package com.ouyang.community.service.file;

import javax.servlet.http.Part;
import java.io.IOException;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 16:03
 */
public interface IFileService {
    String uploadFile(Part file) throws IOException;

    String uploadFile(byte[] uploadBytes, String fileName);

    String uploadFile(Part file,String fileName) throws IOException;
}