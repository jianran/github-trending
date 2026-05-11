package com.githubtrending.springbot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RepositoriesResponse {
    @JsonProperty("trending_repositories")
    private List<Repository> trendingRepositories;

    public List<Repository> getTrendingRepositories() {
        return trendingRepositories;
    }

    public void setTrendingRepositories(List<Repository> trendingRepositories) {
        this.trendingRepositories = trendingRepositories;
    }
}
