package com.telemetry.engine.auth.core.file.util;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import com.telemetry.engine.common.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

  private static final long MAX_SIZE = 1 * 1024 * 1024; // 1MB

  public static void validateImage(MultipartFile image) {
    log.info("[FileUtil.validateImage] Validating image");
    if (image == null || image.isEmpty()) {
      throw new FileUploadException("Image file is missing or empty");
    }

    if (image.getSize() > MAX_SIZE) {

      throw new FileUploadException("Image too large. Max size is 1MB");
    }
    String contentType = image.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new FileUploadException("File is not a valid image");
    }

    List<String> allowed = List.of("image/png", "image/jpeg", "image/jpg");
    if (!allowed.contains(contentType)) {
      throw new FileUploadException("Only PNG, JPG, JPEG allowed");
    }
  }

  public static String resolveOriginalFilename(MultipartFile file) {
    log.info("[FileUtil.resolveOriginalFilename] Resolving original filename");
    String originalFilename = file.getOriginalFilename();

    // Sanitizing filename (removing spaces, unsafe characters)
    String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

    // Adding UUID to avoid collisions
    String uniqueSuffix = UUID.randomUUID().toString();
    String finalFilename = sanitizedFilename + "-" + uniqueSuffix;
    return finalFilename;
  }


  public static String getExtension(String filename) {
    return filename.substring(filename.lastIndexOf(".") + 1);
  }

  public static String getMimeType(String fileName) {
    if (fileName == null)
      return "application/octet-stream";

    String extension = getExtension(fileName);

    switch (extension) {
      case "pdf":
        return "application/pdf";
      case "txt":
        return "text/plain";
      case "jpg":
      case "jpeg":
        return "image/jpeg";
      case "png":
        return "image/png";
      case "gif":
        return "image/gif";
      case "doc":
        return "application/msword";
      case "docx":
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
      case "xls":
        return "application/vnd.ms-excel";
      case "xlsx":
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
      case "zip":
        return "application/zip";
      default:
        return "application/octet-stream";
    }
  }


}
