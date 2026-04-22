# Android — App Signing Setup

We use **Google Play App Signing**. You only manage an *upload key*; Google
holds the real signing key. If the upload key is lost or compromised, you can
reset it via Play Console without bricking the app.

## One-time setup (do this once per developer machine)

### 1. Generate the upload keystore

Run on **your own machine** (the private key never enters this repo):

```bash
mkdir -p ~/.teachmeski-keystore
keytool -genkeypair -v \
  -keystore ~/.teachmeski-keystore/upload.jks \
  -alias teachmeski-upload \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass 'YOUR_PASSWORD' -keypass 'YOUR_PASSWORD' \
  -dname "CN=TeachMeSki, O=TeachMeSki, L=Taipei, C=TW"
```

Back up `~/.teachmeski-keystore/upload.jks` AND the password somewhere you
won't lose (iCloud Drive, 1Password, etc.). If you lose it you'll have to
request an upload key reset from Google — annoying but not fatal thanks to
Play App Signing.

### 2. Create `keystore.properties`

Copy the example file and fill in real values:

```bash
cp teachmeski-android/keystore.properties.example teachmeski-android/keystore.properties
```

Edit `teachmeski-android/keystore.properties`:

```properties
storeFile=/Users/YOU/.teachmeski-keystore/upload.jks
storePassword=YOUR_PASSWORD
keyAlias=teachmeski-upload
keyPassword=YOUR_PASSWORD
```

This file is gitignored. DO NOT commit it.

### 3. Build a release AAB

```bash
cd teachmeski-android
./gradlew :app:bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

Upload `app-release.aab` to Play Console → Internal testing → Create new release.

On Play Console → **Setup → App signing**, Google will ask you to opt into
Play App Signing (you should; this is the whole point). Upload the public
certificate corresponding to your upload key if asked (Play usually handles
this automatically on first upload).

## What happens without `keystore.properties`?

Release builds fall back to the **debug** signing config. The resulting AAB
is **not uploadable to Play** but is fine for local inspection / CI smoke
tests. That's intentional so a fresh clone can still run `./gradlew build`
without secrets.

## Version bumps

Every AAB uploaded to Play needs a **unique, increasing** `versionCode`.
Edit `app/build.gradle.kts`:

```kotlin
versionCode = 2   // was 1
versionName = "1.0.1"
```

Play Console rejects duplicate `versionCode`s with a clear error, so if you
forget you'll find out immediately.

## Key rotation / loss recovery

- Upload key lost: Play Console → App integrity → Request upload key reset.
- Upload key compromised: same procedure, do it immediately.
- Signing key (the one Google holds): you cannot rotate this without
  publishing a new app, so trust Play App Signing.
