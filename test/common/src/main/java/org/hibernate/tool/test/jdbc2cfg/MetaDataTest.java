/*
 * Created on 2004-11-23
 *
 */
package org.hibernate.tool.test.jdbc2cfg;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;
import org.hibernate.tools.test.util.JUnitUtil;
import org.hibernate.tools.test.util.JdbcUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author max
 * @author koen
 * 
 */
public class MetaDataTest {

	static final String[] CREATE_SQL = new String[] {
				"CREATE TABLE BASIC ( A INT NOT NULL, NAME VARCHAR(20), PRIMARY KEY (A)  )",
				"CREATE TABLE SOMECOLUMNSNOPK ( PK VARCHAR(25) NOT NULL, B CHAR, C INT NOT NULL, A_BOOLEAN CHAR )",
				"CREATE TABLE MULTIKEYED ( ORDERID VARCHAR(10), CUSTOMERID VARCHAR(10), NAME VARCHAR(10), PRIMARY KEY(ORDERID, CUSTOMERID) )" 
			};

	static final String[] DROP_SQL = new String[] {
	        "DROP TABLE BASIC", 
	        "DROP TABLE SOMECOLUMNSNOPK",
			"DROP TABLE MULTIKEYED" 
	        };

	private JDBCMetaDataConfiguration jmdcfg = null;

	@Before
	public void setUp() {
		JdbcUtil.createDatabase(this);
		jmdcfg = new JDBCMetaDataConfiguration();
		jmdcfg.readFromJDBC();
	}

	@After
	public void tearDown() {
		JdbcUtil.dropDatabase(this);
	}

	@Test
	public void testBasic() {
		JUnitUtil.assertIteratorContainsExactly(
				"There should be three tables!", 
				jmdcfg.getMetadata().getEntityBindings().iterator(),
				3);
		Table table = jmdcfg.getTable( JdbcUtil.toIdentifier(this, "BASIC" ) );
		Assert.assertEquals( 
				JdbcUtil.toIdentifier(this, "BASIC"), 
				JdbcUtil.toIdentifier(this, table.getName()));
		Assert.assertEquals( 2, table.getColumnSpan() );
		Column basicColumn = table.getColumn( 0 );
		Assert.assertEquals( 
				JdbcUtil.toIdentifier(this, "A"), 
				JdbcUtil.toIdentifier(this, basicColumn.getName()));
		// TODO: we cannot call getSqlType(dialect,cfg) without a
		// MappingassertEquals("INTEGER", basicColumn.getSqlType() ); // at
		// least on hsqldb
		// assertEquals(22, basicColumn.getLength() ); // at least on oracle
		PrimaryKey key = table.getPrimaryKey();
		Assert.assertNotNull( "There should be a primary key!", key );
		Assert.assertEquals( key.getColumnSpan(), 1 );
		Column column = key.getColumn( 0 );
		Assert.assertTrue( column.isUnique() );
		Assert.assertSame( basicColumn, column );
	}

	@Test
	public void testScalePrecisionLength() {
		Table table = jmdcfg.getTable( JdbcUtil.toIdentifier(this, "BASIC"));
		Column nameCol = table.getColumn(new Column( JdbcUtil.toIdentifier(this, "NAME" ) ) );
		Assert.assertEquals( nameCol.getLength(), 20 );
		Assert.assertEquals( nameCol.getPrecision(), Column.DEFAULT_PRECISION );
		Assert.assertEquals( nameCol.getScale(), Column.DEFAULT_SCALE );
	}

	/*
	 * public void testGetTables() {
	 * 
	 * Table table = new Table(); table.setName("dummy"); cfg.addTable(table);
	 * 
	 * Table foundTable = cfg.getTable(null,null,"dummy");
	 * 
	 * assertSame(table,foundTable);
	 * 
	 * foundTable = cfg.getTable(null,"dschema", "dummy");
	 * 
	 * assertNotSame(table, foundTable); }
	 */

	@Test
	public void testCompositeKeys() {
		Table table = jmdcfg.getTable(JdbcUtil.toIdentifier(this, "MULTIKEYED"));
		PrimaryKey primaryKey = table.getPrimaryKey();
		Assert.assertEquals( 2, primaryKey.getColumnSpan() );
	}

}
