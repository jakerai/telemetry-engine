package com.telemetry.engine.auth.security.jwt.key.store.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telemetry.engine.auth.security.jwt.key.model.JwtKeys;
import com.telemetry.engine.auth.security.jwt.key.model.Key;
import com.telemetry.engine.auth.security.jwt.key.store.JwtKeyStore;
import com.telemetry.engine.common.exception.JwtKeyStoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Repository
@RequiredArgsConstructor
public class DatabaseJwtKeyStore implements JwtKeyStore {

  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;
  
  @Override
  public Optional<JwtKeys> findKeys() {
    log.info("Fetching keys from DB");
    String sql =
        "SELECT id, keys, version, created_at, created_by, modified_at, modified_by FROM jwt_keys LIMIT 1";

    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
        try {
          return JwtKeys.builder().id(rs.getLong("id"))
              .keys(objectMapper.readValue(rs.getString("keys"),
                  objectMapper.getTypeFactory().constructCollectionType(List.class, Key.class)))
              .version(rs.getLong("version")).build();
        } catch (Exception e) {
          throw new JwtKeyStoreException("Failed to deserialize JSONB keys");
        }
      }));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  @Transactional
  public void saveKeys(JwtKeys keys) {
    log.info("Saving/Updating keys");
    String jsonKeys = toJson(keys.getKeys());

    String sql = """
        INSERT INTO jwt_keys
            (id, keys, version, created_at, modified_at, created_by, modified_by)
        VALUES
            (1, ?::jsonb, 1, now(), now(), '0', '0')
        ON CONFLICT (id) DO UPDATE
        SET
            keys = EXCLUDED.keys,
            version = jwt_keys.version + 1,
            modified_at = now(),
            modified_by = '0'
        """;

    jdbcTemplate.update(sql, jsonKeys);
  }

  @Override
  @Transactional
  public boolean rotateKeysAtomically(String expectedCurrentKid, JwtKeys newKeys) {
    log.info("Attempting atomic rotation for kid={}", expectedCurrentKid);

    String jsonNewKeys = toJson(newKeys.getKeys());
    String filterJson = buildCurrentKidFilter(expectedCurrentKid);

    String sql = """
        UPDATE jwt_keys
        SET
            keys = ?::jsonb,
            version = version + 1,
            modified_at = now(),
            modified_by = '0'
        WHERE id = 1
        AND keys @> ?::jsonb
        """;

    int rowsAffected = jdbcTemplate.update(sql, jsonNewKeys, filterJson);

    if (rowsAffected == 0) {
      log.warn(
          "Error while rotating keys atomically. Rotation skipped: Kid {} is no longer CURRENT or record changed.",
          expectedCurrentKid);
    }

    return rowsAffected > 0;
  }


  private String buildCurrentKidFilter(String kid) {
    try {
      return objectMapper
          .writeValueAsString(List.of(java.util.Map.of("kid", kid, "status", "CURRENT")));
    } catch (Exception e) {
      throw new JwtKeyStoreException("Failed to build JSON filter");
    }
  }

  private String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new JwtKeyStoreException("Error mapping keys to JSON");
    }
  }

}
