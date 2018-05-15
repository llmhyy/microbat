package microbat.instrumentation.output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class ByteConverter {

	public static byte[] convertToBytes(Object object) throws IOException {
		byte[] bytes;
		ByteArrayOutputStream bos = null;
		ObjectOutput out = null;
		try {
			bos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bos);
			out.writeObject(object);
			bytes = bos.toByteArray();
		} finally {
			if (bos != null) {
				bos.close();
			}
			if (out != null) {
				out.close();
			}
		}
		return bytes;
	}
	

	public static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		Object object;
		ByteArrayInputStream bis = null;
        ObjectInput in = null;
		try{
			bis = new ByteArrayInputStream(bytes);
	        in = new ObjectInputStream(bis);
	        object = in.readObject();
		} finally{
			if (bis != null) {
				bis.close();
			}
			if (in != null) {
				in.close();
			}
		}
        return object;
	}

}
