How to browse local Java App Engine datastore = http://localhost:8888/_ah/admin/

Steps to deploy a new instance:
1) Set cron.xml in code before compiling and deploying.
2) Use the appropriate properties in DisplayStrings.properties before compiling and deploying.
3) Create the initial new admin

Steps 4 - 6 are now taken care of by the new "Game Days" configuration DnD panel on the admin portion of the website:
4) Use AppEngine Datastore Viewer to set which days are active in DayOfWeek table.
5) Manual reload of the configuration of the Day enumerations ON THE SERVER:
    http://<application name>.appspot.com/signup/config/loadDaysConfig
6) Advance the RunDate of the RESET CronTask, so mobile apps can pick up the change in step (3):
    http://<application name>.appspot.com/signup/cron/removeAllPlayers

7) Add email settings.
8) make sure to add admin sender email to https://appengine.google.com/permissions?app_id=s~<app_id>