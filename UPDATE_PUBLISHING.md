# Android updates through GitHub Releases

The home-page Check for updates button uses the latest stable release of the public repository DangerousKitty666/StruKit. Source commits alone do not deliver app updates. The user downloads inside StruKit and confirms installation in Android; there is no silent installation or background polling.

## First installation

Install 7.3 A once by the existing APK method to obtain the updater. 7.3 A APK was built locally on 7 November 2026; no GitHub release has been published yet. Keep using the original signing key: the installed app and every update must have the same application ID and signing certificate. Keep the keystore outside GitHub and backed up privately. The existing personal-install signing configuration points to C:/Users/gidro/.android/debug.keystore.

## Publish an Android update

1. Increase versionCode in app/build.gradle.kts for each update. 0.788 A uses 21. The next update must use 22 or higher. Also set versionName and the visible revision label.
2. Build and test the APK using the original signing key. Do not generate a replacement key.
3. Name the APK exactly `StruKit-0.788-A-vc21.apk` for this version. The `vc` number must equal the APK versionCode.
4. Commit and push the source to the existing repository.
5. On GitHub, choose Releases, then Draft a new release. Create tag `v0.788-A` targeting main. Use title `StruKit Revision 0.788 A` and describe the changes.
6. Attach the APK to the release's binary-file area. Do not add it as a normal source file. Wait for the upload to finish.
7. For an installable stable update, leave Pre-release unchecked, select Set as latest release, then publish. Draft/pre-release versions are ignored. Do not mark a Windows-only release as latest in this Android update feed.
8. On a phone running an older updater-enabled version, choose Check for updates, Download update, then Install update. Allow installations from StruKit in Android Settings on the first attempt, then confirm the installation. App data is retained by an ordinary signed update.

## Validation and behaviour

- HTTPS-only GitHub release downloads with timeouts, cancellation, size limits and progress.
- GitHub's SHA-256 asset digest is checked when supplied. APK package ID, actual versionCode and signing certificate must match the expected application/update.
- Private repositories are not supported; no access token is embedded.
- Android's installer performs the final APK verification and asks the user to install. A phone installation test is still pending.
- The updater introduces Internet permission. CAD viewer content remains local through its request allowlist and Content Security Policy; CAD files are not uploaded.
- HTTP 404 from the latest-release endpoint means there is no accessible stable release, not that the app is current.

Sources: [GitHub release management](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository), [Android installation permission](https://developer.android.com/reference/android/content/pm/PackageManager#canRequestPackageInstalls()), [FileProvider](https://developer.android.com/reference/androidx/core/content/FileProvider).

Set this release as latest. Android versionCode 17 recognises it as newer than vc16.

