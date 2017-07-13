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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.sakaiproject.kaltura.logic.entity.CollectionEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.TestCase;


/**
 * Testing the XML conversions of the collection entity objects
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class CollectionEntityTest extends TestCase {

    /**
     * Test method for {@link org.sakaiproject.kaltura.logic.entity.CollectionEntity#toXml(org.w3c.dom.Document, java.util.Stack)}.
     */
    public void testToXml() throws Exception {
        // setup test data
        MediaCollection mc = new MediaCollection(null, "AZlocation", "AZowner", "AZcollection", true, MediaCollection.SHARING_PUBLIC);
        ArrayList<MediaItem> items = new ArrayList<MediaItem>();
        MediaItem mi1 = new MediaItem(null, "1_itjk7baf", "own2", true, false, false);
        mi1.setCollection(mc);
        items.add(mi1);
        MediaItem mi2 = new MediaItem(null, "1_dgvh2sxv", "own2", true, false, false);
        mi2.setCollection(mc);
        items.add(mi2);
        MediaItem mi3 = new MediaItem(null, "0_ir33td8e", "own2", true, false, false);
        mi3.setCollection(mc);
        items.add(mi3);
        mc.setItems(items);
        CollectionEntity collectionEntity = new CollectionEntity(mc);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        Stack<Element> stack = new Stack<Element>();
        Element e = collectionEntity.toXml(doc, stack);
        assertNotNull(e);
        assertEquals(CollectionEntity.XML_COLLECTION_KEY, e.getNodeName());

        // turn the DOM object into raw XML
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();
        assertNotNull(xmlString);
        assertTrue(xmlString.contains("<collection"));
        assertTrue(xmlString.contains("<item"));
        /*
<collection dateCreated="2012-05-01T16:03:28.307-04:00" dateModified="2012-05-01T16:03:28.307-04:00" description="" hidden="true" id="" locationId="AZlocation" ownerId="AZowner" sharing="public" title="AZcollection">
  <item creatorId="own2" dateCreated="2012-05-01T16:03:28.344-04:00" dateModified="2012-05-01T16:03:28.344-04:00" hidden="true" id="" kalturaId="1_itjk7baf" ownerId="own2" position="0" remixable="false" shared="false" type=""/>
  <item creatorId="own2" dateCreated="2012-05-01T16:03:28.344-04:00" dateModified="2012-05-01T16:03:28.344-04:00" hidden="true" id="" kalturaId="1_dgvh2sxv" ownerId="own2" position="0" remixable="false" shared="false" type=""/>
  <item creatorId="own2" dateCreated="2012-05-01T16:03:28.344-04:00" dateModified="2012-05-01T16:03:28.344-04:00" hidden="true" id="" kalturaId="0_ir33td8e" ownerId="own2" position="0" remixable="false" shared="false" type=""/>
</collection>
         */
    }

    /**
     * Test method for {@link org.sakaiproject.kaltura.logic.entity.CollectionEntity#CollectionEntity(java.lang.String, java.lang.String, org.w3c.dom.Element)}.
     */
    public void testCollectionEntityStringStringElement() throws Exception {
        MediaCollection mc;
        String xml = 
                "<collection id=\"111\" locationId=\"loc1\" ownerId=\"own1\" title=\"AZtitle\" description=\"AZdesc\" dateCreated=\"2012-01-01\" dateModified=\"2012-02-01\" hidden=\"false\" sharing=\""+MediaCollection.SHARING_PUBLIC+"\">\n" +
                "    <item id=\"2222\" kalturaId=\"1_itjk7baf\" ownerId=\"own2\" creatorId=\"own2\" position=\"1\" dateCreated=\"2012-01-01\" dateModified=\"2012-02-01\" type=\""+MediaItem.TYPE_VIDEO+"\" hidden=\"false\" shared=\"true\" remixable=\"false\" />\n" +
                "    <item id=\"3333\" kalturaId=\"1_dgvh2sxv\" ownerId=\"own2\" creatorId=\"own2\" position=\"2\" dateCreated=\"2012-01-01\" dateModified=\"2012-02-01\" type=\""+MediaItem.TYPE_IMAGE+"\" hidden=\"false\" shared=\"true\" remixable=\"false\" />\n" +
                "    <item id=\"4444\" kalturaId=\"0_ir33td8e\" ownerId=\"own2\" creatorId=\"own2\" position=\"3\" dateCreated=\"2012-01-01\" dateModified=\"2012-02-01\" type=\""+MediaItem.TYPE_IMAGE+"\" hidden=\"false\" shared=\"true\" remixable=\"false\" />\n" +
                "</collection>";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
        Document doc = db.parse(bis);
        CollectionEntity collectionEntity = new CollectionEntity("AZlocation", "AZowner", doc.getDocumentElement());
        assertNotNull(collectionEntity);
        mc = collectionEntity.getMediaCollection();
        assertNotNull(mc);
        assertEquals("AZlocation", mc.getLocationId());
        assertEquals("AZowner", mc.getOwnerId());
        assertEquals("AZtitle", mc.getTitle());
        assertEquals("AZdesc", mc.getDescription());
        assertNotNull(mc.getItems());
        assertEquals(3, mc.getItems().size());
        assertNotNull(mc.getItems().get(0));
        assertEquals("1_itjk7baf", mc.getItems().get(0).getKalturaId());
        assertNotNull(mc.getItems().get(1));
        assertEquals("1_dgvh2sxv", mc.getItems().get(1).getKalturaId());
        assertNotNull(mc.getItems().get(2));
        assertEquals("0_ir33td8e", mc.getItems().get(2).getKalturaId());
    }

}
