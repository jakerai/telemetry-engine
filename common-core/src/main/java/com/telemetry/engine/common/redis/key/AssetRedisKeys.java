package com.telemetry.engine.common.redis.key;

public class AssetRedisKeys {

  private static final String ASSET_STATE = "asset:state:";
  private static final String H3_TYPE = "h3:type:";
  private static final String STREAM_TYPE = "stream:type:";
  private static final String STREAM_ASSET = "stream:asset:";

  private AssetRedisKeys() {}

  public static String assetState(long assetId) {
    return ASSET_STATE + assetId;
  }

  public static String h3TypeCell(long assetTypeId, String h3) {
    return H3_TYPE + assetTypeId + ":" + h3;
  }

  public static String streamTypeCell(long assetTypeId, String h3) {
    return STREAM_TYPE + assetTypeId + ":" + h3;
  }

  public static String streamAsset(long assetId) {
    return STREAM_ASSET + assetId;
  }

}
