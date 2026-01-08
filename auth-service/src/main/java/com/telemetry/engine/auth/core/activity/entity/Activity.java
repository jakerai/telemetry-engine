package com.telemetry.engine.auth.core.activity.entity;

import com.telemetry.engine.auth.common.base.entity.BaseEntity;
import com.telemetry.engine.auth.core.activity.enums.Action;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "activity")
public class Activity extends BaseEntity {
  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false, length = 50)
  private Action action;

  @Column(name = "user_id")
  private Long userId;
  
  @Column(name = "remarks", length = 500)
  private String remarks;

  @Column(name = "ip", length = 45)
  private String ip; 
}
