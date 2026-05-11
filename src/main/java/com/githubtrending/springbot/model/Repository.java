package com.githubtrending.springbot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Repository {
    private String name;
    private String fullName;
    private String description;
    private String htmlUrl;
    private String language;
    private Integer stars;
    private Integer forks;
    private String owner;
    private String avatarUrl;
    private String homepage;

    public String getFormattedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(name).append("\n");
        sb.append("👤 ").append(owner).append("\n");
        if (description != null && !description.isEmpty()) {
            sb.append("📝 ").append(description).append("\n");
        }
        sb.append("⭐ ").append(stars).append(" stars | ")
          .append("🍴 ").append(forks).append(" forks");
        if (language != null && !language.isEmpty()) {
            sb.append(" | 💻 ").append(language);
        }
        if (homepage != null && !homepage.isEmpty()) {
            sb.append("\n🌐 ").append(homepage);
        }
        sb.append("\n🔗 ").append(htmlUrl);
        return sb.toString();
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Repository(String name, String fullName, String description, String htmlUrl, String language,
                      Integer stars, Integer forks, String owner, String avatarUrl, String homepage) {
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.htmlUrl = htmlUrl;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.owner = owner;
        this.avatarUrl = avatarUrl;
        this.homepage = homepage;
    }
}
