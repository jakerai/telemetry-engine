package com.telemetry.engine.common.geo;

import java.util.List;
import java.util.stream.Collectors;
import com.uber.h3core.H3Core;

public class H3Service {

  private final H3Core h3;

  private static final int RESOLUTION = 8;
  
  public H3Service() {
    try {
      this.h3 = H3Core.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to init H3", e);
    }
  }

  /* Convert lat/lon to H3 index */
  public long toH3CellAddress(double lat, double lon, int resolution) {
    return h3.latLngToCell(lat, lon, resolution);
  }

  public String toH3(double lat, double lon) {
    long h3Index =toH3CellAddress(lat, lon, RESOLUTION); // fixed resolution 8
    return h3ToAddress(h3Index);
  }
  
  /* Get k-ring neighboring hexes */
  public List<Long> kRing(long centerH3, int k) {
    return h3.gridDisk(centerH3, k);
  }

  /**
   * Get H3 neighbors (k-ring) as strings for Redis sets.
   *
   * @param h3Address center cell as string
   * @param ringSize number of rings around center
   * @return List of H3 addresses as strings
   */
  public List<String> kRing(String h3Address, int ringSize) {
    long center = addressToH3(h3Address);
    List<Long> neighbors = kRing(center, ringSize);
    return neighbors.stream().map(this::h3ToAddress).collect(Collectors.toList());
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
