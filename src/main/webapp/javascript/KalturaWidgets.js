/*
 * Copyright 2010 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

//http://jshint.com/ - disable strict mode (uncheck "When code is not in strict mode") and undef use (uncheck "When variable is defined but not used")
/*global fluid_1_3:true, swfobject: true, kWidget: true, jquery:true, kaltura:true, alert:true, confirm:true, Exception:true */
var fluid_1_3 = fluid_1_3 || {}; //use current released version number
var kaltura = kaltura || {};
var config;
(function($, fluid) {

  var toolId = null;

  var setKalturaInfo = function(that) {
    if (!config) {
      config = [];
      var url;
      if (toolId) {
        url = '/direct/kaltura/config/' + toolId + '.json?timestamp=' + new Date().getTime();
      } else {
        url = '/direct/kaltura/config.json?timestamp=' + new Date().getTime();
      }

      $.ajax({
        async: false,
        cache: false,
        url: url,
        dataType: 'json',
        success: function(json) {
          config.username = json.data.username;
          config.partnerId = json.data.kalturaConfiguration.partnerId;
          config.sessionId = json.data.kalturaSessionId;
          config.endpoint = json.data.kalturaConfiguration.endpoint;
          if (json.data.location !== null) { config.location = json.data.location; }
          // player ids
          config.widgetUploaderId = json.data.widgetUploaderId;
          config.widgetPlayerCaptureId = json.data.widgetPlayerCaptureId;
          config.widgetPlayerVideoId = json.data.widgetPlayerVideoId;
          config.widgetPlayerAudioId = json.data.widgetPlayerAudioId;
          config.widgetPlayerImageId = json.data.widgetPlayerImageId;
          config.widgetEditorId = json.data.widgetEditorId;
          config.widgetClipperId = json.data.widgetClipperId;
          // player sizes
          config.widgetPlayerVideoWidth = json.data.widgetPlayerVideoWidth;
          config.widgetPlayerVideoHeight = json.data.widgetPlayerVideoHeight;
          config.widgetPlayerAudioWidth = json.data.widgetPlayerAudioWidth;
          config.widgetPlayerAudioHeight = json.data.widgetPlayerAudioHeight;
          config.widgetPlayerImageWidth = json.data.widgetPlayerImageWidth;
          config.widgetPlayerImageHeight = json.data.widgetPlayerImageHeight;
        },
        error: function( jqXHR, textStatus, errorThrown ) {
          alert("Failure getting the kaltura config ("+url+"): "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
        }
      });
    }

    that.options.username = config.username;
    that.options.partnerId = config.partnerId;
    that.options.sessionId = config.sessionId;
    that.options.endpoint = config.endpoint;
    // player ids
    that.options.widgetUploaderId = config.widgetUploaderId;
    that.options.widgetPlayerCaptureId = config.widgetPlayerCaptureId;
    that.options.widgetPlayerVideoId = config.widgetPlayerVideoId;
    that.options.widgetPlayerAudioId = config.widgetPlayerAudioId;
    that.options.widgetPlayerImageId = config.widgetPlayerImageId;
    that.options.widgetEditorId = config.widgetEditorId;
    that.options.widgetClipperId = config.widgetClipperId;
    // player sizes
    that.options.widgetPlayerVideoWidth = config.widgetPlayerVideoWidth;
    that.options.widgetPlayerVideoHeight = config.widgetPlayerVideoHeight;
    that.options.widgetPlayerAudioWidth = config.widgetPlayerAudioWidth;
    that.options.widgetPlayerAudioHeight = config.widgetPlayerAudioHeight;
    that.options.widgetPlayerImageWidth = config.widgetPlayerImageWidth;
    that.options.widgetPlayerImageHeight = config.widgetPlayerImageHeight;

  };

  /**
   * Embeds the kaltura player
   * OLD method used swfobject embedding, new method uses kaltura JS library
   * 
   * Kaltura html5 JS library location - HTML5_LIBRARY
   * http://{KALTURA HOST}/p/{PARTNER_ID}/sp/{PARTNER_ID}00/embedIframeJs/uiconf_id/{PLAYER_ID}/partner_id/{PARTNER_ID}
   * 
   * SAMPLE CODE: http://html5video.org/wiki/Kaltura_HTML5_Configuration#Dynamically_embed_the_player
<script src="{HTML5_LIBRARY}"></script>
<div id="kalturaPlayer" style="width:{WIDTH};height:{HEIGHT};"></div>
<script type="text/javascript">
window.doPlayCallback = function( playerId ){ 
    console.log( 'kwidget doPlayCallback ' + playerId);
};
kWidget.embed({
    'targetId': 'kalturaPlayer',
    'wid': '_{PARTNER_ID}',
    'uiconf_id' : '{PLAYER_ID}',
    'entry_id' : '{ENTRY_ID}',
    'flashvars':{
        'externalInterfaceDisabled' : false,
        'autoPlay' : true
    },
    'readyCallback': function( playerId ){
        console.log( "kWidget player ready: " + playerId );
        var kdp = document.getElementById( playerId );
        kdp.addJsListener( 'doPlay', 'doPlayCallback');
    }
});
</script>
   * 
   * NOTE: the "kalturaPlayer" id will need to be generated dynamically if there are multiple players per page
   */
  var embedKcp = function(that) {
    // set container size
    var $container = $(that.container); // container to put the player inside
    $container.css('width', that.options.playerWidth);
    $container.css('height', that.options.playerHeight);

    if (!that.options.categoryId) {
      // try to find it in the page (null if not found)
      that.options.categoryId = $('#siteCategoryId').val();
    }

    if (that.options.useHtml5Player) {
      // html5 player enabled - html5 JS must be loaded somewhere
      // add in script call to activate the player (requires kaltura library)
      if (typeof kWidget === 'undefined') {
        try {
          var errorMessage = kaltura.i18n("i18n_error.missing.html5.library");
          if (errorMessage.indexOf("Unknown i18n key") > -1) { throw "Missing translation"; }
          alert(errorMessage);
        } catch (err) {
          // Default english message if the kaltura.i18n() function hasn't been loaded
          alert("HTML5 Kaltura Javacript library (kWidget) is missing"); 
        }
        // throw an exception if the kaltura library is not available
        throw { 
          name: "Missing global variable", 
          level: "FATAL", 
          message: "HTML5 Kaltura Javacript library (kWidget) is missing"
        };
      }
      // make sure the siteId is populated (if it is possible to populate it)
      if (!that.options.siteId) {
        that.options.siteId = kaltura.findSiteId(); // find or set it to null
      }
      var kWidgetArgs = {
          targetId: that.options.containerId, 
          wid: '_'+that.options.partnerId, 
          uiconf_id: that.options.playerId, 
          entry_id: that.options.entryId, 
          width: that.options.playerWidth, 
          height: that.options.playerHeight, 
          uid: that.options.username,
          pid: that.options.partnerId,
          params: {
            allowScriptAccess: "always",
            allowNetworking: "all",
            allowFullScreen: "true",
            wmode: "opaque"
          },
          flashvars: {
            entryId: that.options.entryId, 
            ks: that.options.sessionId, 
            externalInterfaceDisabled: false,
            autoPlay: false,
            applicationName: "Sakai"
          }
      };
      // add in the category id as context to flashvars if it is known (was that.options.siteId)
      if (that.options.categoryId) {
        kWidgetArgs.flashvars.playbackContext = that.options.categoryId;
      }
      kWidget.embed(kWidgetArgs);
    } else {
      // OLD flash method
      var params = {
          uid: that.options.username,
          pid: that.options.partnerId,
          allowscriptaccess: "always",
          allownetworking: "all",
          allowfullscreen: "true",
          wmode: "opaque"
      };
      var flashVars = {
          ks: that.options.sessionId,
          entryId: that.options.entryId,
          applicationName: "Sakai"
      };
      // add in the category id as context to flashvars if it is known (was that.options.siteId)
      if (that.options.categoryId) {
        flashVars.playbackContext = that.options.categoryId;
      }
      var url = that.options.playerURL;
      var playerWidth = that.options.playerWidth;
      var playerHeight = that.options.playerHeight;
      // create the flash player
      swfobject.embedSWF(url, that.options.containerId, playerWidth, playerHeight, "9.0.0", false, flashVars, params);
    }
  };

  var embedKcw = function(that) {
    var params = {
        allowScriptAccess: "always",
        allowNetworking: "all",
        wmode: "window" //"opaque"
    };
    var uploaderId = that.options.widgetUploaderId;
    if (that.options.uploadSpecialId) {
      // use the special uploader id if it is set
      uploaderId = that.options.uploadSpecialId;
    }
    var kalturaSession = that.options.sessionId;
    if (that.options.uploadSpecialKS) {
      // use the special uploader KS if it is set
      kalturaSession = that.options.uploadSpecialKS;
    }
    var flashVars = {
        uid: that.options.username,
        pid: that.options.partnerId,
        ks: kalturaSession,
        afterAddEntry: that.options.onAddEntry,
        close: that.options.onClose,
        showCloseButton: false,
        Permissions: 1
    };
    swfobject.embedSWF(that.options.endpoint + "/kcw/ui_conf_id/" + uploaderId, that.options.containerId, "680", "360", "9.0.0", "swf/expressInstall.swf", flashVars, params);
  };

  var embedKae = function(that) {

    var params = {
        allowScriptAccess: "always",
        allowNetworking: "all",
        wmode: "opaque"
    };
    var kalturaSession = that.options.sessionId;
    if (that.options.editKS) {
      // use the edit KS if it is set
      kalturaSession = that.options.editKS;
    }
    var flashVars = {
        uid: that.options.username,
        pid: that.options.partnerId,
        ks: kalturaSession,
        partner_id: that.options.partnerId,
        entryId: that.options.entryId,
        entryVersion: -1,
        kshowId: -1,
        subpId: that.options.partnerId + "00",
        jsDelegate: "kaltura.KAEcallbacks",
        debugMode: 0
    };

    swfobject.embedSWF(that.options.endpoint + "/kae/ui_conf_id/" + that.options.widgetEditorId, that.options.containerId, "825", "672", "9.0.0", false, flashVars, params);
  };


  /**
   * Attempts to find the site id from hints in the page or from the current url,
   * see http://jsfiddle.net/tnvKg/ for testing
   * @return string the Sakai siteId if it can be found OR null if it cannot be found
   */
  kaltura.findSiteId = function() {
    var result = null;
    var $body = jQuery(document.body);
    var locationId = $body.find("input#locationId").val();
    if (typeof locationId === "string" && locationId.length > 0) {
      result = locationId.substring(6);
    } else {
      locationId = $body.find("#locationId").text();
      if (typeof locationId === "string" && locationId.length > 0) {
        result = locationId.substring(6);
      } else {
        var siteId = $body.find("input#siteId").val();
        if (typeof siteId === "string" && siteId.length > 0) {
          result = siteId;
        } else {
          siteId = $body.find("#siteId").text();
          if (typeof siteId === "string" && siteId.length > 0) {
            result = siteId;
          } else {
            var url = window.location.href;
            // there might not be an opener or a parent so we have to be more flexible with our checks -AZ
            if (window.opener && window.opener.parent && window.opener.parent.location) {
              url = window.opener.parent.location.href;
            } else if (window.opener && window.opener.location) {
              url = window.opener.location.href;
            } else if (window.parent && window.parent.location) {
              url = window.parent.location.href;
            }
            //var reg = new RegExp(/\/site\/[a-z0-9\-]+/); // old regex would not properly match 
            /* This new regex is better for matching correctly according to actual site ids which are allowed
             * Examples:
             * http://nightly2.sakaiproject.org:8087/portal/site/dda71b95-d39b-4f90-896d-f5b8fa5b3bbb/page/0ca325eb-bc3f-4de4-866f-30c3eb235488
             * http://nightly2.sakaiproject.org:8087/portal/site/ab.12_x,-@z/page/0ca325eb-bc3f-4de4-866f-30c3eb235488
             * Matches the rules in the Validator class in kernel utils
             * SKE-222 also now matches user worksite site ids starting with %7E (ie ~)
             */
            var regex = new RegExp(/\/site\/((%7E)?[^\/\\{}\[\]\(\)%\*\?#&=\s]+)\/?/i);
            var matchArray = url.match(regex);
            if (matchArray && matchArray.length > 1) {
              // matched
              result = matchArray[1];
            }
          }
        }
      }
    }
    return result;
  };

  kaltura.KCP = function(container, options) {

    var that = fluid.initView("kaltura.KCP", container, options);

    if (that.options.populateKalturaInfo) {
      setKalturaInfo(that);
    }

    embedKcp(that);

    return that;
  };

  fluid.defaults("kaltura.KCP", {

    entryId: null,
    containerId: null,
    playerId: null,
    playerUrl: null,
    playerWidth: 480,
    playerHeight: 360,

    toolId: null,
    location: null,

    populateKalturaInfo: true,

    username: null,
    entryOwner: null,
    entryType: null,
    partnerId: null,
    sessionId: null,
    endpoint: null

  });


  kaltura.KCW = function(container, options) {

    var that = fluid.initView("kaltura.KCW", container, options);
    toolId = that.options.toolId;

    if (that.options.populateKalturaInfo) {
      setKalturaInfo(that);
    }

    embedKcw(that);

    return that;
  };

  fluid.defaults("kaltura.KCW", {

    containerId: null,

    onAddEntry: "addEntriesToSiteLibrary",
    onClose: "closeKalturaUploader",

    populateKalturaInfo: true,

    username: null,
    partnerId: null,
    sessionId: null,
    endpoint: null

  });

  kaltura.KAE = function(container, options) {

    var that = fluid.initView("kaltura.KAE", container, options);

    if (that.options.populateKalturaInfo) {
      setKalturaInfo(that);
    }

    embedKae(that);

    return that;
  };

  fluid.defaults("kaltura.KAE", {

    entryId: null,
    containerId: null,

    populateKalturaInfo: true,

    username: null,
    partnerId: null,
    sessionId: null,
    endpoint: null

  });

  fluid.defaults("kaltura.KCLP", {

    entryId: null,
    containerId: null,

    populateKalturaInfo: true,

    username: null,
    partnerId: null,
    sessionId: null,
    endpoint: null

  });

  /**
   * Special handler for callbacks from the KAE
   * http://www.kaltura.org/sites/default/files/AdvancedEditor%20Manuals.pdf
   */
  kaltura.KAEcallbacks = {
      publishHandler: function(data) {
        // nothing
      },
      closeHandler: function(data) {
        //alert("<= close editor => " + data.modified + ", " + data.saved);
        $("#editVideo").dialog("close");
      }    
  };

})(jQuery, fluid_1_3);

//GLOBAL

var addEntriesToSiteLibrary = function(entries) {
  var eids = [];
  jQuery(entries).each(function(idx, entry) {
    var eid = entry.entryId;
    if (undefined === eid) {
      /* this code block handles the kaltura practice of changing the name of the 
       * entryId when using my content in "unique" mode, this practice is inconsistent
       * with the way the kaltura uploader handles new items so this code corrects that
       * inconsistency, this became necessary in August 2010 as a result of a change 
       * made by kaltura to the KCW, 
       * if kaltura fixes this inconsistency in the future then we can remove this code
       * -AZ
       */
      eid = entry.uniqueID;
    }
    if (eid !== undefined) {
      eids.push(eid);
    } else {
      try {
        var errorMessage = kaltura.i18n("i18n_error.missing.entry.id");
        if (errorMessage.indexOf("Unknown i18n key") > -1) { throw "Missing translation"; }
        alert(errorMessage);
      }
      catch (err) {
        // Default english message if the kaltura.i18n() function hasn't been loaded
        alert("Invalid return from Kaltura widget, cannot find entry ID, please contact Kaltura Support");  
      }

    }
  });
  // passing a dummy value in the eid location, since the eids are now passed as part of a data structure.
  var collectionId = $('#collectionId').text(); // DO NOT CHANGE - pulls from a span in uploadMedia.jsp
  var url = '/direct/kaltura/libraryadd/_' + kaltura.getLocationRef();
  jQuery.ajax({
    type: 'POST',
    url: url,
    data: {eids: eids, collectionId: collectionId},
    async: false,
    dataType: 'text',
    error: function( jqXHR, textStatus, errorThrown ) {
      alert("Failure trying to add media ("+eids+") to library ("+url+") and collection ("+collectionId+"): "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
    }
  });
};
