# Khedmati Android GUI Prototype

This branch converts the original toolbar/drawer greeting demo into a single-activity Kotlin Android prototype based on the supplied Khadamate/Khedmati product specification.

## Implemented GUI flows

- Public signed-out browsing with no separate visitor role.
- English, Arabic, and French interface resources; Arabic uses Android RTL mirroring.
- Bottom navigation: Home, Search, Saved, Account.
- Home feed with professional posts and category shortcuts.
- Search by keyword, category, Lebanese region, and minimum rating.
- Dummy "near me" and map-selection actions that clearly state that no GPS/map provider is connected.
- Professional result cards and public professional profiles.
- Services with no price, fixed price, starting-from, range, and description-only price behavior in the seeded data.
- Exact, approximate, and city-only public-location modes in seeded profiles.
- Save/unsave, like/unlike, comment, share, client review, and review update behavior.
- Client and professional dummy sign-in/register flows.
- Professional profile editing, service addition, dummy image attachment, and post creation.
- Local simulated notifications.
- Phone dialer confirmation and external social-link opening.
- Realistic sample data for Lebanon.

## Intentionally dummy / not connected

The project contains **no real cloud credentials and no real cloud network calls**.

- `DummyCloudRepository`: in-memory database/authentication/community behavior.
- `DummyStorageService`: returns `dummy-storage://...` image references and performs no upload.
- Notifications are generated locally; no FCM/APNs/Azure notification provider is configured.
- "Near me" does not request GPS; it demonstrates the flow by selecting Beirut.
- The map selector is a GUI placeholder and does not contact a map provider.

This is deliberate so Firebase, Azure, Supabase, a custom API, or another backend can later replace the data layer without embedding secrets in the app.

## Architecture

Only `MainActivity` is an Android activity. The previous drawer demo and its obsolete menu resources are removed. Presentation is kept in the single activity while models, preferences, dummy data access, and dummy storage are separated into their own Kotlin files.

## Known limitations before production

This prototype is Android-only because the requested repository/application is Kotlin Android. The original product document also discusses iOS and a separate administrator dashboard; those are not implemented here. A production release still needs a real backend with server-side authorization, real media storage, pagination at backend scale, map/location integration, reporting/moderation screens, blocking, account recovery, account deletion, secure authentication, rate limiting, analytics/crash reporting, accessibility/visual QA, legal content, and production tests.

## Validation performed in this change

- Pure Kotlin model/data files compile with `kotlinc`.
- Resource XML and AndroidManifest XML were parsed for well-formedness.
- All app string-resource references were checked against English, Arabic, and French resource files.
- Unit tests cover category filtering, one-review-per-client update behavior, and dummy storage URL behavior.
- A clean GitHub Actions runner using Java 17 and Android SDK 35 successfully ran `./gradlew test assembleDebug` at commit `40bd76965cec5be16396a2d5997ec7d2cfcf3466`.
- The validated run completed `62` Gradle tasks with `BUILD SUCCESSFUL`; the temporary validation workflow was removed afterward so it does not remain as unnecessary project configuration.

You can open the project in Android Studio and run `./gradlew test assembleDebug` with Android SDK 35 installed.
