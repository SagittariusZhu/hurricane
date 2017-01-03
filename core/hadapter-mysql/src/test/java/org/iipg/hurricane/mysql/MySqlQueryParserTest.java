package org.iipg.hurricane.mysql;

import org.iipg.hurricane.mysql.impl.SqlBuilder;
import org.iipg.hurricane.qparser.SimpleQueryParser;
import org.iipg.hurricane.search.Query;
import org.json.JSONException;
import org.junit.Test;

public class MySqlQueryParserTest {
	@Test
	public void run() {
		String qStr = "+readtime:(zhangsan zhu) +readtime:[2014-11-01 TO 2015-01-01]";
		SimpleQueryParser parser = new SimpleQueryParser("text");
		try {
			Query q = parser.parse(qStr);
			System.out.println(q);
			System.out.println(SqlBuilder.getMysqlSql(q));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.iipg.hurricane.qparser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
