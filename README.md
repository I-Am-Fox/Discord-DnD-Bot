# D&D Music Bot

Java 25 Discord music bot for D&D scene-based audio, plus a local CLI controller.

The project is intentionally built around scenes instead of only tracks. Phase 1 uses a `NoopAudioEngine` so the API, persistence, controller, and tests can be developed before wiring real Discord audio.

## Requirements

- Java 25.
- Gradle Wrapper from this repository.
- A Discord bot token for the later Discord integration phase.
- A strong `BOT_API_TOKEN` for controller-to-server calls.

## Modules

- `shared`: DTOs, enums, API contracts.
- `bot-server`: hosted bot process, Javalin API, JSON persistence, scene/playback services.
- `desktop-controller`: local CLI controller that calls the hosted API.

## Environment Variables

Copy `.env.example` for local reference. The application reads real values from environment variables.

Required for Phase 1:

```text
BOT_API_TOKEN
```

Common optional values:

```text
BOT_API_HOST=127.0.0.1
BOT_API_PORT=8080
DATA_DIR=./data
LOG_LEVEL=INFO
```

Reserved for later audio/Discord phases:

```text
DISCORD_TOKEN
LAVALINK_HOST
LAVALINK_PORT
LAVALINK_PASSWORD
```

## Run Bot Server

```powershell
$env:BOT_API_TOKEN = "replace-with-a-long-random-token"
.\gradlew.bat :bot-server:run
```

Health check:

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/health
```

## Run CLI Controller

```powershell
$env:BOT_SERVER_URL = "http://127.0.0.1:8080"
$env:BOT_API_TOKEN = "replace-with-a-long-random-token"
.\gradlew.bat :desktop-controller:run --args="health"
.\gradlew.bat :desktop-controller:run --args="campaigns"
```

Useful commands:

```text
health
campaigns
create-campaign <name> <guildId> [defaultVoiceChannelId]
scenes <campaignId>
create-scene <campaignId> <name> <type> <playlistId>
switch-scene <guildId> <campaignId> <sceneName> [transitionMode]
state <guildId>
volume <guildId> <0-150>
```

## API Auth

All endpoints except `GET /api/health` require either:

```text
Authorization: Bearer <BOT_API_TOKEN>
```

or:

```text
X-Api-Token: <BOT_API_TOKEN>
```

## Build And Test

```powershell
.\gradlew.bat test
.\gradlew.bat :bot-server:shadowJar
```

The deployable server jar is:

```text
bot-server/build/libs/dnd-music-bot-server-all.jar
```

## PebbleHost Deployment

Build the fat jar:

```powershell
.\gradlew.bat :bot-server:shadowJar
```

Upload `bot-server/build/libs/dnd-music-bot-server-all.jar` and run it with:

```bash
java -jar dnd-music-bot-server-all.jar
```

Configure environment variables in the host panel or startup script. The bot server does not require the local controller to be running.

## Add Scenes

Use the API or CLI controller during Phase 1. Data is stored as JSON under `DATA_DIR`.

Example starter data is in `examples/starter-campaign.json`.

## Known Limitations

- Phase 1 does not connect to Discord voice.
- Phase 1 does not play real audio.
- Crossfade and stingers are represented in the state model, but real audio behavior requires a production `AudioEngine`.
- YouTube support is intentionally not assumed.

## Troubleshooting

- `BOT_API_TOKEN is required`: set a non-blank API token.
- Unauthorized API response: make sure controller and server use the same token.
- Port already in use: change `BOT_API_PORT`.
- Data write failures: check `DATA_DIR` permissions.
