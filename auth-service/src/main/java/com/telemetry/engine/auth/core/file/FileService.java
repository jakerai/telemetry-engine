package com.telemetry.engine.auth.core.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

  String storeFile(String folderName, MultipartFile file);

}
