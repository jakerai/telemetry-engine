package com.telemetry.engine.common.geo;

import java.util.List;
import java.util.stream.Collectors;
import com.uber.h3core.H3Core;

/**
 * Service wrapper around Uber H3 library. Provides utilities to convert latitude/longitude into H3
 * cells, navigate neighboring cells (k-ring), and convert between numeric H3 indexes and their
 * string (hex) representations.
 *
 */
public class H3Service {

  private final H3Core h3;

  /**
   * Default H3 resolution used across the system.
   *
   * <p>
   * H3 resolutions define the size of each hexagonal cell on the earth's surface. Higher
   * resolutions produce smaller cells with higher spatial precision.
   * </p>
   *
   * <pre>
   * Resolution | Avg cell edge | Avg cell area
   * -----------|---------------|---------------
   * 6          | ~3.7 km       | ~36 km²
   * 7          | ~1.4 km       | ~5.1 km²
   * 8          | ~460 m        | ~0.74 km²
   * 9          | ~170 m        | ~0.10 km²
   * 10         | ~65 m         | ~0.015 km²
   * </pre>
   *
   * <p>
   * <b>Why resolution 8?</b>
   * </p>
   * <ul>
   * <li>Good balance between spatial accuracy and query performance</li>
   * <li>Works well for city-scale proximity searches (hundreds of meters)</li>
   * <li>Limits Redis key explosion compared to higher resolutions</li>
   * <li>Efficient k-ring expansion for nearby-asset discovery</li>
   * </ul>
   *
   * <p>
   * This resolution is suitable for real-time asset tracking, geo-based subscriptions, and
   * streaming use cases where exact meter-level precision is not required.
   * </p>
   */
  private static final int RESOLUTION = 8;
  private static final double H3_RES8_DIAMETER_METERS = 920.0; // res 8 hex diameter

  public H3Service() {
    try {
      this.h3 = H3Core.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to init H3", e);
    }
  }


  /**
   * Converts latitude and longitude into a numeric H3 cell index.
   *
   * @param lat latitude
   * @param lon longitude
   * @param resolution H3 resolution level
   * @return H3 cell index as {@code long}
   */
  public long getH3CellIndex(double lat, double lon, int resolution) {
    return h3.latLngToCell(lat, lon, resolution);
  }

  /**
   * Converts latitude and longitude into an H3 cell address (hex string) using the default
   * resolution.
   *
   * @param lat latitude
   * @param lon longitude
   * @return H3 cell address as hex string (e.g. {@code 8928308280fffff})
   */
  public String getH3CellAddress(double lat, double lon) {
    long h3Index = h3.latLngToCell(lat, lon, RESOLUTION);
    return toH3Address(h3Index);
  }

  /**
   * Returns neighboring H3 cell indexes (k-ring) around a center cell. Use for performing
   * geo-expansion, proximity queries, or spatial filtering using numeric H3 indexes.
   *
   * @param centerCellIndex center H3 cell index
   * @param ringSize number of rings around the center
   * @return List of neighboring H3 cell indexes
   */
  public List<Long> getKRingIndexes(long centerCellIndex, int radiusMeters) {
    int ringSize = (int) Math.ceil(radiusMeters / H3_RES8_DIAMETER_METERS);
    return h3.gridDisk(centerCellIndex, ringSize);
  }

  /**
   * Returns neighboring H3 cell addresses (k-ring) around a center cell.
   *
   * @param centerCellAddress center H3 cell address (hex string)
   * @param ringSize number of rings around the center
   * @return List of H3 cell addresses as strings
   */
  public List<String> getKRingAddresses(String centerCellAddress, int radiusMeters) {
    int ringSize = (int) Math.ceil(radiusMeters / H3_RES8_DIAMETER_METERS);
    long center = h3.stringToH3(centerCellAddress);
    List<Long> neighbors = h3.gridDisk(center, ringSize);
    return neighbors.stream().map(this::toH3Address).collect(Collectors.toList());
  }


  /**
   * Converts a numeric H3 cell index into its hex string representation.
   *
   * @param h3Index numeric H3 index
   * @return H3 cell address as hex string
   */
  public String toH3Address(long h3Index) {
    return h3.h3ToString(h3Index);
  }


  /**
   * Converts an H3 cell address (hex string) back into its numeric index. Use for performing
   * calculations, k-ring queries, or any H3 API that requires a {@code long} index.
   *
   * @param h3Address H3 cell address as hex string
   * @return H3 cell index as {@code long}
   */
  public long toH3Index(String h3Address) {
    return h3.stringToH3(h3Address);
  }

}
