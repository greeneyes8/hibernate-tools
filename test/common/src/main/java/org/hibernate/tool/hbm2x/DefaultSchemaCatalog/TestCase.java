/*
 * Created on 2004-12-01
 *
 */
package org.hibernate.tool.hbm2x.DefaultSchemaCatalog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.hibernate.boot.Metadata;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.OverrideRepository;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.SchemaSelection;
import org.hibernate.cfg.reveng.TableIdentifier;
import org.hibernate.mapping.Table;
import org.hibernate.tool.metadata.MetadataSourcesFactory;
import org.hibernate.tools.test.util.JdbcUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



/**
 * @author max
 * @author koen
 */
public class TestCase {
	
	@Before
	public void setUp() {
		JdbcUtil.createDatabase(this);
	}
	
	@After
	public void tearDown() {
		JdbcUtil.dropDatabase(this);
	}
	
	@Test
	public void testReadOnlySpecificSchema() {
		OverrideRepository or = new OverrideRepository();
		or.addSchemaSelection(new SchemaSelection(null, "OVRTEST"));
		ReverseEngineeringStrategy res = or.getReverseEngineeringStrategy(new DefaultReverseEngineeringStrategy());
		List<Table> tables = getTables(MetadataSourcesFactory
				.createJdbcSources(res, null, true)
				.buildMetadata());
		Assert.assertEquals(2,tables.size());	
		Table catchild = (Table) tables.get(0);
		Table catmaster = (Table) tables.get(1);	
		if(catchild.getName().equals("CATMASTER")) {
			catchild = (Table) tables.get(1);
			catmaster = (Table) tables.get(0);
		} 	
		TableIdentifier masterid = TableIdentifier.create(catmaster);
		TableIdentifier childid = TableIdentifier.create(catchild);
		Assert.assertEquals(new TableIdentifier(null, "OVRTEST", "CATMASTER"), masterid);
		Assert.assertEquals(new TableIdentifier(null, "OVRTEST", "CATCHILD"), childid);	
	}

	@Test
	public void testOverlapping() {	
		OverrideRepository or = new OverrideRepository();
		or.addSchemaSelection(new SchemaSelection(null, "OVRTEST"));
		or.addSchemaSelection(new SchemaSelection(null, null, "MASTER"));
		or.addSchemaSelection(new SchemaSelection(null, null, "CHILD"));
		ReverseEngineeringStrategy res = 
				or.getReverseEngineeringStrategy(new DefaultReverseEngineeringStrategy());
		Metadata metadata = MetadataSourcesFactory
				.createJdbcSources(res, null, true)
				.buildMetadata();
		Set<TableIdentifier> tables = new HashSet<TableIdentifier>();
		Iterator<Table> iter = metadata.collectTableMappings().iterator();
		while(iter.hasNext()) {
			Table element = iter.next();
			boolean added = tables.add(TableIdentifier.create(element));
			if(!added) 
				Assert.fail("duplicate table found for " + element); 
		}
		Assert.assertEquals(4,tables.size());					
	}
	
	@Test
	public void testUseDefault() {
		Properties properties = new Properties();
		properties.setProperty(Environment.DEFAULT_SCHEMA, "OVRTEST");
		properties.setProperty(Environment.DEFAULT_SCHEMA, "OVRTEST");
		List<Table> tables = getTables(MetadataSourcesFactory
				.createJdbcSources(null, properties, true)
				.buildMetadata());
		Assert.assertEquals(2,tables.size());
		Table catchild = (Table) tables.get(0);
		Table catmaster = (Table) tables.get(1);
		if(catchild.getName().equals("CATMASTER")) {
			catchild = (Table) tables.get(1);
			catmaster = (Table) tables.get(0);
		} 	
		TableIdentifier masterid = TableIdentifier.create(catmaster);
		TableIdentifier childid = TableIdentifier.create(catchild);
		Assert.assertEquals("jdbcreader has not nulled out according to default schema", new TableIdentifier(null, null, "CATMASTER"), masterid);
		Assert.assertEquals("jdbcreader has not nulled out according to default schema", new TableIdentifier(null, null, "CATCHILD"), childid);
	}

	private List<Table> getTables(Metadata metadata) {
		List<Table> list = new ArrayList<Table>();
		Iterator<Table> iter = metadata.collectTableMappings().iterator();
		while(iter.hasNext()) {
			Table element = iter.next();
			list.add(element);
		}
		return list;
	}

}
