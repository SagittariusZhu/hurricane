package org.iipg.hurricane.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.iipg.hurricane.conf.HurricaneConfiguration;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.mysql.impl.MysqlClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlSchema {

	private static Logger LOG = LoggerFactory.getLogger(MysqlSchema.class);
	
	private HurricaneConfiguration conf = null;
	
	private String schemaName = "";
	private List<String> fields = new ArrayList<String>();
	private List<String> indexes = new ArrayList<String>();
	
	public MysqlSchema(HurricaneConfiguration conf) {
		this.conf = conf;
	}

	public void addField(Field item) {
		StringBuffer buf = new StringBuffer();
		buf.append(item.getName()).append(" ");
		if ("string".equalsIgnoreCase(item.getType())) {
			buf.append("char(" + item.getLength() + ") ");
		} else {
			buf.append(item.getType()).append(" ");
		}
		if (item.isRequired()) {
			buf.append("not null ");
		}
		if (item.isUnique()) {
			buf.append("primary key ");
		}
		fields.add(buf.toString());
		
		if (item.isIndexed()) {
			indexes.add(item.getName());
		}
	}

	public void setName(String name) {
		this.schemaName = name;		
	}

	public String getName() {
		return this.schemaName;
	}
	
	/**
	 * create table sys_schema (
	 * 		schemaid char(255) not null primary key, 
	 * 		name char(255) not null
	 * ) ENGINE=ndbcluster;
	 * 
	 * alter table sys_schema add index idx_sys_name(name);
	 * @throws SQLException 
	 */
	public void persist() throws SQLException {
		dropSchema(this.schemaName);
		
		StringBuffer createBuf = new StringBuffer();
		createBuf.append("CREATE TABLE ")
				 .append(this.schemaName).append(" (");
		for (String item : fields) {
			createBuf.append(item).append(",");
		}
		int len = createBuf.length();
		createBuf.replace(len - 1, len, ")");
		createBuf.append(" ENGINE=ndbcluster CHARSET=utf8;");
		//LOG.info(createBuf.toString());
		MysqlClient.executeUpdateSQL(createBuf.toString());
		
		for (String item : indexes) {
			StringBuffer idxBuf = new StringBuffer();
			idxBuf.append("ALTER TABLE ")
				  .append(this.schemaName)
				  .append(" ADD INDEX ")
				  .append("idx_").append(this.schemaName).append("_").append(item)
				  .append("(").append(item).append(");");
			//LOG.info(idxBuf.toString());
			MysqlClient.executeUpdateSQL(idxBuf.toString());
		}
	}

	/**
	 * Drop table sys_schema;
	 * @param schemaName2
	 * @throws SQLException 
	 */
	public void dropSchema(String name) throws SQLException {
		StringBuffer buf = new StringBuffer();
		buf.append("DROP TABLE IF EXISTS ").append(name).append(";");
		//LOG.info(buf.toString());		
		MysqlClient.executeUpdateSQL(buf.toString());
	}
	

}
