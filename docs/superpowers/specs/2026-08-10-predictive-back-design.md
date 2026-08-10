# Predictive Back for Xixi's Kitchen

## Status

Approved design. The implementation plan must be written before application code is changed.

## Goal

Add Android predictive back behavior to the existing single-activity Jetpack Compose app. The gesture must provide a visible, cancelable preview on Android 13 and newer while preserving ordinary back behavior on older versions.

## Existing Context

- App architecture: one `ComponentActivity` hosting Compose content, Hilt, a shared `KitchenViewModel`, and one `Navigation Compose` `NavHost`.
- Routes: `kitchen`, `orders`, `discover`, `mine`, `admin`, and `orderDetail/{id}`.
- Build baseline: `minSdk 24`, `targetSdk 36`, `compileSdk 36`, Activity Compose `1.9.3`, Compose BOM `2024.10.01`, and Navigation Compose `2.8.4`.
- Existing worktree contains unrelated uncommitted UI, data, manifest, and build changes. The implementation must preserve those changes and edit only the files needed for this feature.

## Decisions

### Top-level back semantics

`kitchen`, `orders`, `discover`, and `mine` are independent top-level destinations. Switching tabs must leave only the selected top-level destination as the active root for back behavior. Pressing or swiping back from any of these four pages therefore returns to the launcher through the Activity's system back behavior; it must not navigate to `kitchen` or to the previously selected tab.

The existing per-tab UI state preservation remains desirable, but it is separate from the back stack: switching tabs may save a tab's scroll/form state for restoration, while old tab entries must not remain traversable by back. This rule applies to both system back and the matching top-app-bar behavior, where a top-level page exposes one.

### Nested back semantics

`orderDetail/{id}` remains a child destination of the page that opened it. `admin` remains a child destination of `mine`. Their toolbar back buttons and system back gesture use the same `NavController.popBackStack()` operation. A completed gesture pops exactly one entry; a canceled gesture changes neither the route nor the ViewModel state.

### Visual style

Use the selected Material-style scale/reveal treatment for nested destinations:

- The exiting nested page scales toward approximately `0.90f`, fades slightly, and gains a `24.dp` rounded treatment during the transition.
- The previous page is revealed from a slightly reduced scale (approximately `0.97f`) to full size and full opacity.
- The transition origin is the center of the content surface.
- The timing/easing should follow the platform/library predictive-back seek behavior rather than a separate gesture detector. The animation must be seekable, so the same progress controls completion and reversal.

The exact API composition should use `NavHost`'s `popEnterTransition` and `popExitTransition` (and corresponding forward transitions where needed). Do not wrap the host in a second global `PredictiveBackHandler`; Navigation Compose already collects the back-progress flow and commits or cancels the pop.

Top-level-to-top-level tab changes should remain visually quiet (a short fade or no transition) and must not use the nested-page scale treatment. The system owns the final back-to-home animation from a top-level root.

### Platform opt-in and dependency

- Upgrade `androidx.navigation:navigation-compose` to stable `2.9.8`, retaining the current compile/target SDK and other dependencies unless the implementation build proves a necessary compatibility adjustment.
- Add `android:enableOnBackInvokedCallback="true"` to `MainActivity` in the manifest. Keep the opt-in scoped to this app Activity rather than changing unrelated SDK Activities.
- Do not enable edge-to-edge as part of this feature. Existing inset and glass-surface layout should remain unchanged.

## Architecture and Data Flow

1. `MainActivity` continues to own the Compose content and the Hilt-scoped `KitchenViewModel`.
2. `XixiKitchenApp` owns the remembered `NavHostController` while the user is logged in.
3. A small navigation helper centralizes top-level tab selection. It removes/replaces historical top-level destinations while storing only UI state needed to restore a tab; saved state must never recreate historical top-level entries in the back stack.
4. The `NavHost` declares default quiet transitions and explicit nested pop transitions. Navigation's internal `PredictiveBackHandler` seeks these transitions from gesture progress.
5. On completed nested back, Navigation pops the current entry. On cancellation, Navigation leaves the back stack intact and the seekable transition returns to its resting state.
6. On a top-level root, Navigation does not intercept back. Android dispatches the Activity-level back-to-home animation.
7. Screen refresh effects must not launch duplicate network work merely because a predictive gesture starts and is then canceled. Existing explicit pull-to-refresh behavior remains unchanged; any lifecycle-aware refresh adjustment must be limited to the affected screens.

## Error and Interaction Boundaries

- Dialogs, sheets, cart UI, and edit confirmations keep priority over navigation back. A back event dismisses the topmost transient surface first.
- Existing horizontal card-dismiss gestures must continue to work. Edge-origin back gestures must win when started at the system edge; central card swipes must not pop the navigation stack.
- RTL layouts must mirror directional motion without changing which side starts the system back gesture.
- On API 24-32, the app uses regular committed back and the same transitions complete without progress preview.
- Rapid repeated back events must not produce duplicate `popBackStack()` calls or stale selected-order content.
- A missing or loading order detail still has a valid back target and must animate/pop safely.

## Implementation Surface

Expected files, subject to the implementation plan's final verification:

- `app/build.gradle`: Navigation Compose version update.
- `app/src/main/AndroidManifest.xml`: Activity-level predictive-back opt-in.
- `app/src/main/java/com/xixikitchen/jetpack/ui/XixiKitchenApp.kt`: top-level navigation helper, `NavHost` transitions, and any narrowly scoped refresh/lifecycle changes.
- `app/src/main/java/com/xixikitchen/jetpack/ui/AdminScreens.kt`: only if the shared admin back callback needs adaptation; no unrelated screen refactor.
- Focused unit/UI test files under `app/src/test` or `app/src/androidTest`, plus any required test dependencies.

## Verification Matrix

### Build and static checks

- `./gradlew :app:compileDebugKotlin`
- `./gradlew :app:testDebugUnitTest` (when tests are added)
- `./gradlew :app:assembleDebug`
- Confirm no new deprecation, duplicate-navigation, or manifest-merge errors.

### Behavioral checks

- API 33/34 emulator with predictive back developer option enabled.
- API 35 and API 36 emulator/device with gesture navigation.
- API 24-32 fallback using ordinary back.
- `orders -> orderDetail -> orders`, including cancellation halfway through the gesture.
- `mine -> admin -> mine`, including cancellation halfway through the gesture.
- Back from each of `kitchen`, `orders`, `discover`, and `mine` exits to the launcher instead of returning to another tab.
- Open dialog/cart/editor, then back: dismiss transient UI only.
- Test edge back against card swipe-to-dismiss, RTL, three-button navigation, rapid repeated back, and empty/loading detail states.

## Non-goals

- No redesign of the glass visual system.
- No edge-to-edge migration.
- No custom Activity-level back dispatcher or platform callback.
- No changes to server APIs, authentication, push notifications, or unrelated screen behavior.
- No requirement to add a full end-to-end device test harness; the plan should add the smallest focused automated coverage and document the manual emulator checks.
