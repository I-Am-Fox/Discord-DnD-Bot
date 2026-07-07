# YouTube Playback Plan

YouTube playback is a project requirement. The selected direction is:

```text
Discord bot process -> AudioEngine abstraction -> Lavalink v4 node -> youtube-source plugin
```

Do not implement a direct JDA/LavaPlayer-only YouTube player in the bot process.

## Why Lavalink

- Keeps Discord command/API logic separate from track extraction and streaming.
- Lets the hosted bot stay a normal Java process on PebbleHost.
- Lets the audio node be upgraded independently when YouTube changes.
- Provides a cleaner path to queueing, seeking, filters, and future crossfade support.

## Required Lavalink Setup

Use Lavalink v4 and install the `youtube-source` plugin:

```yaml
lavalink:
  plugins:
    - dependency: "dev.lavalink.youtube:youtube-plugin:1.18.1"
      snapshot: false
  server:
    sources:
      youtube: false
```

The built-in YouTube source must be disabled so the plugin handles YouTube URLs and searches.

The project includes a starter config at:

```text
deploy/lavalink/application.yml.example
```

## Bot Configuration

The bot will connect to the Lavalink node through:

```text
LAVALINK_HOST
LAVALINK_PORT
LAVALINK_PASSWORD
LAVALINK_SECURE
```

Phase 3 should implement `LavalinkAudioEngine` behind the existing `AudioEngine` interface.

## Source Input Rules

The API and Discord commands should accept:

```text
https://www.youtube.com/watch?v=...
https://youtu.be/...
ytsearch:<query>
ytmsearch:<query>
```

For normal `/music play` user input, the bot should convert plain text searches to `ytsearch:<query>` when YouTube support is enabled.

## Reliability Notes

YouTube access is inherently fragile for hosted bots. The `youtube-source` plugin supports multiple InnerTube clients, and may need OAuth or `poToken` configuration if YouTube starts challenging automated requests.

Do not store OAuth refresh tokens, visitor data, `poToken` values, or account credentials in Git. Treat all of them as secrets.

For D&D sessions, keep direct URLs, local/licensed music, and prepared playlists available as a fallback if YouTube has a temporary outage.
