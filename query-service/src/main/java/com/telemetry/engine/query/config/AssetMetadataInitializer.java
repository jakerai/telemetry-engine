package com.telemetry.engine.query.config;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import com.telemetry.engine.query.entity.AssetCategory;
import com.telemetry.engine.query.entity.AssetType;
import com.telemetry.engine.query.persistence.AssetCategoryPersistence;
import com.telemetry.engine.query.persistence.AssetTypePersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class AssetMetadataInitializer implements CommandLineRunner {

  private final AssetMetadataConfig assetMetadataConfig;
  private final AssetCategoryPersistence assetCategoryPersistence;
  private final AssetTypePersistence assetTypePersistence;
  private final TransactionalOperator operator;

  private static final int DB_CONCURRENCY_LIMIT = 5;

  @Override
  public void run(String... args) {
    log.info("Starting asset metadata initialization...");

    /*
     * The app should not accept traffic until the asset metadata is validated and present hence we
     * block here intentionally.
     */
    initializeMetadata()
        .doOnError(e -> log
            .error("Metadata initialization failed. System may be in an inconsistent state.", e))
        .block();
  }

  public Mono<Void> initializeMetadata() {
    return loadCategories().then(loadAssetTypes())
        .doOnSuccess(v -> log.info("Asset metadata initialization completed successfully."))
        .as(operator::transactional)
        .then();
  }

  /** Load categories into DB if not already present */
  private Mono<Void> loadCategories() {
    List<AssetMetadataConfig.Category> categories =
        Optional.ofNullable(assetMetadataConfig.getCategories()).orElse(Collections.emptyList());

    return Flux.fromIterable(categories)
        .filterWhen(
            cfg -> assetCategoryPersistence.existsByName(cfg.getName()).map(exists -> !exists))
        .flatMap(cfg -> {
          log.info("Inserting category: {}", cfg.getName());
          AssetCategory entity =
              AssetCategory.builder().name(cfg.getName()).description(cfg.getDescription()).build();
          return assetCategoryPersistence.save(entity);
        }, DB_CONCURRENCY_LIMIT).then();
  }

  /** Load asset types into DB if not already present */
  private Mono<Void> loadAssetTypes() {
    List<AssetMetadataConfig.Type> types =
        Optional.ofNullable(assetMetadataConfig.getTypes()).orElse(Collections.emptyList());
    log.info("Number of asset types found in yaml file={}", types.size());
    return Flux.fromIterable(types)
        .flatMap(cfg -> assetTypePersistence.existsByName(cfg.getName()).flatMap(exists -> {
          if (exists) {
            log.debug("Asset type {} already exists, skipping.", cfg.getName());
            return Mono.empty();
          }

          /* Only search for Category if we actually need to insert the Type */
          return assetCategoryPersistence.findByName(cfg.getCategory())
              .switchIfEmpty(Mono
                  .error(new IllegalStateException("Category missing in DB: " + cfg.getCategory())))
              .flatMap(category -> {
                log.info("Inserting asset type: {} (category_id={})", cfg.getName(),
                    category.getId());
                AssetType entity = AssetType.builder().name(cfg.getName())
                    .categoryId(category.getId()).description(cfg.getDescription()).build();
                return assetTypePersistence.save(entity);
              });
        }), DB_CONCURRENCY_LIMIT).then();
  }

}
