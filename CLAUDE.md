# CLAUDE.md

Android app (Kotlin, Jetpack Compose, Material 3) for splitting group expenses.
Source of truth for screens and flows: `diagrams/Lof-fi.png` and `diagrams/UX navigation model.png`.
Screen numbers (01–28) in code comments refer to that model.

## Commands

```bash
./gradlew assembleDebug        # build
./gradlew installDebug         # build + install on running emulator/device
./gradlew test                 # unit tests
./gradlew lint                 # Android lint
```
Use Android Studio's bundled JDK (`JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"`) when running from a terminal.

## Architecture

```
MainActivity → AppNavHost (Scaffold + BottomBar + NavHost)
                 ├─ authGraph   01–03, no bottom bar
                 └─ mainGraph   04–28, tabs: Notifications / Groups / Profile
```

| Package | Responsibility |
|---|---|
| `navigation/` | `Routes.kt` (all routes + builders), graphs, nav helpers. **Only place that calls `navigate`/`popBackStack`.** |
| `data/` | Models, `AppViewModel` (single state holder, currently an in-memory fake backend), formatting helpers. |
| `ui/<feature>/` | One file per screen. Screens are `@Composable` functions taking `vm` + callbacks. |
| `ui/components/` | Reusable UI (`AppScreen`, `BottomBar`, `OfflineBanner`, `OverlaySheet`, buttons, rows, fields). |
| `ui/theme/` | Colors, typography. |

## Conventions

- **Navigation:** screens expose callbacks (`onSaved`, `onBack`); the graph decides the destination. "Return → NN" in the model means `popBackStack`/`backToGroup`, never pushing a duplicate screen.
- **Overlays** (10, 14–17) are `OverlaySheet` bottom sheets controlled by local state, not routes.
- **Screens** use `AppScreen` for layout and pass `offline = vm.offlineState(...)` on screens listed in the model's offline section.
- **State:** shared data lives in `AppViewModel`; transient UI state (form fields, open sheets) uses `remember`/`rememberSaveable` in the screen.
- **Reuse before adding:** check `ui/components/` first; add a component when a pattern appears on 2+ screens.
- **Scope:** don't add screens, routes or features that aren't in the navigation model without the group's approval.
- **Style:** official Kotlin style, trailing commas, no wildcard imports, no hard-coded colors outside `theme/` (except the offline banner's amber).
- **Dependencies:** declare in `gradle/libs.versions.toml`; keep AndroidX libraries on compatible versions.
- **Git:** small focused commits, imperative subject line (`Add expense receipt overlay`); never commit `build/`, `local.properties` or secrets.

## Definition of Done

A change is done when:

1. It matches the wireframe and the navigation model, including back/return behavior.
2. `./gradlew assembleDebug` succeeds with no new warnings, and `./gradlew lint` reports no new issues.
3. Tests pass (`./gradlew test`). New logic in `data/` (e.g. balance calculations) has unit tests.
4. The affected flow was clicked through on an emulator, including the Back button and offline mode (App settings → Simulate offline mode) where relevant.
5. Edge cases are handled: empty states, invalid input disabled or explained, deleted/missing items fall back to `MissingScreen`.
6. There is no leftover debug code (`Log.d`, TODO hacks), and `README.md` is updated if structure or setup changed.
7. It has been reviewed by at least one other group member before merging to `main`.
