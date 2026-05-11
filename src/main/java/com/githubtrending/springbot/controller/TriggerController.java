package com.githubtrending.springbot.controller;

import com.githubtrending.springbot.service.TrendingSummaryScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TriggerController {

    private static final Logger log = LoggerFactory.getLogger(TriggerController.class);
    private final TrendingSummaryScheduler scheduler;

    public TriggerController(TrendingSummaryScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/trigger-summary")
    public Map<String, String> triggerSummary(@RequestBody(required = false) Map<String, String> request) {
        log.info("Manual trigger for trending summary generation received");

        try {
            scheduler.triggerSummary();
            log.info("Successfully triggered trending summary");
            return Map.of("status", "success", "message", "Summary sent via Discord");
        } catch (Exception e) {
            log.error("Error during manual summary generation", e);
            return Map.of("status", "error", "message", e.getMessage());
        }
    }
}
