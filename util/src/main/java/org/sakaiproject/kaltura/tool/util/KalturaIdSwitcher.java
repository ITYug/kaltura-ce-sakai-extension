/**
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
package org.sakaiproject.kaltura.tool.util;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * KalturaIdSwitcher is a tool to support the changing of IDs - originally used by Stanford as they migrated from a pre-2013
 * version of the Kaltura plugin at the same time as moving the hosting of the files to Kaltura. This necessitated changing all
 * the ids in the DB before upgrading the tool (which would handle the final data migration on the sakai side).
 * 
 * @author chasegawa@unicon.net
 */
public class KalturaIdSwitcher {
    /**
     * Run the id switcher. First param is the name of the file with the ids, second (optionally) is the Spring configuration
     * file. Spring config file defaults to util.xml
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("The name/path of the ids file must included in the command line args");
        }
        // Read the config file to load the db config and setup
        String appContextFile = args.length > 1 ? args[1] : "/util.xml";
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(appContextFile);
        ISwitcher kis = (ISwitcher) applicationContext.getBean("kalturaIdSwitcher");
        kis.swapIdUsingDataFromFile(args[0]);
    }
}
