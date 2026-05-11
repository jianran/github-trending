package com.githubtrending.springbot.model;

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
}
