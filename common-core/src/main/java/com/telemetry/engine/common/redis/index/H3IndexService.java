package com.telemetry.engine.common.redis.index;

import java.util.List;
import java.util.stream.Collectors;
import com.telemetry.engine.common.geo.H3Service;

public class H3IndexService {

  private static final int RESOLUTION = 8;
  private final H3Service h3Service;

  /**
   * Constructor.
   * 
   * @param h3Service shared singleton H3Service
   */
  public H3IndexService(H3Service h3Service) {
    this.h3Service = h3Service;
  }

  /**
   * Convert lat/lon to H3 string for Redis keys. Internally uses H3Service to get long, then
   * converts to string.
   *
   * @param lat latitude
   * @param lon longitude
   * @return H3 address string
   */
  public String toH3(double lat, double lon) {
    long h3Index = h3Service.getH3CellIndex(lat, lon, RESOLUTION); // fixed resolution 8
    return h3Service.toH3Address(h3Index);
  }

  /**
   * Get H3 neighbors (k-ring) as strings for Redis sets.
   *
   * @param h3Address center cell as string
   * @param ringSize number of rings around center
   * @return List of H3 addresses as strings
   */
  public List<String> kRing(String h3Address, int ringSize) {
    long center = h3Service.toH3Index(h3Address);
    List<Long> neighbors = h3Service.getKRingIndexes(center, ringSize);
    return neighbors.stream().map(h3Service::toH3Address).collect(Collectors.toList());
  }

  /**
   * Optional helper: convert lat/lon to long H3 index. Useful for internal computations.
   */
  public long toH3Long(double lat, double lon) {
    return h3Service.getH3CellIndex(lat, lon, RESOLUTION);
  }

  /**
   * Optional helper: convert H3 string to long.
   */
  public long stringToLong(String h3Address) {
    return h3Service.toH3Index(h3Address);
  }

  /**
   * Optional helper: convert H3 long to string.
   */
  public String longToString(long h3Index) {
    return h3Service.toH3Address(h3Index);
  }

}
