package com.telemetry.engine.auth.core.file.impl;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.telemetry.engine.auth.core.file.FileService;
import com.telemetry.engine.auth.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileServiceImpl implements FileService {

  @Override
  public String storeFile(String folderName, MultipartFile file) {
    log.info("Storing file in folder={}", folderName);
    FileUtil.validateImage(file);

    String filename = FileUtil.resolveOriginalFilename(file);

    return filename;
  }

}
