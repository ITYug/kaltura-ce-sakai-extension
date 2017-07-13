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

//http://jshint.com/
/*global jQuery: true */
jQuery(function(){
  //"use strict";

  /*
   * look through each .kaltura-media container.  Promote anything that is not an image.  If
   * the object is before the image in the container, promote it before the container.  If the
   * object is after the image, promote it after the container.  Wrap embedded text and promote
   * like any other object.  Anything embedded in the container will be erased when the image
   * is converted to an iframe.
   */
  var promote = function(doc) {
    jQuery(doc).find(".kaltura-media").each(function() {
      var foundImage = false;
      for (var i = 0; i < this.childNodes.length; i++) {
        var child = this.childNodes[i];
        if (child.nodeName === "IMG") {
          // don't promote embedded image
          foundImage = true;
        } else if (child.nodeType === 3) {
          // text nodes only
          if (!foundImage) {
            jQuery("<span>" + child.textContent + "</span>").insertBefore(jQuery(this));
          } else {
            jQuery("<span>" + child.textContent + "</span>").insertAfter(jQuery(this));
          }
          jQuery(child).remove();
          i--; // account for childNodes getting smaller when object is removed
        } else {
          // all other nodes
          if (!foundImage) {
            jQuery(child).insertBefore(this);
          } else {
            jQuery(child).insertAfter(this);
          }
          i--;
        }
      }
    });
  };

  var displayKaltura = function(doc) {

    promote(doc);

    // remove div's without embedded images
    jQuery(doc).find(".kaltura-media:not(:has(img))").remove();

    // find kaltura placeholder divs and process each one
    jQuery(doc).find(".kaltura-media").each(function(idx, div){

      // handle the id and type (or just the id if no type is set)
      var relData = jQuery(this).attr("rel");
      var entryId = relData;
      var entryType = "video";
      if (relData.indexOf("::") > 0) {
        var parts = relData.split("::");
        if (parts.length > 0) {
          entryId = parts[0];
          if (parts.length > 1) {
            entryType = parts[1];
          }
        }
      }

      /* TODO handle images differently
            if (entryType === "image") {
                // TODO do something here
            } else {
                // TODO handle the video/audio stuff here
            }
       */

      // replace the contents of the placeholder div with an IFrame view of the content
      // NOTE: it would be better to resize the iframe from within (on viewMedia page) when this is rendered so there are no extra borders
      jQuery(div).html('<iframe src="/kaltura/service/viewMedia.htm?entryId=' + entryId + '&entryType=' + entryType + '" height="370" width="500" frameBorder="0" />');

    });
  };

  var findIframes = function(doc) {

    // find each IFrame in the current document
    jQuery(doc).find("iframe").each(function(idx, iframe) {
      try {
        // as each IFrame loads, process it
        jQuery(iframe).load( function(){

          // get the document associated with this IFrame
          var window = this.contentDocument || this.contentWindow.document;

          // display any Kaltura content within this IFrame 
          displayKaltura(window);

          // process any nested IFrames
          findIframes(window);
        });
      }
      catch (err) {
          // Nothing we can do but swallow the error.
      }
    });
  };

  // on page load, find and process Kaltura media placeholders in each nested IFrame
  jQuery(document).ready(function(){
    findIframes(this);
  });

});
