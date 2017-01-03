package org.iipg.hurricane.db.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 *
 */
/**
 * @author Administrator
 *
 */
public class Schema {
	//schema的名称
	  private String name;
	//schema的描述
	  private String desc;
	 //schema的版本 
	  private String version;

	  private List<Field> fields=new ArrayList();

    /**
	 * @return 获取唯一键值
	 */
	public String getUniqueKey(){
		  return null;
	}
	
	/**
	 * @return 获取所有字段
	 */
	public List<Field> getAllField(){
		return fields;
	}
	
	/**
	 * @param 获取含有指定属性的所有字段
	 * @return
	 */
	public List<Field> getFieldsByPro(String pro){
		return null;
	}
	/**
	 * @return 获取所有字段名称
	 */
	public List<String> getFieldNameList(){
		return null;
	}
	
	/**
	 * @param fieldname 字段名称
	 * @return 获取指定名称的字段
	 */
	public Field getField(String fieldname) {
		return null;
		
	}
	/**
	 * @param fieldName 字段名称
	 * @return 获取指定字段的的所有属性
	 */
	public Map getProByFieldName(String fieldName){
		return null;
	}

}
