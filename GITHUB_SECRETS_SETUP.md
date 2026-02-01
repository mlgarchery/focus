# GitHub Secrets Setup for Android Release Signing

This document explains how to set up GitHub secrets for automatically signing and releasing your Focus Android app.

## Prerequisites

You need an Android keystore file for signing release builds. If you don't have one yet, create it using:

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias focus-release
```

or in Android Studio: In the menu Build -> Generate Signed APK with the parameters in the command above

Follow the prompts to set:

- Keystore password
- Key alias (e.g., `focus-release`)
- Key password
- Your name, organization, etc.

**⚠️ IMPORTANT**: Keep your keystore file and passwords secure! If you lose them, you cannot update your app on the Play Store.

## Required GitHub Secrets

You need to add 4 secrets to your GitHub repository:

### 1. KEYSTORE_BASE64

This is your keystore file encoded as base64.

**To create it:**

```bash
# On Linux/Mac:
base64 -i release-keystore.jks | tr -d '\n' > keystore_base64.txt

# On Windows (PowerShell):
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks")) | Out-File -Encoding ASCII keystore_base64.txt
```

Copy the contents of `keystore_base64.txt` (it will be a very long string).

### 2. KEYSTORE_PASSWORD

The password you used when creating the keystore.

### 3. KEY_ALIAS

The alias you specified when creating the keystore (e.g., `focus-release`).

### 4. KEY_PASSWORD

The key password (often the same as the keystore password).

## Adding Secrets to GitHub

1. Go to your GitHub repository: https://github.com/mlgarchery/focus
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each of the 4 secrets listed above

**Secret Names (must match exactly):**

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

## How the Workflow Works

### On Every Commit/PR

- Builds a **debug APK** (unsigned)
- Uploads it as a workflow artifact
- Verifies that the app builds successfully

### On Version Tag Push (e.g., `v1.0.0`)

- Builds a **release APK** (signed with your keystore)
- Creates a GitHub Release automatically
- Attaches the signed APK to the release
- Generates release notes from recent commits

## Creating a Release

To trigger a release build:

```bash
# Make sure your code is committed and pushed
git add .
git commit -m "Release version 1.0.0"
git push

# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

The GitHub Action will automatically:

1. Build the signed release APK
2. Create a GitHub Release at: https://github.com/mlgarchery/focus/releases
3. Upload the APK to the release

## Troubleshooting

### Build Fails with "Keystore was tampered with"

- Check that `KEYSTORE_BASE64` was copied correctly (no line breaks or extra spaces)
- Verify `KEYSTORE_PASSWORD` is correct

### "Could not find key with alias"

- Check that `KEY_ALIAS` matches exactly what you used when creating the keystore
- Use `keytool -list -v -keystore release-keystore.jks` to verify the alias

### Release Not Created

- Ensure the tag follows the format `vX.X.X` (e.g., `v1.0.0`, `v2.1.3`)
- Check the Actions tab for error messages: https://github.com/mlgarchery/focus/actions

## Workflow Files

- **Workflow:** `.github/workflows/build.yml`
- **Build Config:** `app/build.gradle.kts` (signing configuration)

## Security Notes

- ✅ Keystore file is NOT committed to the repository (protected by `.gitignore`)
- ✅ Secrets are encrypted in GitHub and only visible to repository owners
- ✅ Secrets are only accessible during workflow execution
- ⚠️ Keep a backup of your keystore file in a secure location
