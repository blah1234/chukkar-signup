Steps for a new app deployment:
- Create new launcher icons: 
    http://android-ui-utils.googlecode.com/hg/asset-studio/dist/icons-launcher.html

- Reference the new launcher icons in AndroidManifest.xml

!!!! - New value for "base_url" in application.properties

!!!! - New value for "launch_app_http_host" in strings.xml
    
- New package name for app:
	Start in AndroidManifest.xml and then use Refactor-->Rename context menu item on base package

- Sign APK with private key:
    http://developer.android.com/guide/publishing/app-signing.html#ExportWizard
    
- Publish:
    https://market.android.com/publish/Home