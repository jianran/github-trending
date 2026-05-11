package com.githubtrending.springbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.githubtrending.springbot.model.RepositoriesResponse;
import com.githubtrending.springbot.model.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubTrendingService {

    private static final Logger log = LoggerFactory.getLogger(GitHubTrendingService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${github.trending.endpoint}")
    private String trendingEndpoint;

    @Value("${github.trending.query:trending}")
    private String query;

    @Value("${github.trending.sort:stars}")
    private String sort;

    @Value("${github.trending.order:desc}")
    private String order;

    @Value("${github.trending.top:10}")
    private int topCount;

    public GitHubTrendingService(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public RepositoriesResponse fetchTrendingRepositories() {
        log.info("Fetching trending repositories with query: {}", query);

        String fullUrl = trendingEndpoint + "?q=" + query + "&sort=" + sort + "&order=" + order + "&per_page=" + topCount;

        return webClient.get()
                .uri(fullUrl)
                .header(HttpHeaders.USER_AGENT, "GitHub-Trending-Bot/1.0")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    log.debug("GitHub API response: {}", response);
                })
                .doOnError(error -> log.error("Error fetching trending repositories", error))
                .map(this::parseRepositories)
                .block();
    }

    private RepositoriesResponse parseRepositories(String jsonResponse) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            JsonNode items = jsonNode.get("items");

            List<Repository> repos = new ArrayList<>();
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    String login = item.get("owner").get("login").asText();
                    String description = item.has("description") && item.get("description").asText() != null
                            ? item.get("description").asText() : "";
                    String language = item.has("language") ? item.get("language").asText() : "";
                    String homepage = item.has("homepage") && !item.get("homepage").isNull()
                            ? item.get("homepage").asText() : "";

                    Repository repo = new Repository(
                            item.get("name").asText(),
                            item.get("full_name").asText(),
                            description,
                            item.get("html_url").asText(),
                            language,
                            item.get("stargazers_count").asInt(),
                            item.get("forks_count").asInt(),
                            login,
                            item.get("owner").get("avatar_url").asText(),
                            homepage
                    );
                    repos.add(repo);
                }
            }

            RepositoriesResponse response = new RepositoriesResponse();
            response.setTrendingRepositories(repos);
            return response;

        } catch (Exception e) {
            log.error("Error parsing GitHub API response", e);
            return new RepositoriesResponse();
        }
    }

    public String formatRepositoriesAsText(List<Repository> repositories) {
        if (repositories == null || repositories.isEmpty()) {
            return "No trending repositories found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔥 GitHub Trending Repositories - ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n\n");

        int index = 1;
        for (Repository repo : repositories) {
            sb.append(index++).append(". ").append(repo.getFormattedSummary()).append("\n\n");
        }

        return sb.toString();
    }

    public String getPromptForSummarization(List<Repository> repositories) {
        String reposText = formatRepositoriesAsText(repositories);

        return String.format("""
                You are a helpful assistant that summarizes GitHub trending repositories.
                Please provide a concise summary of the trending repositories below.
                Focus on the most interesting projects, their purposes, and why they're trending.
                Keep the summary under 500 words and make it engaging for developers.

                Trending Repositories:
                %s

                Summary:""", reposText);
    }
}
