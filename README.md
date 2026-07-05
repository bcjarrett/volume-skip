# Volume Skip

Skip tracks with the volume buttons on Android — hold volume-up for the next track,
volume-down for the previous. Works with the screen off. No ads, no analytics, no internet permission.

- Only active while audio is playing; short presses change the volume as normal.
- Uses an accessibility service (key-event filtering only — it cannot read screen content),
  which is the only Android API that can observe volume keys system-wide with the screen off.

## Build

Requires JDK 17 and the Android SDK (platform 35). Create `local.properties` with
`sdk.dir=<path-to-sdk>`, then:

```sh
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Enable

Open the app → **Enable in Accessibility settings** → toggle on **Volume Skip**.
Play some audio and hold volume-up.

Android shows a standard, non-customizable warning when enabling _any_ accessibility service.
This app declares only the volume-key-filtering capability — no screen-content access — and
requests no internet permission. You can verify that in
[accessibility_service_config.xml](app/src/main/res/xml/accessibility_service_config.xml)
(no `canRetrieveWindowContent`) and [AndroidManifest.xml](app/src/main/AndroidManifest.xml)
(only `VIBRATE`).
