package com.telemetry.engine.auth.core.verification.service;

import com.telemetry.engine.auth.core.user.dto.UserDto;
import com.telemetry.engine.auth.core.verification.enums.VerificationChannel;
import com.telemetry.engine.auth.core.verification.enums.VerificationIntent;

public interface VerificationService {

  void sendVerification(UserDto userDto, VerificationIntent intent, VerificationChannel channel);

  void verifyCode(Long userId, String token);

}
