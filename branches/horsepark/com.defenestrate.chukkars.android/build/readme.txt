Steps for a new app deployment:
- Create new launcher icons: 
    http://android-ui-utils.googlecode.com/hg/asset-studio/dist/icons-launcher.html

- Reference the new launcher icons in AndroidManifest.xml

!!!! - Reference correct club name in strings.xml

!!!! - New value for "base_url" in application.properties

!!!! - New value for "launch_app_http_host" in strings.xml

!!!! - New cover art for club

!!!! - In Constants.java, make sure to enter the correct Google Play Services project number for the value of GCM_SENDER_ID
- Google Cloud Messaging project #: 738566066161
    
- New package name for app:
	Start in AndroidManifest.xml and then use Refactor-->Rename context menu item on base package

- Sign APK with private key:
    http://developer.android.com/guide/publishing/app-signing.html#ExportWizard
    
- Publish:
    https://market.android.com/publish/Home