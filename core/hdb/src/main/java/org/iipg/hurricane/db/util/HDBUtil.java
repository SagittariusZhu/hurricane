package org.iipg.hurricane.db.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iipg.hurricane.HAdapterException;
import org.iipg.hurricane.HQueryException;
import org.iipg.hurricane.db.HAdapter;
import org.iipg.hurricane.db.HAdapterFactory;
import org.iipg.hurricane.db.metadata.HDBBaseObject;
import org.iipg.hurricane.db.metadata.HDBBlob;
import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.metadata.HDBResultSet;
import org.iipg.hurricane.db.query.*;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.SchemaParser;
import org.iipg.hurricane.model.HMWDocument;
import org.iipg.hurricane.model.HMWQuery;
import org.iipg.hurricane.search.Query;
import org.iipg.hurricane.util.HDocBuilder;
import org.iipg.hurricane.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HDBUtil {
	
	public static final Log LOG = LogFactory.getLog(HDBUtil.class);
	
//	public static HDocument record2Document(HDBRecord record) {
//		HDocument doc = new HDocument();
//		Map props = record.getInformation();
//		for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
//			String fieldName = (String) it.next();
//			Object fieldValue = props.get(fieldName);
//			doc.setField(fieldName, fieldValue);
//		}
//		doc.setBinary(record.getBinary());
//		return doc;
//	}
	
	public static void checkAdapter(HAdapter adapter) throws HAdapterException {
		
		if (adapter == null) {
			throw new HAdapterException("Adapter is null");
		}
		
	}

	public static HDBQuery parseSimpleQuery(HMWQuery q) {
		String schemaName = q.schema;
		HDBQuery roQuery = HAdapterFactory.createSolrAdapter(schemaName).createQuery();
		roQuery.addWhereClause(new HDBEqualCondition("uuid", q.uuid));
		roQuery.addWhereClause(new HDBEqualCondition("item", q.item));
		return roQuery;
	}
	
	public static HDBCondition buildCondition(Map clause) {
		String fieldName = (String) clause.get("field");
		String op = (String) clause.get("op");
		
		HDBCondition condition = null;
		Object fieldValue = null;
		if ("eq".equalsIgnoreCase(op)) {
			fieldValue = clause.get("value");
			condition = new HDBEqualCondition(fieldName, fieldValue);
		} else if ("ge".equalsIgnoreCase(op)) {
			fieldValue = clause.get("value");
			condition = new HDBGreatOrEqualCondition(fieldName, fieldValue);
		} else if ("gt".equalsIgnoreCase(op)) {
			fieldValue = clause.get("value");
			condition = new HDBGreatCondition(fieldName, fieldValue);
		} else if ("le".equalsIgnoreCase(op)) {
			fieldValue = clause.get("value");
			condition = new HDBLessOrEqualCondition(fieldName, fieldValue);
		} else if ("lt".equalsIgnoreCase(op)) {
			fieldValue = clause.get("value");
			condition = new HDBLessCondition(fieldName, fieldValue);
		} else if ("like".equalsIgnoreCase(op)) {
			fieldValue = clause.get("value");
			condition = new HDBLikeCondition(fieldName, fieldValue);
		} else if ("between".equalsIgnoreCase(op)) {
			Object lowerValue = clause.get("lvalue");
			Object upperValue = clause.get("uvalue");
			condition = new HDBSpanCondition(fieldName, lowerValue, upperValue, true, true);
		} else if ("in".equalsIgnoreCase(op)) {
			fieldValue = clause.get("value");
			condition = new HDBInCondition(fieldName, fieldValue);
		}
		
		return condition;
	}
	
	public static void printResultSet(HDBResultSet rset) {
		System.out.println("Got " + rset.getCount() + " rows.");
		for (int i=0; i<rset.getCount(); i++) {
			System.out.println("--------------");
			HDBBaseObject item = rset.getItem(i);
			if (item instanceof HDBRecord) {
				printHDBRecord((HDBRecord) item);
			} else {
				printHDBBlob((HDBBlob) item);
			}
		}
	}
	
	public static void printHDBRecord(HDBRecord item) {
		Map props = item.getInformation();
		for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
			String fieldName = (String) it.next();
			System.out.println(StringUtil.formatLeftS(fieldName, 14) + " :\t" + props.get(fieldName));
		}
	}
	
	public static void printHDBBlob(HDBBlob item) {
		System.out.println("Byte buffer : " + item.getBlob().length + " bytes.");
	}

	public static HDBUpdateQuery buildUpdateQuery(SchemaParser schema, HDBRecord obj,
			List<String> list, HAdapter adapter) {
		HDBUpdateQuery uQuery = adapter.createUpdateQuery();
		uQuery.setSchema(schema);
		uQuery.setSchemaName(schema.getName());
		uQuery.parse(obj);
		String uuidField = schema.getUniqueKey().getName();
		uQuery.addWhereClause(new HDBInCondition(uuidField, list));
		
		return uQuery;
	}

	public static HMWDocument toHMWDocument(HDBRecord hDoc) {
		HMWDocument doc = new HMWDocument();
		doc.blob = hDoc.getBinary();
		doc.dict = HDocBuilder.build(hDoc.getInformation());
		
		return doc;
	}

	public static String getModeFromQuery(Query q, SchemaParser schema) throws JSONException {
		JSONObject qObj = q.getJSONObject();
		
		String mode = parseQuery(qObj, schema);
		if (mode.length() == 0) mode = "ro";
		
		return mode;
	}

	private static String parseQuery(JSONObject qObj, SchemaParser schema) throws JSONException {
		String ret = "";
		if (qObj.has("clauses")) {
			ret = parseClauses((JSONArray) qObj.get("clauses"), schema);
		} else if (qObj.has("AND")) {
			ret = parseQuery(qObj.getJSONObject("AND"), schema);
		} else if (qObj.has("OR")) {
			ret = parseQuery(qObj.getJSONObject("OR"), schema);			
		} else if (qObj.has("NOT")) {
			ret = parseQuery(qObj.getJSONObject("NOT"), schema);			
		} else {
			String type = qObj.getString("type");
			if ("TermQuery".equals(type)) {
				ret = parseTermQuery(qObj, schema);
			} else if ("PrefixQuery".equals(type)) {
				ret = parseTermQuery(qObj, schema);	
			} else if ("WildcardQuery".equals(type)) {
				ret = parseTermQuery(qObj, schema);				
			} else if ("TermRangeQuery".equals(type)) {
				ret = parseTermQuery(qObj, schema);
			} else if ("MatchAllDocsQuery".equals(type)) {
				ret = "";
			} else {
				throw new JSONException("Unknow query type : " + type);
			}
		}
		return ret;
	}
	
	private static String parseClauses(JSONArray list, SchemaParser schema) throws JSONException {
		if (list.length() < 1) return "";		
		JSONObject qObj = list.getJSONObject(0);
		
		if (list.length() == 1) {
			return parseQuery(qObj, schema);
		}
		
		String mode = parseQuery(qObj, schema);
		for (int i=1; i < list.length(); i++) {
			qObj = list.getJSONObject(i);
			String anotherMode = parseQuery(qObj, schema);
			if (anotherMode.length() > 0 && !mode.equals(anotherMode)) {
				throw new HQueryException("Query Condition only Support one mode pre once.");
			}
		}
		return mode;
	}
	
	private static String parseTermQuery(JSONObject qObj, SchemaParser schema) throws JSONException {
		String fieldName = qObj.getString("name");
		
		//support general field text
		if ("text".equals(fieldName) || fieldName.length() == 0) {
			return "ro";
		}
		
		if (fieldName.indexOf(".") >= 0) {
			String[] arr = fieldName.split("\\.");
			fieldName = arr[0];
		}

		Field field = schema.getField(fieldName);
		if (field == null) {
			field = schema.getDynamicField(fieldName);
		}
		if (field == null) {
			LOG.warn("没有定义的字段:" + fieldName);
			return "";
		}

		String mode = field.getMode();
		if (mode == null) {
			return "ro";
		} else {
			return mode;
		}
	}
}
