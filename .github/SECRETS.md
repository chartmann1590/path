# GitHub Secrets for Android releases

The Android release workflow requires these repository secrets:

- `GOOGLE_SERVICES_JSON`: the full contents of `app/google-services.json`
- `RELEASE_KEYSTORE_BASE64`: base64-encoded release keystore
- `RELEASE_STORE_PASSWORD`: release keystore password
- `RELEASE_KEY_ALIAS`: release key alias
- `RELEASE_KEY_PASSWORD`: release key password
- `ADMOB_APPLICATION_ID`: AdMob application id used in `AndroidManifest.xml`
- `ADMOB_BANNER_AD_UNIT_ID`: banner ad unit id
- `ADMOB_INTERSTITIAL_AD_UNIT_ID`: interstitial ad unit id
- `ADMOB_REWARDED_AD_UNIT_ID`: rewarded interstitial ad unit id
- `GH_API_TOKEN`: token used by in-app feedback issue creation
- `GH_REPO_OWNER`: feedback target repository owner
- `GH_REPO_NAME`: feedback target repository name

The workflow validates these before building so a release cannot be created with missing production configuration.
