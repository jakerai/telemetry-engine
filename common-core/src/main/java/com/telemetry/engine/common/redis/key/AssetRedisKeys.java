package com.telemetry.engine.common.redis.key;

/**
 * Redis keys related to assets.
 *
 * <p>
 * All methods return fully-qualified Redis keys as {@link String}. These keys are used for asset
 * state storage, geo-indexing via H3, and Pub/Sub or Stream-based subscriptions.
 * </p>
 * 
 * @author Vishal Rai 
 */
public class AssetRedisKeys {

  private static final String ASSET_STATE = "asset:state:";
  private static final String H3_TYPE = "h3:type:";
  private static final String STREAM_TYPE = "stream:type:";
  private static final String STREAM_ASSET = "stream:asset:";

  private AssetRedisKeys() {}

  /**
   * Redis key for storing the latest state of a single asset.
   *
   * <p>
   * <b>Use when:</b> Saving or fetching the current state of an asset (location, speed, heading,
   * etc.).
   * </p>
   *
   * <p>
   * <b>Returns:</b> A Redis key in the form {@code asset:state:{assetId}}
   * </p>
   */
  public static String keyForAssetState(long assetId) {
    return ASSET_STATE + assetId;
  }

  /**
   * Redis key for indexing assets by asset type and H3 cell.
   *
   * <p>
   * <b>Use when:</b> Performing spatial queries such as "find all assets of this type within an H3
   * cell or k-ring".
   * </p>
   *
   * <p>
   * <b>Returns:</b> A Redis key in the form {@code h3:type:{assetTypeId}:{h3Cell}}
   * </p>
   */
  public static String keyForH3TypeCell(long assetTypeId, String h3) {
    return H3_TYPE + assetTypeId + ":" + h3;
  }

  /**
   * Redis stream or Pub/Sub key for assets grouped by type and H3 cell.
   *
   * <p>
   * <b>Use when:</b> Subscribing to real-time updates for all assets of a given type within an H3
   * cell.
   * </p>
   *
   * <p>
   * <b>Returns:</b> A Redis key in the form {@code stream:type:{assetTypeId}:{h3Cell}}
   * </p>
   */
  public static String keyForStreamTypeCell(long assetTypeId, String h3) {
    return STREAM_TYPE + assetTypeId + ":" + h3;
  }

  /**
   * Redis stream or Pub/Sub key for a single asset.
   *
   * <p>
   * <b>Use when:</b> Subscribing to real-time updates for one specific asset.
   * </p>
   *
   * <p>
   * <b>Returns:</b> A Redis key in the form {@code stream:asset:{assetId}}
   * </p>
   */
  public static String keyForStreamAsset(long assetId) {
    return STREAM_ASSET + assetId;
  }

}
