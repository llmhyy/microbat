package microbat.instrumentation.output;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OutputWriter extends DataOutputStream {

	public OutputWriter(OutputStream out) {
		super(out);
	}
	
	public final void writeByteArr(byte[] bytes) throws IOException {
		if (bytes == null) {
			writeVarInt(-1);
		} else if (bytes.length == 0) {
			writeVarInt(0);
		} else {
			writeVarInt(bytes.length);
			write(bytes, 0, bytes.length);
		}
	}
	
	public void writeVarInt(final int value) throws IOException {
		if ((value & 0xFFFFFF80) == 0) {
			writeByte(value);
		} else {
			writeByte(0x80 | (value & 0x7F));
			writeVarInt(value >>> 7);
		}
	}
	
	public <K extends Serializable, V> void writeSerializableMap(Map<K, V> map)
			throws IOException {
		if (map == null || map.isEmpty()) {
			writeVarInt(0);
		} else {
			writeVarInt(map.size());
			byte[] bytes = ByteConverter.convertToBytes(map);
			writeByteArr(bytes);
		}
	}
	
	public <T extends Serializable> void writeSerializableList(Collection<T> list) throws IOException {
		if (list == null || list.isEmpty()) {
			writeVarInt(0);
		} else {
			writeVarInt(list.size());
			byte[] bytes = ByteConverter.convertToBytes(list);
			writeByteArr(bytes);
		}
	}
	
	public <T> void writeSerializableObj(T obj) throws IOException {
		byte[] bytes = ByteConverter.convertToBytes(obj);
		writeByteArr(bytes);
	}
	
	public final void writeString(String str) throws IOException {
		if (str == null || str.isEmpty()) {
			writeVarInt(0);
		} else {
			writeVarInt(str.length());
			writeBytes(str);
		}
	}
	
	public synchronized void writeListInt(List<Integer> list) throws IOException {
		if (list == null || list.isEmpty()) {
			writeVarInt(0);
		} else {
			writeVarInt(list.size());
			for (Integer value : list) {
				if (value == null) {
					System.out.println(list);
				}
				writeVarInt(value);
			}
		}
	}
	
	public void writeListString(List<String> list) throws IOException {
		if (list == null) {
			writeVarInt(-1);
		} else if (list.isEmpty()) {
			writeVarInt(0);
		} else {
			writeVarInt(list.size());
			List<String> values = new ArrayList<>(list);
			for (String value : values) {
				writeString(value);
			}
		}
	}
	
	public void writeSize(Collection<?> col) throws IOException {
		if (col == null) {
			writeVarInt(-1);
		} else if (col.isEmpty()) {
			writeVarInt(0);
		} else {
			writeVarInt(col.size());
		}
	}
}
