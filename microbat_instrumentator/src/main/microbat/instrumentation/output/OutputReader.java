package microbat.instrumentation.output;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OutputReader extends DataInputStream {

	public OutputReader(InputStream in) {
		super(in);
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
}
