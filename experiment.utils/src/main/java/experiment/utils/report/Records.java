/**
 * 
 */
package experiment.utils.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sav.common.core.utils.StringUtils;

/**
 * @author LLT
 *
 */
public class Records {
	private Map<String, Integer> columnHeaderMap;
	private Map<String, Record> allRecords;
	private int[] keyCols;
	private List<String> mergedHeaders;
	
	private Records(List<String> mergedHeaders) {
		columnHeaderMap = new HashMap<String, Integer>();
		allRecords = new LinkedHashMap<String, Records.Record>();
		for (int i = 0; i < mergedHeaders.size(); i++) {
			columnHeaderMap.put(mergedHeaders.get(i), i);
		}
		this.mergedHeaders = mergedHeaders;
	}
	
	public Records(List<String> headers, int[] keyCols) {
		this(headers);
		this.keyCols = keyCols;
	}
	
	public Records(List<String> mergedHeaders, List<String> keyHeaders) {
		this(mergedHeaders);
		this.keyCols = new int[keyHeaders.size()];
		for (int i = 0; i < keyHeaders.size(); i++) {
			this.keyCols[i] = columnHeaderMap.get(keyHeaders.get(i));
		}
	}
	
	public List<String> getHeaders() {
		return mergedHeaders;
	}
	
	public void addRecord(List<Object> cellValues) {
		Record record = new Record(cellValues);
		allRecords.put(record.getKeyString(), record);
	}
	
	/* key must be unique */
	public Map<String, Record> groupByKey(List<Record> records, int[] keyCols) {
		Map<String, Record> recordMap = new LinkedHashMap<String, Records.Record>();
		for (Record record : records) {
			recordMap.put(record.getKeyString(), record);
		}
		return recordMap;
	}
	
	public Set<String> getKeys() {
		return allRecords.keySet();
	}
	
	public List<Record> getRecords(List<String> keys) {
		List<Record> result = new ArrayList<Records.Record>(keys.size());
		for (String key : keys) {
			result.add(allRecords.get(key));
		}
		return result;
	}
	
	public Record getRecord(String key) {
		return allRecords.get(key);
	}
	
	public Collection<Record> getRecords() {
		return allRecords.values();
	}
	
	public Map<String, Record> getAllRecords() {
		return allRecords;
	}
	
	public int[] toColumnIdx(List<String> headers) {
		int[] idexies = new int[headers.size()];
		int i = 0;
		for (String header : headers) {
			idexies[i++] = columnHeaderMap.get(header);
		}
		return idexies;
	}
	
	public class Record {
		private List<Object> cellValues;
		
		public Record(List<Object> cellValues) {
			this.cellValues = cellValues;
		}

		public String getKeyString() {
			Object[] keyVals = new Object[keyCols.length];
			for (int i = 0; i < keyCols.length; i++) {
				keyVals[i] = cellValues.get(keyCols[i]);
			}
			return StringUtils.join("_", keyVals);
		}
		
		public String[] getKeyStringArr() {
			String[] keyVals = new String[keyCols.length];
			for (int i = 0; i < keyCols.length; i++) {
				keyVals[i] = String.valueOf(cellValues.get(keyCols[i]));
			}
			return keyVals;
		}

		public String getStringValue(String col) {
			return StringUtils.toStringNullToEmpty(cellValues.get(columnHeaderMap.get(col)));
		}
		
		public String getStringValue(int col) {
			Object value = null;
			if (col < cellValues.size()) {
				value = cellValues.get(col);
			}
			return StringUtils.toStringNullToEmpty(value);
		}
		
		public Object getValue(String col) {
			return getValue(columnHeaderMap.get(col));
		}
		
		public Object getValue(int col) {
			if (col < cellValues.size()) {
				return cellValues.get(col);
			}
			return null;
		}
		
		public List<Object> getCellValues() {
			return cellValues;
		}
		
		public List<String> getHeaders() {
			return Records.this.getHeaders();
		}

		public Records getRecords() {
			return Records.this;
		}
		
		public int getColIdx(String header) {
			return Records.this.columnHeaderMap.get(header);
		}

		public List<Object> getCellValues(String[] mergedHeaders) {
			List<Object> values = new ArrayList<>(mergedHeaders.length);
			for (String header : mergedHeaders) {
				values.add(getValue(header));
			}
			return values;
		}
	}

}
