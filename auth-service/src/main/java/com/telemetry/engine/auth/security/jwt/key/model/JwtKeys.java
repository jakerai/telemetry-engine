package com.telemetry.engine.auth.security.jwt.key.model;

import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Version;
import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "jwt_keys")
public class JwtKeys extends BaseEntity {
  @Version
  private Long version;
  
  @JdbcTypeCode(SqlTypes.JSON) 
  @Column(name = "keys", columnDefinition = "jsonb")
  private List<Key> keys; // should contain max 2 elements: previous and current
  
}
