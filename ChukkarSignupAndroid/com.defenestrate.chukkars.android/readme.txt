Steps for a new app deployment:
- Create new launcher icons: 
    http://android-ui-utils.googlecode.com/hg/asset-studio/dist/icons-launcher.html

- Reference the new launcher icons in AndroidManifest.xml

- New values in strings.xml for:
    "app_name"
    "get_players_url"
    "add_player_url"
    "edit_chukkars_url"
    "query_reset_url"
    "get_active_days_url"
    
- New package name for app:
	Start in AndroidManifest.xml and then use Refactor-->Rename context menu item on base package

- Sign APK with private key:
    http://developer.android.com/guide/publishing/app-signing.html#ExportWizard
    
- Publish:
    https://market.android.com/publish/Home