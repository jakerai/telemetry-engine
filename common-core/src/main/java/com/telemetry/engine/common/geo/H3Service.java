package com.telemetry.engine.common.geo;

import java.util.List;
import com.uber.h3core.H3Core;

public class H3Service {

  private final H3Core h3;

  public H3Service() {
    try {
      this.h3 = H3Core.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to init H3", e);
    }
  }

  /* Convert lat/lon to H3 index */
  public String toH3CellAddress(double lat, double lon, int resolution) {
    return h3.latLngToCellAddress(lat, lon, resolution);
  }

  /* Get k-ring neighboring hexes */
  public List<String> kRing(String h3Index, int k) {
    return h3.gridDisk(h3Index, k);
  }


  /* Convert H3 long to hex string (address) */
  public String h3ToAddress(long h3Index) {
    return h3.h3ToString(h3Index);
  }

  /* Convert hex string (address) back to long */
  public long addressToH3(String address) {
    return h3.stringToH3(address);
  }

}
