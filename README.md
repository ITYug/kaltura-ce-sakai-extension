# kaltura-ce-sakai-extension
Kaltura CE extension for Sakai

This is the Sakai / Kaltura CE integration. 
Developed by Aaron Zeckoski (Lead) of Unicon for Kaltura and University of Virginia / Yale University.
Updated by Chaitanya Kumar Ch of Vidyayug Organization for Sakai 11.x

This tool provides integration between the Kaltura video transcoding and hosting system and a Sakai CLE installation.
NOTE: You need version 11.x of sakai or better to run this tool.

Known Issues:
I didn't update it to work with CK Editor

BUILDING THE TOOL:
mvn clean install

Sakai 11+ (default)
mvn clean install


INSTALLING THE TOOL:

Deploy the app into tomcat by running this command from the location of this README
mvn sakai:deploy
OR copy the war file from the target directory into your tomcat webapps dir manually.

The database tables will be created automatically when the tool first deploys if you have auto.ddl=true.
If you do not, you can use the .sql files under the ddl dir for your database to generate the tables. You
should do this before you deploy the tool. You can generate new ddl files using the -Pddl profile.
NOTE: You will need to run the "upgrade" ddl files as well if you are not using auto.ddl
      AND are doing an upgrade for the first time (they are located in the ddl directory).

IMPORTANT NOTE on Migration to Kaltura server storage (visible in the KMC) after Nov 2012:
Upgrading to a version released after Nov 1 2012 will cause the server to migrate the content 
from the database tables to the kaltura server itself. The migration will run automatically on
server startup and will run on only one server. If the migration is interrupted, it will
continue the next time the server is started (or on the next server started in your cluster).
It is best to run this migration during a period of low usage to avoid user confusion.
The migration takes approximately 1 minute per 30 media items.
The migration will copying all data from the Kaltura tables in the Sakai database into the 
Kaltura server (in a variety of places but mostly in the metadata storage). 
Accessing the Media Gallery tool while it is migrating will provide an indication 
of the estimated time left and content to be migrated.
WARNING: For institutions using the same kaltura.partnerid with dev, test, stage, and prod servers/clusters.
You must configure this key (kaltura.rootCategory=) to something different 
on each of your servers/clusters. If this is not done, changes made to content
in any server/cluster will affect all of them. It will also make it impossible 
to use the KMC effectively because you will have production, testing, 
and dev data in the same categories and playlists in the KMC.
This migration allows KMC admins to better manage the content and add (or remove) kaltura entries 
into courses using the KMC. It also allows KMC admins to create collections 
(by adding a manual playlist to the course site category).

Migration reset:
You can reset the migration if you need or want to rerun it.
WARNING: Any changes made after the migration will be lost.
A) You can reset the migration in your Sakai database by setting all the "MIGRATED" columns 
   to 0 or null in the kaltura_coll and kaltura_item tables (while your Sakai server is off).
B) Then you can clear out your KMC using the following steps:
   1) Content -> Playlists -> Remove all playlists which are in the sakai root category or any sub categories.
   2) Content -> Categories -> Remove the "sakai" (or whatever your kaltura.rootCategory is set to) category and all sub categories
   3) Settings -> Custom Data -> Remove the "entry" schema
      WARNING: do NOT do this once you have live data migrated, it will destroy all 
               the stored metadata and effectively reset all your permissions to defaults on all items and collections.
   NOTE: Your Kaltura account manager should be able to help if you have trouble doing those steps in the KMC.
C) When you restart Sakai, the system will migrate all data to the KMC again.


FCK editor integration:
See the fckeditor/README for instructions on installing the optional FCK editor integration.

Configuration:
Add the following to your sakai.properties file before starting up your server.

kaltura.enabled=true
## your kaltura account partner id
kaltura.partnerid=111111
## your kaltura account admin secret key
kaltura.adminsecret=111111
## your kaltura account secret key
kaltura.secret=YYYYYYYYYYYYYYYYYYYYYYYYYYYYY
## your kaltura server URL (Make sure you use https if your sakai server uses SSL)
#kaltura.endpoint=http://www.kaltura.com
## your kaltura CDN URL (Make sure you use https if your sakai server uses SSL)
#kaltura.cdn=http://cdn.kaltura.com
## custom kaltura widget ids
#kaltura.player.view=111111
#kaltura.player.video.width=480
#kaltura.player.video.height=360
#kaltura.player.edit=111111
#kaltura.player.image=111111
#kaltura.player.image.width=480
#kaltura.player.image.height=360
#kaltura.player.audio=111111
#kaltura.player.audio.width=480
#kaltura.player.audio.height=30
# NOTE: clipping must be enabled in the KMC and in Sakai (see settings below)
#kaltura.clipper=1111111
# The clipper requires a custom player to make the preview work (see https://jira.sakaiproject.org/browse/SKE-98)
# OnPrem users must replace this or it will fail to work, hosted users can use the default player
#kaltura.clipper.player=111111
## NOTE: 5612211 is the old uploader with "my content" in it, it was replaced in Jan 2013 with a new default without "my content"
#kaltura.uploader=1111111
## the special uploader, (if set) will be shown to any users with the kaltura.uploadSpecial permission
#kaltura.uploader.special=1212121
## HTML 5 player
## enabled the html5 compatible player (off by default which means it uses the flash player)
## WARNING: the html5 player requires updated kaltura player uiconf ids (after 7 Mar 2012) 
##          so if you are using custom older players you will need to contact Kaltura for assistance before enabling this,
##          the minimum version of Kaltura is 1.6.8 to use the HTML5 player
#kaltura.player.html5.enabled=true
# Force a custom copy of the kaltura html5 JS library (uses a current one from Kaltura by default)
# In general, there shouldn't be a need to modify this setting. Typically this should only be changed if you are hosting the JS file 
# yourself rather than using the version from Kaltura. If you want to use a different version of the file hosted by Kaltura, the proper 
# change to make in the settings is to {kaltura.player.view|image|audio} properties (changing the player ids).
# DEFAULT: The code defaults to grabbing a current version of the JS from Kaltura using the template shown here. 
# kaltura.player.html5.js={kaltura.endpoint}/p/{kaltura.partnerId}/sp/{kaltura.partnerId}00/embedIframeJs/uiconf_id/{uiConfId}/partner_id/{kaltura.partnerId}
## display warning dialog on item delete
#kaltura.showDeleteItemWarnings=false
## Site library tab only visible if user has privs (Default: false - no permissions required to view the site library)
#kaltura.siteLibraryRequiresPrivs=true
## Enable kaltura tool site archive and duplication support
## Cause all kaltura content (which means the collections and links to the content 
##   on the kaltura server but not the actual media data) to be archived 
##   and copied across when archiving or duplicating a site in sakai, Default: false (disabled)
#kaltura.archive.support.enabled=true
## Allows items to be clipped via the Sakai interface (if this is off then the clippable permission is not shown as well), default: False
##   The CONTENT_INGEST_CLIP_MEDIA option must also be enabled for the kaltura account via the KMC
#kaltura.clipping.enabled=true
## Set the "clippable" permission to true or false (to match the setting) for all new items (all newly added items).
##   This is false by default (indicates clipping the item is currently NOT allowed).
#kaltura.clipping.default.allowed=true
## Control the root category used to store all site (course) categories in for Sakai, Default: "sakai"
##   WARNING: changing this after the migration will cause all existing content to be orphaned (it will not be removed but it will become inaccessible in Sakai)
#kaltura.rootCategory=sakai
## Control the mechanism used for kaltura metadata updates, 
## when false, this uses the newer XSLT metadata updates processor (atomic updates) but only works on new Kaltura video platform versions (2013+),
## when true, this uses the XML only mechanism which is slower but works on all versions of Kaltura video platform
## Default: false
#kaltura.old.xml.metdata.update=true
## Allows the migration to be disabled
## WARNING: This should NOT be disabled in PRODUCTION since it will mean existing user collections and
## item permissions will no longer be accessible. If you plan to not migrate older database data,
## you should remove all the content from the kaltura database tables (KALTURA_COLL and KALTURA_ITEM)
## and then remove this config option.
## Default: false (migration enabled)
#kaltura.migration.disabled=true
## DEPRECATED SETTINGS
## WARNING: this is deprecated, use the i18n file to adjust the content of messages
## custom intro message (appears on the home page of the tool, can include html)
#kaltura.tool.introduction.instructions=
## custom empty collections message (appears on the home page of the tool when there are no collections, can include html)
## WARNING: this is deprecated, use the i18n file to adjust the content of messages
#kaltura.empty.collections.instructions=
## WARNING: use of the old editor is no longer supported, using the clipping editor (clipper) instead
#kaltura.editor=111111

Configuration Notes:

Fill in at least your partnerid, adminsecret, and secret.

Site archiving support:
When this is enabled, the following functionality in Sakai will cause Kaltura content to be archived/copied/merged
 - Site Info -> Duplicate Site
 - Site Info -> Import from Site
 - Site Archive -> Export
 - Site Archive -> Import
   This will only work if "KalturaEntityProducer" is added to the archive services list or merge filtering is disabled.
   Disable merge filtering using the following Sakai configuration property:
   Sakai 2.8 or older: mergeFilterSakaiServices@org.sakaiproject.archive.api.ArchiveService=false
   Sakai 2.9 or newer: archive.merge.filter.services=false
NOTE: The site is duplicated with all the collections, including the media that was added by an admin and 
may not be visible to the instructor(s) in the new site (because of permissions).

OnPrem (a locally hosted Kaltura server):
If you are running a local kaltura server then you should also update the kaltura.endpoint and kaltura.cdn.
You will also need to update all the players to match with player uiconf ids from your system 
(the defaults are only good for hosted (SaaS)). You also might not be able to use some newer features like
the html5 player unless you have the paid version available.

SSL:
If you use SSL (https) on your sakai server then you should also update the kaltura.endpoint and kaltura.cdn 
to use SSL (https). For example: kaltura.cdn=https://cdn.kaltura.com

KALTURA features:
METADATA_PLUGIN_PERMISSION - Required for the tool to work at all. Contact support or enabled this feature before installing this tool.
CONTENT_INGEST_CLIP_MEDIA - Required for clipping to be enabled.  Contact support or enabled this feature to use the clipping feature.

CUSTOM WIDGETS:
Kaltura's application studio enables you to configure your player instances with branding, watermarks, select controls etc. 
The application studio is accessed through the KMC Studio tab on the Kaltura website.

USING THE TOOL:

Start Sakai and you will see the new app appear as a tool option which can be selected for a course or project site.

My Media:
The "My Media" tab displays all the content uploaded by the current user. Users can always edit and clip 
the content in their My Media (unless clipping is disabled by the system config option).
The My Media tab can be hidden using permissions (see below).

Item Permissions:
 - Public (non-hidden)  - Allows media to be visible or hidden from users, Defaults to private (only the owner or manager can see it)
 - Reusable (shared)    - Allows media to be used in the rich text (FCK) editor, Default to false (cannot be used in the editor)
 - Clippable (edtiable) - Allows media to be clipped using the Clipping Editor (trimming and clipping), Default to false (cannot be clipped)

NOTE on Item visibility (public/non-hidden):
The visibility of the items is controlled via the public/non-hidden setting on the item (private/hidden by default) and these factors:
 - system admin can see all items
 - manager (kaltura.manage) can always see items even if they are hidden
 - owner of an item can always see it
 - user with control or edit permissions on an item can always see it
 - items which are set as non-hidden are visible to anyone with read permission in the site
 - items in Community (public) and Personal (shared) collections are visible to anyone with read permission in the site

Collection Level Permissions:
The ability to control (modify/remove) items is handled via a combination of the user being an admin 
and the setting on the collection:
 - Community (public) - users can remove any of the items in the collection, 
   any user with at least one non-special kaltura permission can add items to the collection,
   all items are visible to all users, 
   owner can add/edit/remove all items
 - Personal (shared) - users can only remove their own items but cannot change other items,
   any user with at least one non-special kaltura permission can add items to the collection, 
   owner can add/edit/remove all items
 - Instructor (admin) - only admins can add/edit/remove the items in the collection, 
   owner can add/edit/remove all items
   (normal users can only view it) [DEFAULT]
 - Owner (private) - only the collection owner can add/edit/remove the items in the collection,
   (does NOT make the items in the collection invisible)

NOTE on Collections and Item permissions:
 - The owner of a collection or the user controlling a collection (admin) can 
 always see all the items in the collection, they also are granted the ability to 
 update the item permissions on items in their collections

Sakai Permissions (in Sakai course/project/site):
 - kaltura.admin    - Allows a user to create and manage the media collections and their items, 
                      can also adjust item permissions on the items in the collections they control
 - kaltura.manager  - Allows a user to manage the permissions (hidden, shared, etc.) for all items 
                      including those in collections they have access to
 - kaltura.editor   - Allows a user to edit the metadata (title, desc, tags) for all items 
                      including those in collections they have access to
 - kaltura.write    - Allows a user to upload media and add media items to the site library,
                      if the user does not also have read access they will be limited to only
                      viewing collections they can upload items into and only viewing their
                      own uploaded items
 - kaltura.read     - Allow the users to view the content (media items and collections) only,
                      if the user does not also have write access they will not be able to
                      upload content at all or access My Media

Special Sakai permissions (these have unique functions and are for handling special cases):
 - kaltura.uploadSpecial    - Allow the user to view the special uploader (a.k.a. UpSp)
                              as a replacement for the regular uploader,
                              the special uploader typically contains extra tabs
 - kaltura.showSiteLibrary  - If kaltura.siteLibraryRequiresPrivs is true, user must have
                              this priv to see the Site Library tab.

Permissions grid:
                                        | admin | manager | editor | write | read |
view collections                        |   X   |    X    |   X    |   X$  |  X$  |
view hidden collections                 |   X   |    X    |   X    |   -   |  -   |
add/edit/remove collection              |   X   |    -    |   -    |   -   |  -   |
add/remove item to/from collection *    |   X   |    -    |   -    |   -   |  -   |
reorder/remove item from collection *   |   X   |    -    |   -    |   -   |  -   |
view items                              |   X   |    X    |   X    |   -   |  X   |
view hidden items *                     |   -   |    X    |   X    |   -   |  -   |
view item download link                 |   -   |    X    |   -    |   -   |  -   |
view item embed code                    |   X   |    X    |   -    |   -   |  -   |
upload (add) new items (library)        |   -   |    -    |   -    |   X   |  -   |
remove items (library)                  |   -   |    X    |   -    |   -   |  -   |
edit items metadata                     |   -   |    -    |   X    |   -   |  -   |
edit items permissions (library)        |   -   |    X    |   -    |   -   |  -   |
edit items permissions (collection)     |   X%  |    X    |   -    |   -   |  -   |
access custom upload tab on KCW         |   -   |    -    |   -    |   -   |  -   |

$ can only view collections which have at least one public item (Community/Personal collections always viewable)
* also can be allowed by collection permissions
% only for the collections they control
NOTE: items permissions refer to all items in the site

-Chaitanya Kumar Ch(chaitanya.cheekate@vidyayug.com)
