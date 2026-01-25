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
@Table("telemetry.asset_category")
public class AssetCategory extends BaseEntity {
  
  @Column("name")
  private String name;
  
  @Column("description")
  private String description;
}
