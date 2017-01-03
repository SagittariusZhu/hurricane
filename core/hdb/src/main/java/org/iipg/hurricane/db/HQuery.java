package org.iipg.hurricane.db;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBQuery;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.db.util.HDBUtil;
import org.iipg.hurricane.model.HMWQuery;
import org.iipg.hurricane.qparser.SimpleQueryParser;
import org.iipg.hurricane.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class HQuery {

	private static final Logger LOG = LoggerFactory.getLogger(HQuery.class);

	private Map<String, HDBQuery> query = null;

	public HQuery() {
		query = new HashMap<String, HDBQuery>();
	}

	public HDBQuery getSolrQuery() {
		HDBQuery roQuery = query.get("solrQuery");
		return roQuery;
	}
	public void setSolrQuery(HDBQuery hQuery) {
		query.put("solrQuery", hQuery);
	}

	public static HQuery parse(HMWQuery query) throws IOException, ParserConfigurationException, SAXException{
		String schemaName = query.schema;
		SchemaParser schema = new SchemaParser(schemaName);

		HDBQuery hQuery = ((HDBNormalBroker) HDBFactory.getHDBBroker(schemaName)).getSolrAdapter().createQuery();

		hQuery.setSchema(schema);

		hQuery.setSchemaName(schemaName);

		String[] selectFields = null;
		if (query.selectFields == null || query.selectFields.length() == 0) {
			selectFields = schema.getFieldNames();
		} else {
			selectFields = query.selectFields.split(",");
		}
		boolean uniqueSelected = false;
		for (String name : selectFields) {
			Field field = schema.getField(name);
			if (field == null) {
				LOG.info("非静态字段:" + name);
				field = schema.getDynamicField(name);
				if (field == null) {
					LOG.warn("没有定义的字段:" + name);
					continue;
				}
			}

			String type = field.getType();
			if (HTyper.isExtType(type)) {
				try {
					HTyper typer = HTyper.getTyper(field);
					Field[] fields = typer.getFields();
					for (Field item : fields)
						hQuery.addSelectField(item.getName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}

			if (field.isUnique()) {
				uniqueSelected = true;
				hQuery.addSelectField(name);
			} else {
				String mode = field.getMode();
				if (mode.equalsIgnoreCase("ro")) {
					hQuery.addSelectField(name);
				} else {
					LOG.warn("未知存储方式:" + name);
					continue;
				}
			}
		}

		if (query.qStr.length() > 0) {
			SimpleQueryParser parser = new SimpleQueryParser("text");
			try {
				Query q = parser.parse(query.qStr);
				if ("ro".equalsIgnoreCase(HDBUtil.getModeFromQuery(q, schema))) {
					hQuery.setWhereClauses2(q);
					if (!uniqueSelected) {
						hQuery.addSelectField(schema.getUniqueKey().getName());
					}
				}
			} catch (Exception ignore) {}
		}

		if (query.orderByStr != null) {
			String[] orderFields = query.orderByStr.split(",");
			for (String item : orderFields) {
				String[] arr = item.split(" ");
				String name = arr[0];
				String op = "asc";
				if (arr.length > 1)	op = arr[1];

				if (name == null || name.length() == 0) continue;
				
				Field field = schema.getField(name);
				if (field == null) {
					LOG.info("非静态字段:" + name);
					field = schema.getDynamicField(name);
					if (field == null) {
						LOG.warn("没有定义的字段:" + name);
						continue;
					}
				}
				String mode = field.getMode();
				if (mode == null) {
					hQuery.addSortField(name, op);
				} else if (mode.equalsIgnoreCase("ro")) {
					hQuery.addSortField(name, op);
				} else {
					LOG.warn("未知存储方式:" + name);
					continue;
				}
			}
		}

		if (query.highlightFields != null && query.highlightFields.length() > 0) {
			String[] highlightFields = query.highlightFields.split(",");
			for (String fieldName : highlightFields) {
				Field field = schema.getField(fieldName);
				if (field == null) {
					field = schema.getDynamicField(fieldName);
				}
				if (field == null) {
					String msg = "Not defined highlight field : " + fieldName; 
					LOG.warn(msg);
					throw new HQueryException(msg);
				}

				String type = field.getType();
				if (!"text".equalsIgnoreCase(type)) {
					String msg = "Highlight field type must be text! " + fieldName; 
					LOG.warn(msg);
					throw new HQueryException(msg);
				}
				if (!"ro".equalsIgnoreCase(field.getMode())) {
					String msg = "Highlight field only support for RO mode! " + fieldName; 
					LOG.warn(msg);
					throw new HQueryException(msg);
				}
				hQuery.addHighlightField(fieldName);
			}
		}
		hQuery.setHighlightMode(query.highlightMode);

		int start = query.rowStart;
		int rowCount = query.rowCount;

		hQuery.setRowStart(start);
		hQuery.setRowCount(rowCount);

		HQuery q = new HQuery();
		q.setSolrQuery(hQuery);

		return q;
	}
}
