package com.telemetry.engine.query.base.entity;

import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
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
public class BaseEntity {
  
  @Column("id")
  @Id
  private Long id;

  @Column("created_by")
  private Long createdBy;

  @Column("modified_by")
  private Long modifiedBy;

  @Column("created_at")
  @CreatedDate
  private Instant createdAt;

  @Column("modified_at")
  @LastModifiedDate
  private Instant modifiedAt;
  
}
