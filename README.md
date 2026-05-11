# GitHub Trending Bot

A Spring Boot application that fetches GitHub trending repositories, summarizes them using a local llamacpp server, and sends the summary via Discord DMs.

## Features

- **GitHub Trending Fetcher**: Automatically fetches trending repositories from GitHub
- **AI Summarization**: Uses llamacpp server API to generate intelligent summaries
- **Discord Bot**: Sends summarized reports via Discord DMs on a scheduled basis
- **Configurable Schedule**: Customizable cron expression for summary generation

## Prerequisites

- Java 17+
- Maven 3.6+
- Discord Bot Token
- llamacpp server running locally (or remote endpoint)

## Setup

### 1. Discord Bot Setup

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application
3. Add a bot user and get the token
4. Enable `MESSAGE_CONTENT` intent
5. Invite the bot to your server:
   ```
   https://discord.com/api/oauth2/authorize?client_id=YOUR_CLIENT_ID&permissions=8&scope=bot
   ```

### 2. llamacpp Server Setup

Start a llamacpp server:
```bash
# Using llama.cpp
./server -m models/your-model.gguf -c 2048 --port 8080
```

Or use an Ollama instance:
```bash
ollama serve
```

### 3. Configuration

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Required variables:
- `DISCORD_BOT_TOKEN`: Your Discord bot token
- `DISCORD_DM_USER_IDS`: Comma-separated list of user IDs to send DMs to
- `LLAMACPP_SERVER_URL`: URL of your llamacpp server

Optional variables:
- `GITHUB_TRENDING_PERIOD`: `daily`, `weekly`, or `monthly`
- `GITHUB_TRENDING_TOP`: Number of repos to fetch (default: 10)
- `SUMMARY_SCHEDULE_CRON`: Cron expression for scheduling (default: daily at 9 AM)

### 4. Build and Run

```bash
# Using Maven
mvn clean install
mvn spring-boot:run

# Using Gradle (alternative)
./gradlew bootRun
```

## API Endpoints

### Scheduled Task
The application runs a scheduled task that:
1. Fetches trending GitHub repositories
2. Generates a summary using llamacpp
3. Sends the summary via Discord DM to configured users

## Project Structure

```
spring-bot/
├── src/main/java/com/githubtrending/springbot/
│   ├── SpringBotApplication.java
│   ├── config/
│   │   ├── ApplicationConfig.java
│   │   └── WebClientConfig.java
│   ├── model/
│   │   ├── Repository.java
│   │   └── RepositoriesResponse.java
│   └── service/
│       ├── GitHubTrendingService.java
│       ├── LlamaCppService.java
│       ├── DiscordBotService.java
│       └── TrendingSummaryScheduler.java
└── src/main/resources/
    └── application.properties
```

## Customization

### Change Schedule
Modify `SUMMARY_SCHEDULE_CRON` in `.env`:
- Every hour: `0 * * * *`
- Every day at midnight: `0 0 0 * * *`
- Every 6 hours: `0 */6 * * *`

### Change Model Parameters
Edit `LlamaCppService.java` to adjust:
- `temperature`: Creativity (0.0 - 1.0)
- `top_p`: Nucleus sampling
- `top_k`: Vocabulary sampling
- `n_predict`: Max tokens to generate

## Troubleshooting

### Discord DMs Not Sending
- Ensure user IDs are correct (enable Developer Mode in Discord to get IDs)
- Verify the bot has permission to send DMs
- Check that users have added the bot as a friend

### llamacpp Connection Issues
- Verify the server is running and accessible
- Check firewall settings
- Test with curl: `curl -X POST http://localhost:8080/api/chat -d '{"prompt":"test"}'`

### No Repositories Found
- Check the GitHub Trending API availability
- Verify network connectivity

## License

MIT License
