/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.hibernate.tool.hbm2x.hbm2hbmxml.IdBagTest;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.hbm2x.Exporter;
import org.hibernate.tool.hbm2x.HibernateMappingExporter;
import org.hibernate.tools.test.util.HibernateUtil;
import org.hibernate.tools.test.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Dmitry Geraskov
 * @author koen
 */
public class TestCase {

	private static final String[] HBM_XML_FILES = new String[] {
			"UserGroup.hbm.xml"
	};
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	private File outputDir = null;
	private Exporter hbmexporter = null;

	@Before
	public void setUp() throws Exception {
		Metadata metadata = 
				HibernateUtil.initializeMetadata(this, HBM_XML_FILES);
		outputDir = temporaryFolder.getRoot();
		hbmexporter = new HibernateMappingExporter();
		hbmexporter.setMetadata(metadata);
		hbmexporter.setOutputDirectory(outputDir);
		hbmexporter.start();
	}
	
	@Test
	public void testAllFilesExistence() {
		Assert.assertFalse(new File(
				outputDir,
				"/GeneralHbmSettings.hbm.xml").exists());
		JUnitUtil.assertIsNonEmptyFile(new File(
				outputDir,
				"/org/hibernate/tool/hbm2x/hbm2hbmxml/IdBagTest/User.hbm.xml"));
		JUnitUtil.assertIsNonEmptyFile(new File(
				outputDir,
				"/org/hibernate/tool/hbm2x/hbm2hbmxml/IdBagTest/Group.hbm.xml"));		
	}
	
	@Test
	public void testArtifactCollection() {
		Assert.assertEquals(
				2,
				hbmexporter.getArtifactCollector().getFileCount("hbm.xml"));
	}
	
	@Test
	public void testReadable() {
		StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();
		ssrb.applySetting(AvailableSettings.DIALECT, HibernateUtil.Dialect.class.getName());
        MetadataSources metadataSources = new MetadataSources(ssrb.build());
        metadataSources.addFile(new File(
        		outputDir, 
        		"/org/hibernate/tool/hbm2x/hbm2hbmxml/IdBagTest/User.hbm.xml"));
        metadataSources.addFile(new File(
        		outputDir, 
        		"/org/hibernate/tool/hbm2x/hbm2hbmxml/IdBagTest/Group.hbm.xml"));
        Assert.assertNotNull(metadataSources.buildMetadata());
    }
	
	@Test
	public void testIdBagAttributes() {
		File outputXml = new File(
				outputDir,  
				"/org/hibernate/tool/hbm2x/hbm2hbmxml/IdBagTest/User.hbm.xml");
		JUnitUtil.assertIsNonEmptyFile(outputXml);
		SAXReader xmlReader =  new SAXReader();
		xmlReader.setValidation(true);
		Document document;
		try {
			document = xmlReader.read(outputXml);
			XPath xpath = DocumentHelper.createXPath("//hibernate-mapping/class/idbag");
			List<?> list = xpath.selectNodes(document);
			Assert.assertEquals("Expected to get one idbag element", 1, list.size());
			Element node = (Element) list.get(0);
			Assert.assertEquals(node.attribute( "table" ).getText(),"`UserGroups`");
			Assert.assertEquals(node.attribute( "name" ).getText(),"groups");
			Assert.assertEquals(node.attribute( "lazy" ).getText(),"false");
			Assert.assertEquals(node.attribute( "access" ).getText(),"field");
		} catch (DocumentException e) {
			Assert.fail("Can't parse file " + outputXml.getAbsolutePath());
		}		
	}
	
	@Test
	public void testCollectionId() throws DocumentException {
		File outputXml = new File(
				outputDir,  
				"/org/hibernate/tool/hbm2x/hbm2hbmxml/IdBagTest/User.hbm.xml");
		SAXReader xmlReader =  new SAXReader();
		xmlReader.setValidation(true);
		Document document = xmlReader.read(outputXml);	
		XPath xpath = DocumentHelper.createXPath("//hibernate-mapping/class/idbag/collection-id");
		List<?> list = xpath.selectNodes(document);
		Assert.assertEquals("Expected to get one collection-id element", 1, list.size());
		Element node = (Element) list.get(0);
		Assert.assertEquals(node.attribute( "column" ).getText(),"userGroupId");
		Assert.assertEquals(node.attribute( "type" ).getText(),"long");
		list = node.elements("generator");
		Assert.assertEquals("Expected to get one generator element", 1, list.size());
		node = (Element) list.get(0);
		Assert.assertEquals(node.attribute( "class" ).getText(),"increment");
	}
	
}
