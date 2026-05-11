package com.githubtrending.springbot.service;

import com.githubtrending.springbot.model.Repository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrapes GitHub's official trending page (https://github.com/trending)
 * to get repos ranked by star increase "today" — something the Search API can't do.
 */
@Service
public class GitHubTrendingScraper {

    private static final Logger log = LoggerFactory.getLogger(GitHubTrendingScraper.class);
    private static final String TRENDING_URL = "https://github.com/trending?since=daily";

    public List<Repository> fetchTrendingToday() {
        log.info("Scraping GitHub trending page for today's fastest-rising repos...");

        try {
            Document doc = Jsoup.connect(TRENDING_URL)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .timeout(10_000)
                    .get();

            List<Repository> repos = new ArrayList<>();

            for (Element article : doc.select("article.Box-row")) {
                try {
                    // Full repo name: owner/name
                    Element h2 = article.selectFirst("h2");
                    if (h2 == null) continue;
                    Element link = h2.selectFirst("a");
                    if (link == null) continue;

                    String href = link.attr("href").replaceFirst("^/", "");
                    String[] parts = href.split("/");
                    if (parts.length < 2) continue;
                    String owner = parts[0];
                    String name = parts[1];
                    String fullName = owner + "/" + name;

                    // Description
                    Element descEl = article.selectFirst("p.col-9");
                    String description = descEl != null ? descEl.text().trim() : "";

                    // Language
                    Element langEl = article.selectFirst("[itemprop=programmingLanguage]");
                    String language = langEl != null ? langEl.text().trim() : "";

                    // Total stars
                    Element starsEl = article.selectFirst("a[href$=/stargazers]");
                    String starsText = starsEl != null ? starsEl.text().replaceAll(",", "").trim() : "0";
                    int totalStars = parseIntSafe(starsText);

                    // Forks
                    Element forksEl = article.selectFirst("a[href$=/forks]");
                    String forksText = forksEl != null ? forksEl.text().replaceAll(",", "").trim() : "0";
                    int forks = parseIntSafe(forksText);

                    // Stars gained today
                    String todayStarsText = "0";
                    for (Element span : article.select("span.d-inline-block")) {
                        String text = span.text().trim();
                        if (text.matches("[\\d,]+\\s*stars\\s*today")) {
                            todayStarsText = text.replaceAll("[^\\d]", "");
                            break;
                        }
                    }
                    int starsToday = parseIntSafe(todayStarsText);

                    // Build repo object — use totalStars as base, note today's gain in description
                    String enhancedDesc = description;
                    if (starsToday > 0) {
                        enhancedDesc = description + (description.isEmpty() ? "" : " · ") +
                                "⭐ +" + starsToday + " today";
                    }

                    Repository repo = new Repository(
                            name,
                            fullName,
                            enhancedDesc,
                            "https://github.com/" + fullName,
                            language,
                            totalStars,
                            forks,
                            owner,
                            "",  // avatar not available from trending page
                            ""   // homepage not available
                    );
                    repos.add(repo);

                } catch (Exception e) {
                    log.warn("Skipped a trending repo entry due to parse error", e);
                }
            }

            log.info("Scraped {} trending repos from GitHub trending page", repos.size());
            return repos;

        } catch (Exception e) {
            log.error("Failed to scrape GitHub trending page", e);
            return List.of();
        }
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
