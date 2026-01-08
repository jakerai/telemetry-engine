package com.telemetry.engine.auth.core.file.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.telemetry.engine.auth.core.file.service.FileService;
import com.telemetry.engine.auth.core.file.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

  @Override
  public String storeFile(String folderName, MultipartFile file) {
    log.info("[FileServiceImpl.storeFile] Storing file in folder={}", folderName);
    FileUtil.validateImage(file);

    String filename = FileUtil.resolveOriginalFilename(file);

    return filename;
  }

}
