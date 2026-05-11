package com.githubtrending.springbot.service;

import com.githubtrending.springbot.model.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrendingSummaryScheduler {

    private static final Logger log = LoggerFactory.getLogger(TrendingSummaryScheduler.class);
    private final GitHubTrendingService gitHubTrendingService;
    private final LlamaCppService llamaCppService;
    private final MockAIService mockAIService;
    private final DiscordBotService discordBotService;

    public TrendingSummaryScheduler(GitHubTrendingService gitHubTrendingService,
                                     LlamaCppService llamaCppService,
                                     MockAIService mockAIService,
                                     DiscordBotService discordBotService) {
        this.gitHubTrendingService = gitHubTrendingService;
        this.llamaCppService = llamaCppService;
        this.mockAIService = mockAIService;
        this.discordBotService = discordBotService;
    }

    @Scheduled(cron = "${summary.schedule.cron:0 0 9 * * *}")
    public void generateAndSendSummary() {
        log.info("Starting scheduled trending summary generation...");

        try {
            // Step 1: Fetch trending repositories
            var reposResponse = gitHubTrendingService.fetchTrendingRepositories();
            List<Repository> repositories = reposResponse.getTrendingRepositories();

            if (repositories == null || repositories.isEmpty()) {
                log.warn("No repositories found to summarize");
                return;
            }

            // Step 2: Generate prompt for summarization
            String prompt = gitHubTrendingService.getPromptForSummarization(repositories);

            // Step 3: Generate summary using llamacpp (fallback to mock if unavailable)
            String summary = generateSummaryOrDefault(prompt);

            // Step 4: Send summary via Discord
            discordBotService.sendSummaryToUsers(summary);

            log.info("Successfully generated and sent trending summary");

        } catch (Exception e) {
            log.error("Error during scheduled summary generation", e);
        }
    }

    public void triggerSummary() {
        log.info("Manual trigger for trending summary generation received");

        try {
            // Step 1: Fetch trending repositories
            var reposResponse = gitHubTrendingService.fetchTrendingRepositories();
            List<Repository> repositories = reposResponse.getTrendingRepositories();

            if (repositories == null || repositories.isEmpty()) {
                log.warn("No repositories found to summarize");
                return;
            }

            // Step 2: Generate prompt for summarization
            String prompt = gitHubTrendingService.getPromptForSummarization(repositories);

            // Step 3: Generate summary using llamacpp (fallback to mock if unavailable)
            String summary = generateSummaryOrDefault(prompt);

            // Step 4: Send summary via Discord
            discordBotService.sendSummaryToUsers(summary);

            log.info("Successfully generated and sent trending summary");

        } catch (Exception e) {
            log.error("Error during manual summary generation", e);
        }
    }

    private String generateSummaryOrDefault(String prompt) {
        try {
            return llamaCppService.generateSummary(prompt);
        } catch (Exception e) {
            log.warn("AI service unavailable, using mock summary: {}", e.getMessage());
            return mockAIService.generateSummary(prompt);
        }
    }
}
