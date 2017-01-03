package org.iipg.hurricane.db.schema.ext;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.iipg.hurricane.db.metadata.HDBRecord;
import org.iipg.hurricane.db.schema.Field;
import org.iipg.hurricane.db.schema.HTyper;
import org.iipg.hurricane.util.FileUtil;

public class HFileTyper extends HTyper {
	
	private static String SIZE_FIELD_FLAG = "IS---?----"; 
	private static String NAME_FIELD_FLAG = "IS---?----"; 
	private static String BLOB_FIELD_FLAG = "-S---?----"; 

	
	public HFileTyper(Field ori) {
		super(ori);
	}
	
	@Override
	public Field getDefaultField() {
		String nameFieldName = getOriField().getName() + "_name";
		return createField(nameFieldName, "text_cn",
				mergeFlag(NAME_FIELD_FLAG, getOriField().getFlag()));
	}

	@Override
	public Field[] getFields() {
		List<Field> list = new ArrayList<Field>();
		String oriName = getOriField().getName();
		String sizeFieldName = oriName + "_size";
		String nameFieldName = oriName + "_name";
		String blobFieldName = oriName + "_blob";
		list.add(createField(sizeFieldName, "int", 
				mergeFlag(SIZE_FIELD_FLAG, getOriField().getFlag())));
		list.add(createField(nameFieldName, "text_cn",
				mergeFlag(NAME_FIELD_FLAG, getOriField().getFlag())));
		list.add(createField(blobFieldName, "string", 
				mergeFlag(BLOB_FIELD_FLAG, getOriField().getFlag())));
		return list.toArray(new Field[0]);
	}
	
	@Override
	public Object run(Object value) {
		if (getOriField().isMultiValued()) {
			List ret = new ArrayList();
			List<String> list = (List<String>) value;
			for (String item : list) {
				ret.add(fetchItem(item));
			}
			return ret;
		} else {
			return fetchItem((String) value);
		}
	}
	
	private Map<String, Object> fetchItem(String value) {
		Map<String, Object> ret = new HashMap<String, Object>();

		String oriName = getOriField().getName();
		String sizeFieldName = oriName + "_size";
		String nameFieldName = oriName + "_name";
		String blobFieldName = oriName + "_blob";

		File f = new File(value);
		if (f.exists() && f.isFile()) {
			ret.put(sizeFieldName, f.length());
			ret.put(nameFieldName, f.getName());
			String uuid = UUID.randomUUID().toString();
			setBinary(uuid, FileUtil.read(f));
			ret.put(blobFieldName, uuid);
		}
		
		return ret;
	}

	@Override
	public void convert(HDBRecord record) {
		String oriName = getOriField().getName();
		String sizeFieldName = oriName + "_size";
		String nameFieldName = oriName + "_name";
		String blobFieldName = oriName + "_blob";
		
		if (getOriField().isMultiValued()) {
			List sizeValues = (List) record.get(sizeFieldName);
			List nameValues = (List) record.get(nameFieldName);
			List blobValues = (List) record.get(blobFieldName);
			List newValues = new ArrayList();
			for (int i=0; i<nameValues.size(); i++) {
				int sizeValue = (Integer) sizeValues.get(i);
				String nameValue = (String) nameValues.get(i);
				String blobValue = (String) blobValues.get(i);
				try {
					newValues.add(mergeValue(sizeValue, nameValue, blobValue));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			record.put(oriName, newValues);
		} else {
			int sizeValue = (Integer) record.get(sizeFieldName);
			String nameValue = (String) record.get(nameFieldName);
			String blobValue = (String) record.get(blobFieldName);
			try {
				String newValue = mergeValue(sizeValue, nameValue, blobValue);
				record.put(oriName, newValue);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		record.remove(sizeFieldName);
		record.remove(nameFieldName);
		record.remove(blobFieldName);
	}

	private String mergeValue(int sizeValue, String nameValue, String blobValue) throws UnsupportedEncodingException {
		String value = "size=" + sizeValue
					 + "&name=" + URLEncoder.encode(nameValue, "utf-8") 
					 + "&blob=" + URLEncoder.encode(blobValue, "utf-8");
		return value;
	}

	@Override
	public String getQueryValue(String value) {
		// TODO Auto-generated method stub
		return value;
	}




}
