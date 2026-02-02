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

To get SHA-1 and SHA-256 fingerprints for your signing certificate in Android Studio:

- Open the Gradle tool window on the far right side of Android Studio.
- Click the Execute Gradle Task icon (the small "elephant" icon).
- In the command box that appears, type: signingReport and press Enter.
- Open the Run tab at the bottom of the screen. You will see a list of all your build variants. 
  Scroll until you find your specific variant ("release" one) to see the SHA-1 and SHA-256 strings.

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
