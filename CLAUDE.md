# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Capacitor plugin that bridges Android Health Connect to web/hybrid apps. It is Android-only — there is no iOS implementation. Published as `@pushappy/capacitor-health-connect` to a private Google Artifact Registry, forked from `ubie-oss/capacitor-health-connect`.

## Commands

```bash
npm run build          # compile TS + generate docs + bundle with Rollup
npm run verify         # full check: Android Gradle build+test AND web build
npm run verify:android # cd android && ./gradlew clean build test
npm run lint           # ESLint + Prettier check
npm run fmt            # auto-fix ESLint + Prettier
npm run docgen         # regenerate README API docs from JSDoc in src/definitions.ts
npm run ar-login       # authenticate to Google Artifact Registry (needed before publish)
npm run pack-and-publish # build + auth + npm publish
```

## Architecture

### TypeScript layer (`src/`)

- `src/definitions.ts` — single source of truth for the entire public API: the `HealthConnectPlugin` interface plus all types. JSDoc comments here drive the auto-generated README API section via `@capacitor/docgen` — **do not edit the `<docgen-api>` block in README.md directly**.
- `src/index.ts` — registers the plugin with Capacitor (`registerPlugin('HealthConnect', {})`), re-exports everything from `definitions.ts`.

Build output in `dist/`:
- `dist/esm/` — ESM output from `tsc` (for bundlers)
- `dist/plugin.js` — IIFE bundle via Rollup (for script tags)
- `dist/plugin.cjs.js` — CJS bundle via Rollup

### Android layer (`android/`)

- `HealthConnectPlugin.kt` — maps each `@PluginMethod` to Android Health Connect SDK calls. Uses `lifecycleScope.launch` for coroutines. The `HealthConnectClient` is lazily initialized on first successful availability check.
- `Serializer.kt` — handles JSON serialization/deserialization between JS types and Health Connect SDK types.
- Depends on `androidx.health.connect:connect-client:1.1.0-beta01`, minSdk 26, Kotlin jvmTarget 21.

### Publishing

Package publishes to a private npm repository. Run `npm run ar-login` first to refresh credentials, then `npm run pack-and-publish`.