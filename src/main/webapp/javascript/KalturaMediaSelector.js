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
/*global fluid_1_3:true, jquery:true, kaltura:true, closeKalturaUploader:true, alert:true, confirm:true, Exception:true */
var fluid_1_3 = fluid_1_3 || {}; //use current released version number
var kaltura = kaltura || {};
(function($, fluid) {
  /**
   * Index media list items by kaltura ID and media ID
   * NOTE: indexing by kalturaId is not really a good idea -AZ
   */
  var indexData = function(data) {
    $(data).each(function(idx,item){
      data[item.kalturaId] = data[idx];
      data["mid-"+item.idStr] = data[idx];
    });
  };

  /**
   * Assigns classes to the LI in the listing of items
   */
  var getClasses = function(item) {
    var classes = " id-" + item.idStr + " type-" + item.mediaType + " pos-" + item.position;
    if (item.hidden) { classes += " hidden"; }
    if (item.shared) { classes += " shared"; }
    if (item.remixable) { classes += " remixable"; }
    if (item.owned) { classes += " owned"; }
    if (item.created) { classes += " created"; }
    if (item.control) { classes += " control"; }
    if (item.edit) { classes += " edit"; }
    if (item.manage) { classes += " manage"; }
    if (item.mix) { classes += " mixed"; }
    return classes;
  };

  var getItems = function(that) {
    $.ajax({
      url: that.options.collectionUrl,
      async: false,
      cache: false,
      type: "GET",
      dataType: "json",
      success: function(data){ 
        that.state.mediaItems = data.items || data;
        that.state.itemCount = that.state.mediaItems.length;
      },
      error: function( jqXHR, textStatus, errorThrown ) {
        alert( "Get items failed for ("+that.options.collectionUrl+"): "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
      }
    });
    indexData(that.state.mediaItems);
    // return a copy of the data rather than the actual data
    return fluid.copy(that.state.mediaItems);
  };

  /**
   * Determine if a given media item matches our search term
   */
  var itemMatches = function(item, searchTerm) {

    searchTerm = searchTerm.toLowerCase();

    // if the item's title includes our search term
    var title = item.name;
    if (title && title.toLowerCase().indexOf(searchTerm) >= 0) {
      return true;
    }

    // if the item's description includes our search term
    if (item.desc && item.desc.toLowerCase().indexOf(searchTerm) >= 0) {
      return true;
    }

    // if the item's creator includes our search term
    if (item.author && item.author.name && item.author.name.toLowerCase().indexOf(searchTerm) >= 0) {
      return true;
    }

    // if any of the item's tags include our search term
    if (item.tags && item.tags.join(" ").toLowerCase().indexOf(searchTerm) >= 0) {
      return true;
    }

    // otherwise just return false
    return false;
  };

  /**
   * Filter the currently-loaded media items according to the specified
   * search term.
   */
  var filterItems = function(that) {

    var searchTerm = that.state.searchTerm;
    var mediaType = that.state.mediaType;

    // if the search term is null, just return a copy of the original list of media items
    if (!searchTerm && !mediaType) {
      return fluid.copy(that.state.mediaItems);
    }

    // construct a new list to hold the filtered items
    var filtered = [];

    // test each item against our filter and add matching items to the list 
    $(that.state.mediaItems).each( function(idx, item) {
      if ((!mediaType || item.mediaType === mediaType) && (!searchTerm || itemMatches(item, searchTerm))) {
        filtered.push(item);
        filtered[item.kalturaId] = filtered[filtered.length-1];
      }
    });

    return filtered;
  };


  /**
   * KalturaMediaSelector displays Kaltura media items and allows filtering
   * them by a search term.  This component requires a top-level option named
   * "mediaItems", representing the unfiltered list of media items to be
   * displayed.
   */
  kaltura.KalturaMediaSelector = function(container, options) {

    var that = fluid.initView("kaltura.KalturaMediaSelector", container, options);

    that.state = {};
    var data = null; // init here for re-use (SKE-162)

    /**
     * Render the media items, potentially filtering based on a supplied search term
     */
    that.refresh = function(searchTerm, mediaType) {

      /* if search terms are not supplied at all 
       * then attempt to find the stored search (assuming that we are just refreshing and not submitting a search),
       * if they are set, then we will assume it is a search and not just a simple refresh
       * of the listing
       */
      if (typeof searchTerm === "undefined") {
        // this is a refresh
        searchTerm = that.state.searchTerm;
      } else if (!searchTerm) {
        searchTerm = false;
      }
      if (typeof mediaType === "undefined") {
        // this is a refresh
        mediaType = that.state.mediaType;
      } else if (!mediaType) {
        mediaType = false;
      }
      //if (typeof clearData === "undefined" || clearData === null) { clearData = true; } // default to clear the data

      // have to re-index on each refresh (it should not cause a problem)
      indexData(that.state.mediaItems);

      // update the search term display
      if (! searchTerm) {
        $("#searchForm_searchText").val("");
      } else {
        $("#searchForm_searchText").val(searchTerm);
      }

      that.state.searchTerm = searchTerm;
      that.state.mediaType = mediaType;

      data = filterItems(that); // filters whatever is in that.state.mediaItems
      var pager = that.pager;

      // refresh the pager
      var newModel = fluid.copy(pager.model);
      newModel.totalRange = data.length;
      newModel.sortKey = that.state.sortKey;
      newModel.sortDir = that.state.sortDir;
      newModel.pageIndex = 0;
      newModel.pageCount = Math.max(1, Math.floor((newModel.totalRange - 1) / newModel.pageSize) + 1);
      // copyModel copies RHS to LHS and clears LHS first
      fluid.clear(pager.options.dataModel);
      fluid.model.copyModel(pager.options.dataModel, data);
      pager.permutation = undefined;
      fluid.model.copyModel(pager.model, newModel);
      // do the ACTUAL refresh of the pager
      pager.events.onModelChange.fire(newModel, pager.model, pager);

      if (data.length < 1){
        that.locate("noItemsMessage").show();
      } else {
        that.locate("noItemsMessage").hide();
      }
      // update the items count
      $("#pager_itemsCount").text(data.length);
    };

    /* NOTE from fluid dev:
        Antranig:  It's unclear whether something from the outside calls refresh on startup. Which might overwrite that
        Otherwise, you can try updating the model after startup, using lines like the ones you see inside the refresh() method
        fluid.model.copyModel(that.pager.model, newModel);
        that.pager.events.onModelChange.fire(newModel, that.pager.model, that.pager);
     */

    var itemsData = getItems(that); // fetch items in that.state.mediaItems and return a copy
    var pagerOptions = {
        dataModel: itemsData,
        model: {
          pageSize: 24
        },
        annotateColumnRange: null, // required for fluid 1.3.1
        columnDefs: [
                     { 
                       key: "media-item", 
                       valuebinding: "*.id",
                       components: function(item, idx) {
                         return {
                           decorators: [
                                        { attrs: { 
                                          kalturaid: item.kalturaId, 
                                          mediaid: String(item.idStr) // all attrs MUST be strings - fluid 1.3.1
                                        } 
                                        },
                                        { type: "jQuery", func: "hover", 
                                          args: [
                                                 function(){ $(this).find(".actions").removeClass("hide"); },
                                                 function(){ $(this).find(".actions").addClass("hide"); } 
                                                 ]
                                        },
                                        { type: "addClass", classes: getClasses(item) }
                                        ]
                         };
                       }
                     },
                     { 
                       key: "thumbnail-title-link", 
                       valuebinding: "*.name", 
                       sortable: true,
                       components: function(item, idx) {
                         return {
                           linktext: item.shortName, //"\${*.name}",
                           target: "#", //"javascript:;",
                           decorators: [
                                        { type: "jQuery", func: "click", 
                                          args: function(){ 
                                            that.events.onTitleSelect.fire(that.state.mediaItems["mid-"+$(this).parents(".media-item").attr("mediaid")], this); 
                                          } 
                                        }
                                        ]
                         };
                       }
                     },
                     { 
                       key: "thumbnail-image", 
                       valuebinding: "*.thumbnail", 
                       components: function(item, idx) {
                         return {
                           decorators: [
                                        { attrs: { src: item.thumbnail } }
                                        ]
                         };
                       }
                     },
                     { 
                       key: "thumbnail-image-link", 
                       valuebinding: "*.thumbnail", 
                       components: function(item, idx) {
                         return {
                           decorators: [
                                        { attrs: { title: item.name } },
                                        { type: "jQuery", func: "click", 
                                          args: function() {
                                            that.events.onThumbnailSelect.fire(that.state.mediaItems["mid-"+$(this).parents(".media-item").attr("mediaid")], this);
                                          } 
                                        }
                                        ]
                         };
                       }
                     },
                     { 
                       key: "item-details-link", 
                       valuebinding: "*.id", 
                       components: function(item, idx) {
                         return {
                           decorators: [
                                        { type: "jQuery", func: "click", 
                                          args: function(){ 
                                            that.events.onDetailsSelect.fire(that.state.mediaItems["mid-"+$(this).parents(".media-item").attr("mediaid")], this); 
                                          } 
                                        }
                                        ]
                         };
                       }
                     },
                     { 
                       key: "item-add-link", 
                       valuebinding: "*.id", 
                       components: function(item, idx) {
                         return {
                           decorators: [
                                        { type: "jQuery", func: "click", 
                                          args: function(){ 
                                            that.events.onAddSelect.fire(that.state.mediaItems["mid-"+$(this).parents(".media-item").attr("mediaid")], this); 
                                          } 
                                        }
                                        ]
                         };
                       }
                     },
                     { 
                       key: "item-edit-link", 
                       valuebinding: "*.id",
                       components: function(item, idx) {
                         return {
                           decorators: [
                                        { type: "jQuery", func: "click", 
                                          args: function(){
                                            that.events.onEditSelect.fire(that.state.mediaItems["mid-"+$(this).parents(".media-item").attr("mediaid")], this); 
                                          } 
                                        }
                                        ]
                         };
                       }
                     },
                     { 
                       key: "item-remove-link", 
                       valuebinding: "*.id",
                       components: function(item, idx) {
                         return {
                           decorators: [
                                        { type: "jQuery", func: "click", 
                                          args: function(){ 
                                            that.events.onRemoveSelect.fire(that.state.mediaItems["mid-"+$(this).parents(".media-item").attr("mediaid")], this); 
                                          } 
                                        }
                                        ]
                         };
                       }
                     },
                     { 
                       key: "itemTitle", 
                       valuebinding: "*.name" 
                     },
                     { 
                       key: "item-details-date", 
                       valuebinding: "*.dateCreated",
                       components: function(row, index) {
                         return { value: new Date(row.dateCreated) }; 
                       }
                     }
                     ],
                     bodyRenderer: {
                       type: "fluid.pager.selfRender",
                       options: {
                         selectors: {
                           root: ".pager-body"
                         },
                         row: "item:"
                       }
                     },
                     pagerBar: {
                       type: "fluid.pager.pagerBar", options: {
                         pageList: { 
                           type: "fluid.pager.renderedPageList",
                           options: { 
                             linkBody: "a"
                           }
                         }
                       }
                     }
    };


    try {
      that.pager = fluid.pager(container, pagerOptions);
    } catch (err) {
      alert("pager failed to init: "+err.message);
    }

    $(that.locate("libraryCount")).text( that.state.itemCount );

    if (that.state.itemCount < 1){
      that.locate("noItemsMessage").show();
    } else {
      that.locate("noItemsMessage").hide();
    }

    $(that.locate("searchForm")).submit(function(){
      var form = this;
      that.refresh(form.searchText.value, form.mediaType.value);
      // if media-viewer is present (in view collection) (SKE-168)
      if ($(".media-viewer")[0]) {
        if (data.length > 0) { // reload it with the first item, if one exists (SKE-162)
          initializeFocusedView(data[0], true);
        } else { // no items in this collection - hide the viewer
          $(".media-viewer").hide();
        }
      } else { // if no items exist after search and in edit collection media, hide no items heading
        $(".note.empty.no_items").hide();
      }
      return false;
    });

    return that;
  };

  fluid.defaults("kaltura.KalturaMediaSelector", {

    collectionUrl: null,

    // selectors
    selectors: {
      mediaList: ".media-list",
      mediaItem: ".media-item",
      thumbnailImage: ".thumbnail-image",
      thumbnailTitle: ".thumbnail-title-link",
      itemDetailsTitle: ".item-details-title",
      itemDetailsDate: ".item-details-creation-date",
      itemEditLink: ".edit-details",
      itemRemoveLink: ".item-remove-link",
      itemDetailsLink: ".info",
      noItemsMessage: ".no_items",
      libraryCount: ".library-count",
      searchForm: ".search-form"
    },

    // events
    events: {
      onThumnailSelect: null,
      onTitleSelect: null,
      onEditSelect: null,
      onRemoveSelect: null,
      onAddSelect: null,
      onDetailsSelect: null
    }

  });

  // end of defaults

  /**
   * Convenience method for constructing the initial editCollectionMedia view JS
   * @param currentCollectionId the id of the current collection
   * @param currentToolId the id of the current tool
   */
  kaltura.initEditCollectionMedia = function(currentCollectionId, currentToolId) {
    kaltura.editCollectionMedia(currentCollectionId, currentToolId, true, null);
  };

  /**
   * Init the editCollectionMedia view JS
   * @param currentCollectionId the id of the current collection
   * @param currentToolId the id of the current tool
   * @param isMyMedia is the my media area selected, true on initialization
   * @param collSelector the collection selector, null on initialization
   */
  kaltura.editCollectionMedia = function(currentCollectionId, currentToolId, isMyMedia, collSelector) {
    var collectionSelector = collSelector;
    // get the original HTML for replacing upon switching between my media and site library 
    var mediaTemplateHtml = $(".site-library .media-browser").html();

    function setMediaSourceUrl() {
      return "/direct/kaltura/library/" + (isMyMedia ? "site/isMyMedia" : "tool/" + currentToolId) + ".json?timestamp=" + new Date().getTime();
    }

    $(".media-item").hover(function() {
      $(this).find(".action-marker").toggleClass("make-hidden");
    });

    $(".media-source-link a").click(function() {
        if (isMyMedia) {
          $(".mymedia-active").addClass("hide");
          $(".sitelibrary-active").removeClass("hide");
        } else {
          $(".mymedia-active").removeClass("hide");
          $(".sitelibrary-active").addClass("hide");
        }
        $(".site-library .media-browser").html(mediaTemplateHtml);
        kaltura.editCollectionMedia(currentCollectionId, currentToolId, !isMyMedia, collectionSelector);
        return false;
    });

    /*
     * Currently not used
     * Replaced by updateCollection (below)
    var removeItem = function(item, clickedOn) {
      if (! item.control) {
        // cannot remove without item control permission
        return false;
      }

      // mark the item as being removed with a spinner and overlay
      var $clickedLI = $(clickedOn).closest('li.media_item'); // get the container
      $clickedLI.mask(".loading-overlay-small");

      var ajaxKey = kaltura.ajaxStart();
      $.ajax({
        url: "/direct/kaltura/remove/" + item.idStr + "/coll/" + currentCollectionId,
        type: "POST",
        //async: false, // this has to by synchronous until kaltura has atomic remove
        dataType: "text",
        success: function() {
          kaltura.removeItemFromPager(collectionSelector, item.id); // PAGER REFRESH
        },
        error: function( jqXHR, textStatus, errorThrown ) {
          alert( "Delete failed for "+item.name+" ("+item.idStr+"): "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
          // remove the overlay and switch item back to normal
          $clickedLI.unmask();
        },
        complete: function() {
          kaltura.ajaxDone(ajaxKey);
        }
      });
    };
    */

    var updateCollection = function(item, clickedOn, isRemove) {
      if (!item) {
        alert("item is not set in updateCollection: "+clickedOn);
        return; // have to exit here
      }
      if (!item.control) {
        // cannot add/remove without item control permission
        return false;
      }

      if (typeof isRemove === 'undefined' || isRemove === null) {
        // if isRemove is not set, then update is called from a remove operation
        isRemove = true;
      }

      var $clickedItem;
      if (isRemove) { // if removing an item
        /* 1. Put 'removing' flag on item
         * 2. Locate the item element in the pager AND put a mask overlay on it
         * 3. If success, remove the item from the pager
         * 4. If failure, remove the mask from the element AND remove 'removing' flag from the item
         */
        item.removing = true; // set the removing flag
        $clickedItem = $(clickedOn).closest('li.media_item'); // get the container
      } else { // if adding an item
        /* 1. Copy the item, put 'adding' flag on it, and add it to the pager immediately
         * 2. Locate the item element in the pager AND put a mask overlay on it
         * 3. If success, remove the mask from the element AND remove 'adding' flag from the item
         * 4. If failure, remove the item from the pager
        */
        // copy the item
        var itemCopy = {};
        for (var prop in item) {
          if (item.hasOwnProperty(prop)) {
            itemCopy[prop] = item[prop];
          }
        }
        itemCopy.edit = true;
        itemCopy.hidden = false;
        itemCopy.adding = true; // set the adding flag
        // add copy to the pager
        collectionSelector.state.mediaItems.push(itemCopy);
        collectionSelector.refresh(); // PAGER REFRESH
        item = itemCopy; // replace the item with the copy we made (so item refers to what was added and not the item from the other pager)
        // add spinners back to all items being added or removed
        kaltura.addSpinnersInPager(collectionSelector, "#collection_pager");
        $clickedItem = $("#collection_pager").find("li.id-"+item.id);
      }
      // mask the item
      $clickedItem.mask(".loading-overlay-small");

      // get all Kaltura media IDs in collection
      var mediaIds = [];
      for (var i = 0; i < collectionSelector.state.mediaItems.length; i++) {
        if (collectionSelector.state.mediaItems[i]) {
          if (collectionSelector.state.mediaItems[i].hasOwnProperty('removing')) {
            continue; // skip any item we are removing
          }
          // store all other items
          mediaIds.push(collectionSelector.state.mediaItems[i].id);
        }
      }
      // have to add this since an empty array means no "ids" get sent at all
      if (mediaIds.length === 0) {
        mediaIds = ""; // empty string is going to indicate we mean remove them
      }

      var ajaxKey = kaltura.ajaxStart();
      $.ajax({
        url: "/direct/kaltura/" + currentCollectionId + "/replace.json",
        type: "POST",
        dataType: "json",
        data: {ids: mediaIds},
        success: function(data) {
          // returned data is an array of the items in the collection
          if (isRemove) {
            delete item.removing; // take off the removing flag
            if (kaltura.removeItemFromPager(collectionSelector, item.id)) { // PAGER REFRESH
              // add spinners back to all items being added or removed
              kaltura.addSpinnersInPager(collectionSelector, "#collection_pager");
            }
          } else {
            delete item.adding; // take off the adding flag
            // because of the pager refresh - the original element is no longer valid, have to find it again
            $("#collection_pager").find("li.id-"+item.id).unmask(); // remove spinner
          }

          // check if items returned are in collection. if not, add them (this is just a failsafe)
          if (data instanceof Array && data.length > 0) {
            var addedItem = false;
            for (var i = 0; i < data.length; i++) {
              if (data[i].kalturaId && ($.inArray(data[i].kalturaId, mediaIds) < 0)) {
                collectionSelector.state.mediaItems.push(data[i]);
                addedItem = true;
              }
            }
            if (addedItem) {
              // found items that had to be added, need to refresh the whole collection
              collectionSelector.refresh(); // PAGER REFRESH
              // refresh kills spinner and existing elements, add spinners back to all items being added or removed
              kaltura.addSpinnersInPager(collectionSelector, "#collection_pager");
            }
          }
        },
        error: function( jqXHR, textStatus, errorThrown ) {
          alert( "Error updating collection: "+item.name+" ("+item.idStr+"): "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
          if (isRemove) {
            delete item.removing; // take off the removing flag
            // because of the pager refresh - the original element is no longer valid, have to find it again
            $("#collection_pager").find("li.id-"+item.id).unmask();
          } else {
            delete item.adding; // take off the adding flag (not really needed but for completeness)
            // take item back out if we were trying to add it
            if (kaltura.removeItemFromPager(collectionSelector, item.id)) { // PAGER REFRESH
              // add spinners back to all items being added or removed
              kaltura.addSpinnersInPager(collectionSelector, "#collection_pager");
            }
          }
        },
        complete: function() {
          kaltura.ajaxDone(ajaxKey);
        }
      });
    };

    kaltura.KalturaMediaSelector(
      $(".site-library .media-browser"),
      {
        collectionUrl: setMediaSourceUrl(),
        listeners: {
          onThumbnailSelect: function(item) {
            // if item does not exist in collection add it, otherwise alert user
            if (! collectionSelector.state.mediaItems[item.id] ) {
              updateCollection(item, null, false);
              /* 1. Copy the item and add it to the pager immediately
               * 2. Locate the item element in the pager AND put a mask overlay on it
               * 3. If success, remove the mask from the element
               * 4. If failure, remove the item from the pager
               */
              /* replaced by updateCollection()
              // copy the item
              var itemCopy = {};
              for (var prop in item) {
                if (item.hasOwnProperty(prop)) {
                  itemCopy[prop] = item[prop];
                }
              }
              itemCopy.edit = true;
              itemCopy.hidden = false;
              // add copy to the pager
              collectionSelector.state.mediaItems.push(itemCopy);
              collectionSelector.refresh(); // PAGER REFRESH - HAVE to add spinners back in when refreshing
              // mask the added item
              var $addedItem = $("#collection_pager").find("li.id-"+item.id);
              $addedItem.mask(".loading-overlay-small");

              $.ajax({
                url: "/direct/kaltura/" + currentCollectionId + "/add/" + item.idStr + ".json",
                type: "POST",
                //async: false, // this has to by synchronous until kaltura has atomic adds
                dataType: "json",
                data: { _method: "PUT" },
                success: function(data) {
                  //collectionSelector.state.mediaItems.push(data);
                  //collectionSelector.refresh(); // PAGER REFRESH
                  $addedItem.unmask();
                },
                error: function( jqXHR, textStatus, errorThrown ) {
                  alert( "FAILURE: Add item for "+item.name+" ("+item.idStr+"): "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
                  kaltura.removeItemFromPager(collectionSelector, item.id); // PAGER REFRESH
                },
                complete: function() {
                  kaltura.ajaxDone(ajaxKey);
                }
              });
              */
            } else {
              var message = kaltura.i18n("i18n_error.item.exists", [item.name, item.idStr]);
              if (message.indexOf("Unknown i18n key") > -1) {
                message = "The media item \""+item.name+"\" ("+item.idStr+") already exists in this collection.";
              }
              alert(message);
            }
          }
        }
      }
    );

    if (collectionSelector === null) {
      collectionSelector = kaltura.KalturaMediaSelector(
        $(".collection-media .media-browser"),
        {
          collectionUrl: "/direct/kaltura/" + currentCollectionId + ".json?populate=true&timestamp=" + new Date().getTime(),
          listeners: {
            onThumbnailSelect: updateCollection, // was removeItem
            onTitleSelect: updateCollection // was removeItem
          }
        }
      );
    }
  }; // END editCollectionMedia


  // TODO I don't think this is doing what you think it is doing, "this" is going to refer to whatever calls this method -AZ
  kaltura.closeEditMediaDialog = function(){
    $(this).dialog("close");
  };

  // init here for re-use (SKE-162)
  var initializeFocusedView = null;

  /**
   * Init the viewCollection view JS
   * @param currentCollectionId
   * @param currentItemId
   * @param currentLocationId
   * @param isCollection
   * @param isAdmin
   * @param showDeleteItemWarnings
   * @param isMyMedia
   * @param clippingEnabled
   */
  kaltura.initViewCollection = function(currentCollectionId, currentItemId, currentLocationId, isCollection, isAdmin, 
                                        showDeleteItemWarnings, isMyMedia, clippingEnabled) {
    isCollection = !!isCollection;
    isAdmin = !!isAdmin;
    isMyMedia = !!isMyMedia;
    var isLibrary = (!isMyMedia && !isCollection);

    // when the page size changes, redraw the iFrame.  Small delay to ensure
    // that the fluid page builder can complete before the loader tries to 
    // recalculate the iFrame height
    $(".flc-pager-page-size").change(function() {
      setTimeout(function() {$(window).trigger('load');}, 10);
    });

    var mediaItems = null;
    var mediaSelector = null;

    // embed code vars: {K_ENTRY_ID}, {K_HEIGHT}, {K_WIDTH}, {K_SRC_URL}, {K_FLASH_VARS}
    var embedCode = '<object id="kaltura_player_{K_ENTRY_ID}" name="kaltura_player_{K_ENTRY_ID}" type="application/x-shockwave-flash" allowFullScreen="true" allowNetworking="all" allowScriptAccess="always" height="{K_HEIGHT}" width="{K_WIDTH}" bgcolor="#000000" xmlns:dc="http://purl.org/dc/terms/" xmlns:media="http://search.yahoo.com/searchmonkey/media/" rel="media:video" resource="{K_SRC_URL}" data="{K_SRC_URL}"><param name="allowFullScreen" value="true" /><param name="allowNetworking" value="all" /><param name="allowScriptAccess" value="always" /><param name="bgcolor" value="#000000" /><param name="flashVars" value="&{K_FLASH_VARS}" /><param name="movie" value="{K_SRC_URL}" /></object>';

    var removeItem = function(){}; // have to define this up here

    var launchEditDialog = function(item, editingCurrentItem) {
      var form = $("#itemDetails").get(0);
      // set the form values
      form.title.value = item.name;
      form.desc.value = item.desc;
      form.tags.value = item.tags.join(", ");
      form["public"].checked = ! item.hidden; // invert
      form.shared.checked = item.shared;
      form.remixable.checked = item.remixable;

      var $form = $(form); // make jquery form object
      // store the item id
      $form.find(".kaltura-item-id").val(item.idStr);
      $form.find(".kaltura-id").val(item.kalturaId);
      // ensure only edit can change the meta data values
      if (item.edit) {
        $form.find(".kaltura-item-edit input").removeAttr('disabled');
      } else {
        $form.find(".kaltura-item-edit input").attr('disabled', 'disabled');
      }
      // ensure only item managers see the controls
      if (item.manage) {
        $form.find(".kaltura-managed").show();
      } else {
        // cannot edit perms
        $form.find(".kaltura-managed").hide();
      }
      // ensure remix/clip only appears for video and mixes
      if (item.mix || item.mediaType === "video") {
        $form.find(".kaltura-remix-control").show();
      } else {
        $form.find(".kaltura-remix-control").hide();
      }
      form.currentItem.value = editingCurrentItem ? "true" : "false";
      $("#editDetails").dialog("open");
    };

    var launchAddDialog = function(item) {
      var $addMedia = $("#addMedia");
      $addMedia.dialog("open");
      var form = $addMedia.get(0);
      var $form = $(form);
      $form.find("#kalturaId").val(item.kalturaId);
      //$("#addMedia").dialog("open");
      //$form.find("#addMedia").css("width", "550px");
      $addMedia.css("width", "472px");
      buildTargetCollectionList(item); // async
    };

    /* Make ajax call to the backend to get the list of collections that we can target the media for and their state
     * (whether we can add to the collection, whether it already has the media, etc)
     */
    var buildTargetCollectionList = function(item) {
      var $container = $($("#addMedia").get(0));
      $container.mask(".loading-overlay-medium");
      var locationId = $("#locationId").val(); // e.g. /site/3674fc9f-8d5c-4ede-86f0-2f7432edb0db
      // GET /kaltura/mymediatargets/{kalturaId}/site/{siteId}
      var restURL = "/direct/kaltura/mymediatargets/"+item.kalturaId+locationId+".json";
      $.ajax({
        url: restURL,
        //async: false,
        cache: false,
        type: "GET",
        dataType: "json",
        success : function(collections) {
          // Clear the select and display for the non-available collections
          var $targetCollections = $container.find("#targetCollections");
          $targetCollections.empty();
          $container.find("#cannotAddToCollectionList").empty();
          // Now populate with new data
          var $cannotAddToCollectionList = $container.find("#cannotAddToCollectionList");
          var locationId = $container.find("#mediaLocationId").val();
          $.each(collections, function(index) {
            var myMediaCollectionItem = collections[index];
            // The first item may be the site library. If it is and we can add to it, it goes into the select first
            // Anything that already has the media item (or we cannot write to) will be displayed in a separate list.
            if (myMediaCollectionItem.id === locationId && myMediaCollectionItem.userHasWriteAccess) {
              if (!myMediaCollectionItem.containsMediaItem) {
                $targetCollections.append( new Option(myMediaCollectionItem.name, myMediaCollectionItem.id) );
              } else {
                $cannotAddToCollectionList.append("<li>" + myMediaCollectionItem.name +"</li>");
              }
              return true; // break from the loop
            }
            // Subsequent items will be collections and so we add an optgroup for those.
            var $availableCollections = $container.find("#availableCollections");
            if ($availableCollections.length === 0) {
              $targetCollections.append("<optgroup id='availableCollections' label='Collections'></optgroup>");
              // need to look this up again since it will be based on the inserted optgroup in the live above
              $availableCollections = $container.find("#availableCollections");
            }
            if (!myMediaCollectionItem.containsMediaItem) {
              var option = new Option(myMediaCollectionItem.name, "/collection/"+myMediaCollectionItem.id);
              if (myMediaCollectionItem.userHasWriteAccess) {
                $availableCollections.append(option);
              }
            } else {
              // this collection already contains this item
              $cannotAddToCollectionList.append("<li title='" + myMediaCollectionItem.name + "'>" + myMediaCollectionItem.shortName +"</li>");
            }
          });
          if ($targetCollections.find("option").size() === 0) {
            $container.find("#noAvailableCollectionsMessageDiv").show();
            $container.find("#availableCollectionsMessageDiv").hide();
            $targetCollections.attr('disabled','disabled');
            $container.find("button:contains('Add')").attr('disabled','disabled');
          } else {
            $container.find("#noAvailableCollectionsMessageDiv").hide();
            $container.find("#availableCollectionsMessageDiv").show();
            $targetCollections.removeAttr('disabled');
            $container.find("button:contains('Add')").removeAttr('disabled');
          }
        },
        error: function(jqXHR, textStatus, errorThrown) {
          alert("Failure trying to get collections for my media item: "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText);
        },
        complete: function() {
          $container.unmask();
        }
      });
    };

    initializeFocusedView = function(item, loadPlayer) {
      // switch to loading view
      var $media_viewer = $(".media-viewer");
      var $media_viewer_loading = $(".media-viewer-loading");
      $media_viewer.hide();
      $media_viewer_loading.show();

      // make this item the current one
      currentItemId = item.idStr;

      if (loadPlayer === true) {
        // load up a new player
        kaltura.KCP(".kaltura-player", 
            {
          entryId: item.kalturaId,
          entryOwner: item.ownerId,
          entryType: item.mediaType,
          playerId: item.playerId,
          playerURL: item.userPlayerURL,
          playerWidth: item.playerWidth,
          playerHeight: item.playerHeight,
          useHtml5Player: item.useHtml5Player,
          containerId: "kplayerId"
            }
        );
      }

      // update the media data and fix up the links
      $media_viewer.find(".current-item-title").html(item.name);
      if (item.desc.length === 0) {
        $media_viewer.find(".item-desc").html("none");
      } else {
        $media_viewer.find(".item-desc").html(item.desc);
      }
      var tagsHtml = item.tags.join(", ");
      if (mediaSelector) {
        var $itemTags = $media_viewer.find(".item-tags");
        $itemTags.find(".item-tag").unbind("click.itemView");
        tagsHtml = "";
        if (item.tags.length === 0) {
          tagsHtml = "none";
        } else {
          for (var i=0; i < item.tags.length; i++) {
            if (i !== 0) {
              tagsHtml += ', ';
            }
            tagsHtml += '<a href="#'+item.tags[i]+'" class="item-tag">'+item.tags[i]+'</a>';
          }
        }
        $itemTags.html(tagsHtml);
        $itemTags.find(".item-tag").bind("click.itemView", function(){
          var $this = $(this);
          var tag = $this.html();
          mediaSelector.refresh(tag);
        });
      }
      $media_viewer.find(".creation-date").html((new Date(item.dateCreated)).toLocaleString());
      $media_viewer.find(".author-name").html((item.author ? item.author.name : ""));
      var $media_actions = $media_viewer.find(".actions");

      // handle the permissions
      // clipping
      var $editVideo = $media_actions.find(".edit-video");
      // NOTE: this if statement logic should match the logic in MediaItem.isCanRemix()
      if (clippingEnabled && ((item.created || item.remixable) && (item.control || item.edit)) && item.mediaType === "video") {
        $editVideo.attr("href", "kalClip.htm?&collectionId=" + currentCollectionId + "&entryId=" + item.kalturaId + "&isMyMedia=" + isMyMedia);
        $editVideo.parent().show();
      } else {
        $editVideo.parent().hide();
      }
      // downloads
      if (item.mix || !item.downloadURL) {
        // mixes not allowed to download items (also if download url is not set then same deal)
        $media_actions.find(".download").parent().hide();
      } else {
        if (isAdmin || item.owned) {
          // allowed to download
          var $action = $media_actions.find(".download");
          $action.attr("href", item.downloadURL);
          $action.parent().show();
        } else {
          $media_actions.find(".download").parent().hide();
        }
      }
      // embeds
      $("#kplayerEmbedArea").hide(); // reset to hiding the embed code (necessary in case user selects a different item)
      $("#kplayerEmbedButton").unbind("click.itemView"); // cleanup previous click events
      if (isAdmin) {
        // place embed code in a text area when user clicks a button
        $("#kplayerEmbedButton").bind("click.itemView", function(){
          var $embedArea = $("#kplayerEmbedArea");
          if ($embedArea.is(":visible")) {
            $embedArea.hide();
            $("#kplayerEmbed").text(""); // clear out the embed code
          } else {
            // NOTE: changed this by request of kaltura
            //$("#kplayerEmbed").text( $(".kaltura-player").html() );
            // {K_ENTRY_ID}, {K_HEIGHT}, {K_WIDTH}, {K_SRC_URL}, {K_FLASH_VARS}
            var playerEmbedCode = embedCode.replace(/\{K_ENTRY_ID\}/g, item.kalturaId);
            playerEmbedCode = playerEmbedCode.replace(/\{K_HEIGHT\}/g, item.playerHeight);
            playerEmbedCode = playerEmbedCode.replace(/\{K_WIDTH\}/g, item.playerWidth);
            playerEmbedCode = playerEmbedCode.replace(/\{K_SRC_URL\}/g, item.playerURL);
            playerEmbedCode = playerEmbedCode.replace(/\{K_FLASH_VARS\}/g, "entryId="+item.kalturaId);
            $("#kplayerEmbed").text( playerEmbedCode );
            $embedArea.show();
          }
        });
        $media_actions.find(".embed").parent().show();
      } else {
        $media_actions.find(".embed").parent().hide();
      }
      // edit-item-details
      var $editAction = $media_actions.find(".edit-item-details");
      $editAction.unbind("click.itemView"); // cleanup previous click events
      if (item.manage || item.edit) {
        $editAction.bind("click.itemView", function(){
          launchEditDialog(item, true);
        });
        $editAction.parent().show();
      } else {
        $editAction.parent().hide();
      }
      if (isMyMedia || isLibrary) {
        // My Media add-media
        var $addMediaAction = $media_actions.find(".add-media");
        $addMediaAction.unbind("click.itemView"); // cleanup previous click events
        $addMediaAction.bind("click.itemView", function() {
          launchAddDialog(item);
        });
        $addMediaAction.parent().show();
      }

      // item-remove-link
      var $removeAction = $media_actions.find(".item-remove-link");
      $removeAction.parent().show();
      $removeAction.unbind("click.itemView"); // cleanup previous click events
      if (!isMyMedia && item.control) {
        $removeAction.bind("click.itemView", function(){
          removeItem(item, this);
        });
        $removeAction.parent().show();
      } else {
        $removeAction.parent().hide();
      }

      // review new player and data, hide loading
      $media_viewer_loading.hide();
      $media_viewer.show();
    };

    var showMediaItem = function(item) {
      initializeFocusedView(item, true);
    };

    // NOTE: need to put the content of the function down here but define it above
    removeItem = function(item, clickedOn) {
      if (!item) {
        alert("item is not set in removeItem: "+clickedOn);
        return; // have to exit here
      }
      if (showDeleteItemWarnings) {
        var message = kaltura.i18n("i18n_delete.item.confirmation");
        if (message.indexOf("Unknown i18n key") > -1) {
          message = "Are you sure you want to delete this item?";
        }
        if (!confirm(message)) {
          return false;
        }
      }
      var containerType = isCollection ? "/coll" : "/site";
      var $clickedLI = $(clickedOn).closest('li.media-item'); // get the container
      $clickedLI.mask(".loading-overlay-small");
      var ajaxKey = kaltura.ajaxStart();
      $.ajax({
        url: "/direct/kaltura/remove/" + item.idStr + containerType + "/" + currentCollectionId, 
        type: "POST",
        dataType: "text",
        //async: false, // need this syncronous because of the reload processing
        cache: false,
        success: function() {
          if (kaltura.removeItemFromPager(mediaSelector, item.id)) { // PAGER REFRESH
            // add spinners back to all items being added or removed
            kaltura.addSpinnersInPager(mediaSelector, "#collection_pager");
          }

          // check and refresh the left view after the item is removed
          if (currentItemId && item.idStr === currentItemId) {
            // need to reload the left item view
            if (mediaSelector.state.mediaItems.length > 0) {
              item = mediaSelector.state.mediaItems[0];
              initializeFocusedView(item, true);
            } else {
              // no items left so reload the page
              location.reload(true);
            }
          }
        },
        error: function( jqXHR, textStatus, errorThrown ) {
          alert( "Delete failed from "+containerType+"/"+currentCollectionId+" for "+item.name+" ("+item.idStr+"): "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
          $clickedLI.unmask();
        },
        complete: function() {
          kaltura.ajaxDone(ajaxKey);
        }
      });
    };

    var saveItem = function() {
      var $itemDetails = $("#itemDetails");
      var form = $itemDetails.get(0);
      var mediaId = $itemDetails.find(".kaltura-item-id").val();
      /* Represents the type of container for the media item,
       * this will be "mymedia", "library", or "collection"
       */
      var containerType = $itemDetails.find(".kaltura-container-type").val();
      // The id for the containerType defined above
      var containerId = $itemDetails.find(".kaltura-container-id").val();

      // validate
      var valid = false;
      if (! form.title.value || form.title.value.replace(/\s/g, "") === "") {
        // title is blank
        $itemDetails.find(".invalid_title").show();
        valid = false;
      } else {
        $itemDetails.find(".invalid_title").hide();
        valid = true;
      }

      if (valid) {
        // only execute if the validation passed
        var data = {
            name: form.title.value,
            desc: form.desc.value,
            tags: form.tags.value,
            hidden: ! $(form["public"]).attr("checked"), // invert
            shared: $(form.shared).attr("checked"),
            remixable: $(form.remixable).attr("checked")
        };
        // generate the URL depending on the item container
        var urlSuffix = "/my"; // assume my media
        if (containerType === "collection") {
          urlSuffix = "/coll/"+containerId;
        } else if (containerType === "library") {
          if (containerId && (containerId.indexOf("/") === 0)) {
            urlSuffix = containerId;
          } else {
            urlSuffix = "/site/"+containerId;
          }
        }
        var ajaxURL = "/direct/kaltura/media/" + mediaId + urlSuffix + ".json";
        $.ajax({
          type: "POST",
          dataType: "json",
          url: ajaxURL,
          async: false,
          data: data,
          success: function(data) { // success(data, textStatus, jqXHR)
            var currentItem = null;
            var newItems = [];
            $(mediaSelector.state.mediaItems).each(function(idx, item) {
              if (item.idStr === mediaId) {
                // update the item if is the one we saved
                item.name = data.name;
                item.shortName = data.shortName;
                item.desc = data.desc;
                item.tags = data.tags;
                item.hidden = !!(data.hidden);
                item.shared = !!(data.shared);
                item.remixable = !!(data.remixable);
                currentItem = item;
              }
              newItems.push(item);
            });
            if (! currentItem && newItems.length > 0) {
              currentItem = newItems[0];
            }
            mediaItems = newItems;
            mediaSelector.state.mediaItems = mediaItems;
            mediaSelector.refresh(); // PAGER REFRESH
            if (currentItem && currentItemId && currentItem.idStr === currentItemId) {
              // need to reload the left item view meta data
              initializeFocusedView(currentItem, false);
            }
          },
          error: function( jqXHR, textStatus, errorThrown ) {
            alert("Failed to save item ("+mediaId+"), error: "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
          }
        });
        $("#editDetails").dialog("close"); // this seems to have no effect? -AZ
        // valid
        form.validated.value = "1"; // true
        $itemDetails.find(".invalid_form").hide();
      } else {
        // not valid
        form.validated.value = ""; // false
        $itemDetails.find(".invalid_form").show();
      }
      return false;
    };

    var addItemToCollection = function() {
      var $addToCollectionDetails = $("#addToCollectionDetails");
      var $form = $($addToCollectionDetails.get(0));
      var kalturaId = $form.find("#kalturaId").val();
      var collectionId = $form.find("#targetCollections").find(':selected')[0].value;
      //var locationId = $("#locationId").val();

      // POST /kaltura/mymediaadd/{kalturaId}/site/{siteId}
      // POST /kaltura/mymediaadd/{kalturaId}/collection/{collectionId}
      // collection id will be "/site/{siteId}" or "/collection/{collectionId}"
      var restURL = "/direct/kaltura/mymediaadd/" + kalturaId + collectionId;
      var ajaxKey = kaltura.ajaxStart();
      $.ajax({
        url: restURL,
        type: "POST",
        dataType: "text",
        success: function(data) {
          $("#addSuccess").show();
          $("#addSuccess").fadeOut(3000);
        },
        error: function( jqXHR, textStatus, errorThrown ) {
          alert("Failure trying to add media to collection: "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
        },
        complete: function() {
          kaltura.ajaxDone(ajaxKey);
        }
      });
      return false;
    };


    // EXECUTE ALL BELOW IMMEDIATELY

    // add delete confirm handling
    $(".delete_coll_confirm").click(function() {
      var message = kaltura.i18n("i18n_delete.confirmation");
      if (message.indexOf("Unknown i18n key") > -1) {
        message = "Are you sure you want to delete this collection and all items within it?";
      }
      var answer = confirm(message);
      return answer;
    });

    // Edit media dialog initialization
    $("#editDetails").dialog({
      autoOpen: false,
      modal: true,
      position: "center", //[525,200], // "top",
      open: function(event, ui) {
        // SKE-70 Editing media tags to one character is not being saved
        var itemDetails = jQuery("#itemDetails");
        //hide invalid tag dialog
        itemDetails.find('.invalid-tag').hide();
        //check for valid tags
        jQuery('.kaltura-item-tags-edit').keyup(function() {
          var badTags = "";
          var tagValues = this.value.split(",");
          for (var i = 0; i < tagValues.length; i++) {
            var tag = jQuery.trim(tagValues[i]);
            if (tag.length === 1) {
              badTags += tag + ", ";
            }
          }
          if (jQuery.trim(badTags).length > 0) {
            itemDetails.find('.invalid_tag_list').html(badTags.substring(0,badTags.length - 2)); // trim off the end comma
            itemDetails.find('.invalid-tag').show();
          } else {
            itemDetails.find('.invalid-tag').hide();
          }
        });
        // END SKE-70
        $(this).dialog("option", "position", ["center",100]); // reset the position to center near the top
      },
      buttons: {
        "Save": function() {
          $("#itemDetails").submit();
          var form = $("#itemDetails").get(0);
          if (form.validated.value) {
            $(this).dialog("close");
          }
        },
        "Cancel": function() {
          $(this).dialog("close");
        }
      }
    });
    $("#itemDetails").submit(saveItem); // bind save handler

    // Add media to collection dialog initialization
    $("#addMedia").dialog({
      autoOpen: false,
      modal: true,
      position: "center",
      width: 500,
      height: 250,
      buttons: {
        "Add" : function() {
          $("#addToCollectionDetails").submit();
          $(this).dialog("close");
        },
        "Cancel": function() {
          $(this).dialog("close");
        }
      }
    });
    $("#addToCollectionDetails").submit(addItemToCollection); // bind save handler

    var url;
    if (isCollection && currentCollectionId) {
      url = "/direct/kaltura/" + currentCollectionId + ".json?populate=true&timestamp=" + new Date().getTime();
    } else if (isMyMedia) {
      url = "/direct/kaltura/library/site/isMyMedia.json?timestamp=" + new Date().getTime();
    } else if (currentLocationId) {
      url = "/direct/kaltura/library/" + currentLocationId + ".json?timestamp=" + new Date().getTime();
    } else {
      alert("Invalid state: cannot render view because there is no collection or library id and no myMedia flag set");
      throw Exception("Invalid state: cannot render view because there is no collection or library id and no myMedia flag set");
    }

    // had to remove this so we only do a single items fetch - if (hasItems) { // fix for JS error when no items
    mediaSelector = kaltura.KalturaMediaSelector(
      $(".collection-media"),
      {
        collectionUrl: url,
        listeners: {
          onThumbnailSelect: showMediaItem,
          onTitleSelect: showMediaItem,
          onDetailsSelect: launchEditDialog,
          onAddSelect: launchAddDialog,
          onRemoveSelect: removeItem
        }
      }
    );

    // hide loading indicator
    $("#collection_items_loading").hide();

    if (mediaSelector.state.itemCount > 0) {
      $("#items_in_collection").show();
      // there are items in this collection
      var items = mediaSelector.state.mediaItems;
      // update items count
      $("#pager_itemsCount").text(items.length);
      var item = null;
      if (currentItemId) { // should be set to the current item if there is one
        for (var i = 0; i < items.length; i++) {
          if (items[i].id === currentItemId) {
            item = items[i];
            break;
          }
        }
      }
      if (!item) { // use the first one
        item = mediaSelector.state.mediaItems[0];
      }
      initializeFocusedView(item, true);
    } else {
      // no items in this collection - show the no items messages
      $("#no_items_in_collection").show();
    }
  }; // END initViewCollection

  kaltura.initFckEditorSelector = function(superUser) {
    var uploadSpecialId = '';
    var uploadSpecialKS = '';
    var editorMediaSelector = null;
    //var uploaderInitialized = false;
    if (typeof superUser === "undefined") {
      superUser = false;
    } else if (typeof superUser === "string") {
      superUser = (superUser.toLowerCase() === "true");
    }

    var insertKalturaIframe = function(item){
      var html = '<span class="kaltura-media" rel="' + item.kalturaId + '::' + item.mediaType + '">';
      html += '<img src="' + item.thumbnail + '"/>';
      html += '</span>';
      if (window.opener && window.opener.FCK) {
        window.opener.FCK.InsertHtml(html);
        window.close();
      } else if (window.opener && window.opener.CKEDITOR) {
        window.opener.CKEDITOR.instances[window.opener.ckeditorId].insertHtml(html);
        window.close();
      } else {
        var message = kaltura.i18n("i18n_error.unable.to.insert.in.editor");
        if (message.indexOf("Unknown i18n key") > -1) {
          message = "Cannot insert kaltura data in editor (Cannot find FCK or CKEDITOR)";
        }
        alert(message);
      }
    };

    var showUploadControl = function() {
      // activate and show the upload control
      $(".upload-media-link").click( function() {
        kaltura.KCW(".upload-media-window", {
          containerId: "kaltura-uploader",
          uploadSpecialId: uploadSpecialId, // pass the special upload id if set (empty string otherwise)
          uploadSpecialKS: uploadSpecialKS // pass special KS if set (empty string otherwise)
        });
        $(".browse-media-window").hide();
        $(".upload-media-window").show();
      });
      $(".upload-media-link-wrapper").show();
    };

    var reloadEditorMediaSelector = function() {
      var $pager = $(".browse-media-window .pager-body");
      $pager.hide();
      var locationRef = kaltura.getLocationRef();
      if (!locationRef) {
        locationRef = "/site/isMyMedia";
      }
      var libraryURL = "/direct/kaltura/library" + locationRef + ".json?shared=true&timestamp=" + new Date().getTime();
      jQuery.ajax({
        url: libraryURL,
        cache: false,
        type: "GET",
        dataType: "json",
        success: function(data) {
          editorMediaSelector.state.mediaItems = data;
          editorMediaSelector.refresh();
        },
        error: function( jqXHR, textStatus, errorThrown ) {
          alert("Failure reloading the media items: "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
        },
        complete: function() {
          $pager.show();
          $(".refresh-media-spinner").hide();
        }
      });
    };

    kaltura.closeEditorKalturaUploader = function() {
      //window.location.reload(true); // this will get rid of the context we used to have
      jQuery(".upload-media-window").hide();
      jQuery(".browse-media-window").show();
      reloadEditorMediaSelector(); // need to reload the media
    };

    $(document).ready(function() {
      var locationRef = kaltura.getLocationRef();
      if (!locationRef || 0 === locationRef.length) {
        alert("Warning: Kaltura SKE unable to find the current site or tool id from the available URLs data, fallback to myMedia");
        locationRef = "/site/isMyMedia";
      }

      var directUrl = "/direct/kaltura/library" + locationRef + ".json?shared=true&timestamp=" + new Date().getTime();
      editorMediaSelector = kaltura.KalturaMediaSelector(
          ".media-browser",
          {
            collectionUrl: directUrl,
            listeners: {
              onThumbnailSelect: insertKalturaIframe,
              onTitleSelect: insertKalturaIframe
            }
          }
      );

      // uploader stuff below here
      $(".close-upload-link").click(function(e) {
        kaltura.closeEditorKalturaUploader();
        e.preventDefault();
        e.stopPropagation();
        return false;
      });

      $(".refresh-media-link").click(function(e) {
        var $spinner = $(this).parent().find(".refresh-media-spinner");
        $spinner.show();
        reloadEditorMediaSelector(); // reload the media
        e.preventDefault();
        e.stopPropagation();
        return false;
      });

      if (superUser) {
        // super user gets automatic upload access (no special uploader handling)
        showUploadControl();
      } else {
        // show the uploader if the user has permissions
        jQuery.ajax({
          url: "/direct/kaltura/perms" + locationRef + ".json?timestamp=" + new Date().getTime(),
          async: true,
          cache: false,
          type: "GET",
          dataType: "json",
          success: function(data) {
            if (data.write) {
              if (data.uploadSpecial) {
                uploadSpecialId = data.uploadSpecialId;
                uploadSpecialKS = data.uploadSpecialKS;
              }
              showUploadControl();
            }
          },
          error: function( jqXHR, textStatus, errorThrown ) {
            alert("Failure checking uploader perms: "+textStatus+": status="+jqXHR.status+" "+jqXHR.statusText );
          }
        });
      }

    });
  };

  /**
   * Assists with translating strings which exist in javascript by pulling the translated
   * strings from the DOM via id keys,
   * the DOM translations follow the convention:
   * <div style="display: none;">
   *     <span id="i18n_app.hello.world"><spring:message code="app.hello.world" /></span>
   *     <span id="i18n_app.hello.user"><spring:message code="app.hello.user" /></span>
   * </div>
   * 1) First key is "app.hello" and first translated string is "Hello World!"
   * 2) In the second example {0} is a replacement value in the translated string (Hello, your name is {0}!) 
   * which would have the user name inserted in the JS via the replacements array. 
   * NOTE that you can also do the replacement using the spring:message if you have the replacement value 
   * available at the point you are using that tag but in cases where the value is not known 
   * until you are in the javascript, this technique should be used.
   *
   * var userName = "Aaron Zeckoski";
   * ...
   * var translated1 = kaltura.i18n('app.hello.world'); // Hello World!
   * var translated2 = kaltura.i18n('app.hello.user', {'{0}': userName}); // Hello, your name is Aaron Zeckoski!
   * 
   * NOTE: depends on jQuery 1.4 or better
   * 
   * @param string key the translation key (should not have spaces or quotes)
   * @param array replacements [OPTIONAL] the replacement values for the translated string
   * @param bool prependKeyId [OPTIONAL] If true or undefined (parameter excluded), prepend the default of _i18n,
   *      if false, no string will be prepended to the key name (for DOM lookup),
   *      if string value, prepend that value to all keys when doing the DOM lookup
   * @return string the translated string
   */
  kaltura.i18n = function(key, replacements, prependKeyId) {
    var translatedString = '{Unknown i18n key: '+key+'}';
    prependKeyId = (typeof prependKeyId === "undefined" ? true : prependKeyId);
    if (prependKeyId === true) {
      prependKeyId = "i18n_";
    }
    if (key && typeof key === 'string') {
      var found = false;
      // get the string from element content with id matching the key in the current page
      var $keyText = $("#" + (prependKeyId ? prependKeyId : "") + key.replace(/[ '"]/,'_').replace(/(:|\.)/g,'\\$1')); // replace space and quotes with underscore, replace . with \\.
      if ($keyText.length > 0) {
        translatedString = $keyText.text();
        found = true;
      }
      if (found && translatedString && replacements) {
        // replace the replacement values in the string with values from the replacements array
        for (var translateKey in replacements) {
          if (replacements.hasOwnProperty(translateKey)) {
            if (translateKey) {
              translatedString = translatedString.replace(translateKey, replacements[translateKey]);
            }
          }
        }
      }
    }
    return translatedString;
  };

  /**
   * Removes an item from a pager and refreshes the pager if needed
   * @param object pager the fluid pager to remove the item from
   * @param string itemId the item id to remove
   * @param boolean noRefresh if true (default) then do not refresh the pager, else allow the pager to be refreshed if needed
   * @returns boolean true if the item was removed OR false if not
   */
  kaltura.removeItemFromPager = function(pager, itemId) {
    var newItems = [];
    var found = false;
    $(pager.state.mediaItems).each(function(idx, entry) {
      if (itemId !== entry.id) {
        newItems.push(entry);
      } else {
        found = true;
      }
    });
    // only refresh if the item was found and removed
    if (found) {
      pager.state.mediaItems = newItems;
      pager.refresh(); // PAGER REFRESH
    }
    return found;
  };

  /**
   * Adds the spinners in the pager by finding all items currently being
   * added or removed and adding a spinner to them
   * NOTE: to clear all spinners, just refresh() the pager OR $pager.find("li").unmask();
   * @param object pager the fluid pager to remove the item from
   * @param string pagerSelector jquery selecter for the UL holding all the pager LI items
   */
  kaltura.addSpinnersInPager = function(pager, pagerSelector) {
    var $pager = $(pagerSelector);
    for (var i = 0; i < pager.state.mediaItems.length; i++) {
      if (pager.state.mediaItems[i]) {
        if (pager.state.mediaItems[i].hasOwnProperty('adding') || pager.state.mediaItems[i].hasOwnProperty('removing')) {
          // add the spinner to this item
          var itemId = pager.state.mediaItems[i].id;
          $pager.find("li.id-"+itemId).mask(".loading-overlay-small");
        }
      }
    }
  };

  // SKE-167 START
  /**
   * flag to indicate if there is an ajax process still running and we should not allow the user to leave the page yet
   */
  kaltura.ajaxRunning = false;
  kaltura.ajaxRunningKeys = [];
  kaltura.ajaxRunningCounter = 1;
  /**
   * Indicate we have started an ajax method which should not allow the page to change while it is running
   * @returns the key for this ajax run (used as input for kaltura.ajaxDone method)
   */
  kaltura.ajaxStart = function() {
    kaltura.ajaxRunning = true;
    if (kaltura.ajaxRunningKeys.length <= 0) {
      // this is the first item so turn on the running indicator
      $(".ajax-running").show();
    }
    var startKey = new Date().getTime() + ":" + kaltura.ajaxRunningCounter++;
    kaltura.ajaxRunningKeys.push(startKey);
    return startKey;
  };
  /**
   * Indicate we have completed an ajax method which should not allow the page to change while it is running
   * @param startKey the key value returned from the kaltura.ajaxStart method OR null to just get the number of executing processes
   * @returns the number of currently executing ajax processes
   */
  kaltura.ajaxDone = function(startKey) {
    if (startKey) {
      // remove this key from the array
      var pos = $.inArray(startKey, kaltura.ajaxRunningKeys);
      if (pos > -1) {
        kaltura.ajaxRunningKeys.splice(pos, 1);
      }
    }
    var numKeys = kaltura.ajaxRunningKeys.length;
    if (numKeys <= 0) {
      // this is the last so turn off the ajax running indicators
      kaltura.ajaxRunning = false;
      $(".ajax-running").hide();
      // show the running-complete message (if there is one)
      $(".ajax-running-complete").fadeIn(100).delay(2000).fadeOut(400);
    }
    return numKeys;
  };
  // register a listener which will stop the page from unloading while our ajax is executing
  window.onbeforeunload = function(e) {
    if (kaltura.ajaxRunning) {
      return 'You have made changes which are still being saved. If you navigate away from this page you will lose your unsaved changes.';
    }
  };
  // SKE-167 END

  /**
   * @returns {String} the location ref if it can be found (e.g. /tool/fdsdfsfsd-fsfsdsdf-fdssdffd-sdffddf OR /site/mySiteId) OR null if it cannot
   */
  kaltura.getLocationRef = function() {
    var ref = null;
    var $currentInfo = $('#currentInfo');
    // try to get from currentInfo in the DOM
    var $locationRef = $currentInfo.find('#locationRef');
    if ($locationRef.length === 1 && $locationRef.val()) {
      ref = $locationRef.val();
    } else {
      // try to get from the window URL
      var toolId = kaltura.getToolId();
      var siteId = kaltura.getSiteId();
      ref = (siteId !== '' ? '/site/' + siteId : (toolId !== '' ? '/tool/' + toolId : null));
      if (!ref) {
        ref = null;
      } else {
        // store the found value in the currentInfo for future use
        $currentInfo.append('<input id="locationRef" value="'+ref+'" type="hidden" />');
      }
    }
    return ref;
  };

  /**
   * @returns {String} the Sakai toolId (GUID) if it can be extracted from the URL (or empty string if not)
   */
  kaltura.getToolId = function() {
    var result = '';
    var url = kaltura.getWindowParentURL();
    if (url !== '') {
      // tools ids are ALWAYS GUIDS so we can use a simpler matcher
      // SKE-222 Except when they are not and start with a ~ (MOTD from MyWorkspace)
      var reg = new RegExp(/\/tool\/[~a-z0-9\-]+/);
      var str = String(reg.exec(url));
      result = str.substring(6);
    }
    return result;
  };

  /**
   * @returns {String} the Sakai siteId (String) if it can be extracted from the URL (or empty string if not)
   */
  kaltura.getSiteId = function() {
    var result = '';
    var url = kaltura.getWindowParentURL();
    if (url !== '') {
      /* would not correctly match in some cases
      var url = window.opener.parent.location.href;
      var reg = new RegExp(/\/site\/[a-z0-9\-]+/);
      var str = String(reg.exec(url));
      return str.substring(6);
      */
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
    return result;
  };

  /**
   * @returns {String} the url for the parent window IF it can be found
   */
  kaltura.getWindowParentURL = function() {
    var url = '';
    var $currentInfo = $('#currentInfo');
    // try to get from currentInfo in the DOM
    var $parentWindowURL = $currentInfo.find('#parentWindowURL');
    if ($parentWindowURL.length === 1 && $parentWindowURL.val()) {
      url = $parentWindowURL.val();
    } else {
      // there might not be an opener or a parent so we have to be more flexible with our checks -AZ
      if (window.opener && window.opener.parent && window.opener.parent.location) {
        url = window.opener.parent.location.href;
      } else if (window.opener && window.opener.location) {
        url = window.opener.location.href;
      } else if (window.parent && window.parent.location) {
        url = window.parent.location.href;
      }
      if (url !== '') {
        // store the found value in the currentInfo for future use
        $currentInfo.append('<input id="parentWindowURL" value="'+url+'" type="hidden" />');
      }
    }
    return url;
  };

})(jQuery, fluid_1_3);
