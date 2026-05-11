package com.githubtrending.springbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockAIService {

    private static final Logger log = LoggerFactory.getLogger(MockAIService.class);

    public String generateSummary(String prompt) {
        log.info("Generating mock summary (AI server unavailable)...");

        // Extract repository count from prompt
        String summary = "GitHub Trending Summary\n";
        summary += "========================\n\n";
        summary += "Currently unable to connect to the AI summarization service. Here's a quick overview of the top repositories:\n\n";
        summary += "1. build-your-own-x - Master programming by recreating your favorite technologies from scratch.\n";
        summary += "2. A great open-source project for developers\n";
        summary += "3. Another popular GitHub repository\n";
        summary += "4. A useful tool for software engineering\n";
        summary += "5. Community-driven project with high engagement\n\n";
        summary += "Note: To enable AI-powered summaries, ensure the llamacpp server is running at the configured endpoint.";

        return summary;
    }
}
