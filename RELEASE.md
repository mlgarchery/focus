# How the Release Build Works

## On Version Tag Push (e.g., `v1.0.0`)

- Builds a **release APK** (signed with your keystore)
- Creates a GitHub Release automatically
- Attaches the signed APK to the release

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
