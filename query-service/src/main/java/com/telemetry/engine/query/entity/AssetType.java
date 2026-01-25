package com.telemetry.engine.query.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import com.telemetry.engine.query.base.entity.BaseEntity;
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
@Table("telemetry.asset_type")
public class AssetType extends BaseEntity {
  
  @Column("name")
  private String name;
  
  @Column("category_id")
  private Long categoryId;
  
  @Column("description")
  private String description;
  
}
