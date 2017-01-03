package org.iipg.hurricane.solr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.iipg.hurricane.db.query.HDBCondition;
import org.iipg.hurricane.db.query.HDBSpanCondition;
import org.iipg.hurricane.util.TimeTool;

public class QueryClauseBuilder {
	
	private static SimpleDateFormat sdfSolr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");

	public String toStringClauseForHDBEqualCondition(HDBCondition cond) {
		String newStr = (String) cond.getFieldValue();
		newStr = newStr.replaceAll("\\:", "\\\\:");
		return newStr;
	}
	
	public String toDateClauseForHDBEqualCondition(HDBCondition cond) {
		try {
			Date d = TimeTool.parse((String) cond.getFieldValue());
			return "[" + sdfSolr.format(d) + " TO " + sdfSolr.format(d) + "]";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public String toDateClauseForHDBGreatCondition(HDBCondition cond) {
		try {
			Date d = TimeTool.parse((String) cond.getFieldValue());
			return "{" + sdfSolr.format(d) + " TO *]";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public String toDateClauseForHDBGreatOrEqualCondition(HDBCondition cond) {
		try {
			Date d = TimeTool.parse((String) cond.getFieldValue());
			return "[" + sdfSolr.format(d) + " TO *]";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public String toDateClauseForHDBLessCondition(HDBCondition cond) {
		try {
			Date d = TimeTool.parse((String) cond.getFieldValue());
			return "[* TO " + sdfSolr.format(d) + "}";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public String toDateClauseForHDBLessOrEqualCondition(HDBCondition cond) {
		try {
			Date d = TimeTool.parse((String) cond.getFieldValue());
			return "[* TO " + sdfSolr.format(d) + "]";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public String toDateClauseForHDBSpanCondition(HDBCondition c) {
		HDBSpanCondition cond = (HDBSpanCondition) c;
		String lower = cond.getLowerValue().toString();
		String upper = cond.getUpperValue().toString();
		try {
			Date d1 = TimeTool.parse(lower);
			Date d2 = TimeTool.parse(upper);
			return (cond.isIncludeLower() ? "[" : "{") 
					+ sdfSolr.format(d1) + " TO " + sdfSolr.format(d2) 
					+ (cond.isIncludeUpper() ? "]" : "}");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	// string type
	public String toStringClauseForHDBLikeCondition(HDBCondition cond) {
		return (String) cond.getFieldValue();
	}
	
	public String toStringClauseForHDBSpanCondition(HDBCondition c) {
		HDBSpanCondition cond = (HDBSpanCondition) c;
		String lower = cond.getLowerValue().toString();
		String upper = cond.getUpperValue().toString();
		return (cond.isIncludeLower() ? "['" : "{'") 
				+ lower + "' TO '" + upper 
				+ (cond.isIncludeUpper() ? "']" : "'}");
	}
}
