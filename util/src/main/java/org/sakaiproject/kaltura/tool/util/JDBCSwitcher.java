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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This version of the switcher handles actually updating the database. Configure the datasource in the Spring configuration file.
 * 
 * @author chasegawa@unicon.net
 */
public class JDBCSwitcher extends AbstractSwitcher {
    private JdbcTemplate jdbcTemplate;

    public void swapIdUsingDataFromFile(String idsFile) throws IOException {
        System.out.println("\nReading ids\n");
        final List<Ids> ids = parseFileForId(idsFile);
        String sql = "UPDATE kaltura_item SET kalturaId = ? where kalturaId = ?";
        System.out.println("Begining database update\n");
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return ids.size();
            }

            public void setValues(PreparedStatement ps, int index) throws SQLException {
                Ids id = ids.get(index);
                ps.setString(1, id.getNewId());
                ps.setString(2, id.getOldId());
            }
        });
        System.out.println("Update complete\n");
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

}
