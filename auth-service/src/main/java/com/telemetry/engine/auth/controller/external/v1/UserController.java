package com.telemetry.engine.auth.controller.external.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.telemetry.engine.auth.core.identity.dto.response.MyProfileResponse;
import com.telemetry.engine.auth.core.identity.dto.response.PictureUploadResponse;
import com.telemetry.engine.auth.core.identity.dto.response.UserProfileResponse;
import com.telemetry.engine.auth.core.user.service.UserService;
import com.telemetry.engine.common.dto.response.ServiceResponse;

/**
 * @author Vishal Rai
 */

@RequestMapping(path = "/api/external/v1/users")
@RestController
public class UserController {

  @Autowired
  private UserService userService;


  @GetMapping(path = "/me/profile", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<MyProfileResponse>> getMyProfile() {

    return ResponseEntity.ok(userService.getMyProfile());
  }

  @GetMapping(path = "/{userId}/profile", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<UserProfileResponse>> getUserProfile(
      @PathVariable(name = "userId", required = true) Long userId) {

    return ResponseEntity.ok(userService.getUserProfileById(userId));
  }

  @PostMapping(path = "/profile-picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceResponse<PictureUploadResponse>> uploadProfilePicture(
      @RequestPart(name = "file", required = true) MultipartFile file) {

    return ResponseEntity.ok(userService.updateUserProfilePicture(file));
  }

}
