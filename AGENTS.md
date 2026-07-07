# D&D Music Bot Project Guidance

This repository is for a Java 25 Discord music bot and local controller for Dungeons & Dragons session audio.

## Architecture

- Use a multi-module Gradle Kotlin DSL build.
- Keep common API contracts in `shared`.
- Keep hosted bot, REST API, state, persistence, and audio abstractions in `bot-server`.
- Keep the local controller in `desktop-controller`.
- Use Java 25 without preview features.
- Use records for immutable DTOs.
- Use constructor injection and interfaces at external boundaries.
- Do not hardcode Discord tokens, API tokens, passwords, guild IDs, hostnames, or local paths.

## Phase 1 Scope

- JSON persistence for campaigns and scenes.
- `NoopAudioEngine` to validate API/UI flow before real Discord audio.
- Authenticated Javalin REST API.
- CLI controller first; JavaFX later.
- Unit tests that do not require Discord, Lavalink, PebbleHost, or network services.
- Deployable `bot-server` fat jar through `:bot-server:shadowJar`.

## Phase 2 Scope

- JDA startup when `DISCORD_TOKEN` is configured.
- Slash command registration for `/music`, `/scene`, and `/campaign`.
- Guild-scoped command registration through optional `DISCORD_COMMAND_GUILD_ID`.
- Command handlers must stay thin and call services.
- Voice join/leave may use JDA voice connection placeholders before a real `AudioEngine` is implemented.
- Do not stream real audio until the DAVE/Lavalink decision is made and implemented behind `AudioEngine`.
- YouTube playback is required; target Lavalink v4 plus the `lavalink-devs/youtube-source` plugin.
- Avoid direct JDA/LavaPlayer-only YouTube playback in the bot process.

## Runtime Rules

- Bot server must run independently of the controller.
- Config comes from environment variables.
- API auth is required for all non-health endpoints.
- State is per guild/server.
- Real audio implementation must sit behind `AudioEngine`.
- Prefer Lavalink v4 for production audio, with `youtube-source` for YouTube URLs and searches.
- JDA audio connections require a DAVE-compatible setup before real audio playback.

## Validation

Use:

```powershell
.\gradlew.bat test
.\gradlew.bat :bot-server:shadowJar
```

Never commit real `.env` files, generated build output, or secrets.
