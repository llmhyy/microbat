package microbat.filedb.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.output.ByteConverter;

/**
 * @author LLT
 *
 */
public class DataFile extends RandomAccessFile {

	public DataFile(File file, AccessMode mode) throws FileNotFoundException {
		super(file, mode.mode);
	}

	public DataFile(String name, AccessMode mode) throws FileNotFoundException {
		super(name, mode.mode);
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

	public <K extends Serializable, V> void writeSerializableMap(Map<K, V> map) throws IOException {
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

	public String readString() throws IOException {
		int len = readVarInt();
		if (len == -1) {
			return null;
		} else if (len == 0) {
			return "";
		} else {
			byte[] bytes = new byte[len];
			readFully(bytes);
			return new String(bytes);
		}
	}

	public byte[] readByteArray() throws IOException {
		int len = readVarInt();
		if (len == -1) {
			return null;
		} else if (len == 0) {
			return new byte[0];
		} else {
			byte[] bytes = new byte[len];
			readFully(bytes);
			return bytes;
		}
	}

	public int readVarInt() throws IOException {
		final int value = 0xFF & readByte();
		if ((value & 0x80) == 0) {
			return value;
		}
		return (value & 0x7F) | (readVarInt() << 7);
	}

	public List<Integer> readListInt() throws IOException {
		int size = readVarInt();
		if (size == -1) {
			return null;
		}
		List<Integer> list = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			list.add(readVarInt());
		}
		return list;
	}

	public List<String> readListString() throws IOException {
		int size = readVarInt();
		if (size == -1) {
			return null;
		}
		List<String> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(readString());
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	protected <T> T readSerializableObj() throws IOException {
		byte[] bytes = readByteArray();
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			return (T) ByteConverter.convertFromBytes(bytes);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> readSerializableList() throws IOException {
		int size = readVarInt();
		if (size == 0) {
			return new ArrayList<>(0);
		}
		byte[] bytes = readByteArray();
		if (bytes == null || bytes.length == 0) {
			return new ArrayList<>(0);
		}
		List<T> list;
		try {
			list = (List<T>) ByteConverter.convertFromBytes(bytes);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	protected <K, V> Map<K, V> readSerializableMap() throws IOException {
		int size = readVarInt();
		if (size == 0) {
			return new HashMap<>();
		}
		byte[] bytes = readByteArray();
		if (bytes == null || bytes.length == 0) {
			return new HashMap<>();
		}
		Map<K, V> map;
		try {
			map = (Map<K, V>) ByteConverter.convertFromBytes(bytes);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		return map;
	}

	public static enum AccessMode {
		READ_ONLY("r"), READ_WRITE("rw");

		private String mode;

		private AccessMode(String mode) {
			this.mode = mode;
		}
	}
}
