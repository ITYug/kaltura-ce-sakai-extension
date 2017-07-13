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
package org.sakaiproject.kaltura.model;

import junit.framework.TestCase;


/**
 * Testing for static methods
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class MediaItemTest extends TestCase {

    /**
     * Test method for {@link org.sakaiproject.kaltura.model.MediaItem#truncateText(java.lang.String, int, int)}.
     */
    public void testTruncateText() {
        assertEquals("this is too short",
                MediaItem.truncateText("this is too short", 0, 0) );
        // --------=: 123456789-123456789-123456789-123456789-123456789-
        assertEquals("this is now no longer too short to be truncated...",
                MediaItem.truncateText("this is now no longer too short to be truncated but it is not using reverse space", 0, 0) );

        assertEquals("this is too short",
                MediaItem.truncateText("this is too short", 50, 0) );
        // --------=: 123456789-123456789-123456789-123456789-123456789-
        assertEquals("this is now no longer too short to be truncated...",
                MediaItem.truncateText("this is now no longer too short to be truncated but it is not using reverse space", 50, 0) );
        // --------=: 123456789-123456789-123456789-123456789-123456789-
        assertEquals("but now this is now no longer too short to be t...",
                MediaItem.truncateText("but now this is now no longer too short to be truncated but it is not using reverse space", 50, 0) );
        // --------=: 123456789-123456789-123456789-123456789-123456789-
        assertEquals("but now this is now no longer too short to be...",
                MediaItem.truncateText("but now this is now no longer too short to be truncated but it is not using reverse space", 50, 6) );
    }

}
