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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSwitcher implements ISwitcher {
    /**
     * One entry per line - assuming format of: Commented lines (--) are skipped. new id,old id (new comma old)
     * 
     * @param idsFile
     * @return List of the ID pairs
     * @throws IOException
     */
    protected List<Ids> parseFileForId(String idsFile) throws IOException {
        List<Ids> result = new ArrayList<Ids>();
        BufferedReader reader = new BufferedReader(new FileReader(new File(idsFile)));
        while (reader.ready()) {
            String idLine = reader.readLine();
            if (idLine.startsWith("--")) {
                continue; // Skip comments
            }
            String[] ids = idLine.split(",");
            result.add(new Ids(ids[1], ids[0]));
        }
        reader.close();
        return result;
    }

    class Ids {
        private String newId;
        private String oldId;

        public Ids(String oldId, String newId) {
            this.oldId = oldId;
            this.newId = newId;
        }

        public String getNewId() {
            return newId;
        }

        public String getOldId() {
            return oldId;
        }
    }
}
