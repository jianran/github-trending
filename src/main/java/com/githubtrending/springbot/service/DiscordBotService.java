package com.githubtrending.springbot.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "discord.bot.token", havingValue = "", matchIfMissing = false)
public class DiscordBotService {

    private static final Logger log = LoggerFactory.getLogger(DiscordBotService.class);

    @Value("${discord.bot.token}")
    private String botToken;

    @Value("${discord.bot.dm.user.ids}")
    private String userIdsStr;

    private JDA jda;
    private List<Long> targetUserIds;

    @PostConstruct
    public void init() {
        if (botToken == null || botToken.isEmpty()) {
            log.warn("Discord bot token not configured. Bot will not start.");
            return;
        }

        log.info("Initializing Discord bot...");

        targetUserIds = Arrays.stream(userIdsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        if (targetUserIds.isEmpty()) {
            log.warn("No target user IDs configured for DMs");
        }

        CountDownLatch latch = new CountDownLatch(1);
        DiscordEventListener listener = new DiscordEventListener(latch);

        this.jda = JDABuilder.createDefault(botToken)
                .addEventListeners(listener)
                .build();

        try {
            latch.await(10, TimeUnit.SECONDS);
            log.info("Discord bot initialized successfully");
        } catch (InterruptedException e) {
            log.error("Failed to wait for Discord bot to be ready", e);
            Thread.currentThread().interrupt();
        }
    }

    public void sendSummaryToUsers(String summary) {
        if (jda == null) {
            log.warn("Discord bot not initialized, cannot send DM");
            return;
        }

        log.info("Sending summary to {} users", targetUserIds.size());

        for (Long userId : targetUserIds) {
            try {
                User user = jda.retrieveUserById(userId).complete();
                if (user == null) {
                    log.warn("User {} not found", userId);
                    continue;
                }

                String truncatedSummary = truncateSummary(summary, 1500);

                user.openPrivateChannel()
                        .flatMap(channel -> channel.sendMessage(truncatedSummary))
                        .queue(
                                message -> log.info("Sent DM to user {} ({})", user.getName(), user.getId()),
                                error -> log.error("Failed to send DM to user {}", user.getName(), error)
                        );

            } catch (Exception e) {
                log.error("Error sending DM to user {}", userId, e);
            }
        }
    }

    private String truncateSummary(String summary, int maxLength) {
        if (summary.length() <= maxLength) {
            return summary;
        }

        String truncated = summary.substring(0, maxLength);
        int lastPeriod = truncated.lastIndexOf(".");
        if (lastPeriod > maxLength * 0.7) {
            truncated = truncated.substring(0, lastPeriod + 1);
        }

        return truncated + "\n\n*(Summary truncated due to length)*";
    }

    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            log.info("Shutting down Discord bot...");
            jda.shutdown();
        }
    }

    private class DiscordEventListener extends ListenerAdapter {
        private final CountDownLatch latch;

        public DiscordEventListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onReady(ReadyEvent event) {
            log.info("Discord bot is ready! Logged in as: {}", event.getJDA().getSelfUser().getName());
            latch.countDown();
        }
    }
}
