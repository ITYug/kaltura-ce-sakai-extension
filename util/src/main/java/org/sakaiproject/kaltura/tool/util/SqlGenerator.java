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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SqlGenerator extends AbstractSwitcher {
    private String fileName;

    public void swapIdUsingDataFromFile(String idsFile) throws IOException {
        FileWriter fileWriter = new FileWriter(new File(fileName));
        System.out.println("\nReading ids\n");
        final List<Ids> ids = parseFileForId(idsFile);
        String sql1 = "UPDATE kaltura_item SET kalturaId = ";
        String sql2 = " where kalturaId = ";
        System.out.println("Begining output\n");
        for (Ids id : ids) {
            StringBuilder sb = new StringBuilder(sql1);
            sb.append(id.getNewId()).append(sql2).append(id.getOldId()).append(";");
            fileWriter.write(sb.toString());
            fileWriter.write("\n");
        }
        fileWriter.close();
        System.out.println("SQL file generation complete\n");
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
