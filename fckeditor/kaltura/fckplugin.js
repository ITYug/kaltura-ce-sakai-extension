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
var KalturaCommand=function(){
  //create our own command, we dont want to use the FCKDialogCommand because it uses the default fck layout and not our own
};

KalturaCommand.GetState=function() {
  return FCK_TRISTATE_OFF; //we dont want the button to be toggled
};

KalturaCommand.Execute=function() {
  //open a popup window when the button is clicked
  window.showModalDialog('/kaltura/service/fckKalturaSelector.htm', 'Kaltura', 'dialogwidth=700,dialogheight=550,scrollbars=no,scrolling=no,location=no,toolbar=no,resizable');
};

FCKCommands.RegisterCommand('Kaltura', KalturaCommand ); //otherwise our command will not be found


var K_kalturaButton = new FCKToolbarButton('Kaltura', FCKLang['DlgKalturaTitle']);
K_kalturaButton.IconPath = FCKConfig.PluginsPath + 'kaltura/kaltura.gif';

FCKToolbarItems.RegisterItem('Kaltura', K_kalturaButton);
