package com.githubtrending.springbot.controller;

import com.githubtrending.springbot.service.TrendingSummaryScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class TriggerController {

    private static final Logger log = LoggerFactory.getLogger(TriggerController.class);
    private final TrendingSummaryScheduler scheduler;

    public TriggerController(TrendingSummaryScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/trigger-summary")
    public Map<String, String> triggerSummary() {
        log.info("Manual trigger received — running in background");

        // Run async to avoid blocking the reactor thread
        CompletableFuture.runAsync(() -> {
            try {
                scheduler.triggerSummary();
            } catch (Exception e) {
                log.error("Async trigger failed", e);
            }
        });

        return Map.of("status", "success", "message", "Summary generation triggered in background");
    }
}
