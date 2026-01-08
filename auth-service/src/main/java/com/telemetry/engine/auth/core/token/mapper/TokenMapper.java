package com.telemetry.engine.auth.core.token.mapper;

import com.telemetry.engine.auth.core.token.dto.TokenDto;
import com.telemetry.engine.auth.core.token.entity.Token;

public class TokenMapper {

  public static TokenDto toTokenDto(Token token) {

    return TokenDto.builder().id(token.getId()).type(token.getType()).userId(token.getUserId())
        .expiresAt(token.getExpiresAt()).build();
  }

}
