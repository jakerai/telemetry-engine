package com.telemetry.engine.consumer.metrics;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DailyCounter {

  private final AtomicInteger totalProcessed = new AtomicInteger(0);
  private volatile LocalDate currentDay = LocalDate.now();

  /**
   * Increment the counter by batchCount. Resets automatically if day changes.
   */
  public void increment(int batchCount) {
      LocalDate today = LocalDate.now();
      // Check if day changed
      if (!today.equals(currentDay)) {
          totalProcessed.set(0);
          currentDay = today;
          log.info("New day started. totalProcessed reset to 0");
      }

      int total = totalProcessed.addAndGet(batchCount);
      log.info("Processed {} items in this batch. Total processed today: {}", batchCount, total);
  }

  public int getTotal() {
      return totalProcessed.get();
  }
  
}
