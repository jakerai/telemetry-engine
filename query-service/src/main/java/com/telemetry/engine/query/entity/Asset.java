package com.telemetry.engine.query.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import com.telemetry.engine.query.base.entity.BaseEntity;
import com.telemetry.engine.query.enums.AssetStatus;
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
@Table("telemetry.asset")
public class Asset extends BaseEntity {

  @Column("name")
  private String name;
  
  @Column("model")
  private String model;
  
  @Column("type_id")
  private Long typeId;
      
  @Column("status")
  private AssetStatus status;
  
  @Column("owner_id")
  private Long ownerId;
 
  @Column("operator_id")
  private Long operatorId;
  
}
