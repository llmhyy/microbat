package microbat.instrumentation.instr;

import java.util.HashMap;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantMethodType;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantObject;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.ConstantPoolGen;

import microbat.model.trace.ConstWrapper;

/*
 * Trims a constant pool so that we are able to store 
 * and search from it more efficiently.
 */
public class ConstantPoolTrimmer {
	public static HashMap<Integer, ConstWrapper> trim(ConstantPoolGen cpg) {
		HashMap<Integer, ConstWrapper> result = new HashMap<>();
		ConstantPool cp = cpg.getConstantPool();
		Constant[] constants = cp.getConstantPool();
		for (int i = 0; i < constants.length; i++) {
			Constant c = constants[i];
			if (c == null || c instanceof ConstantUtf8 || c instanceof ConstantNameAndType) {
				continue;
			} else if (c instanceof ConstantObject) {
				ConstantObject co = (ConstantObject) c;
				Object o = co.getConstantValue(cp);
				result.put(i, new ConstWrapper(c.getTag(), o.toString()));
			} else if (c instanceof ConstantCP) {
				ConstantCP ccp = (ConstantCP) c;
				int name_type_index = ccp.getNameAndTypeIndex();
				String class_name = ccp.getClass(cp);
				ConstantNameAndType cnt = (ConstantNameAndType) cp.getConstant(name_type_index, Const.CONSTANT_NameAndType);
				String name = cnt.getName(cp);
				String type = cnt.getSignature(cp);
				result.put(i, new ConstWrapper(c.getTag(), class_name, name, type));
			} else if (c instanceof ConstantMethodType) {
				ConstantMethodType cmt = (ConstantMethodType) c;
				int descriptor_index = cmt.getDescriptorIndex();
				ConstantUtf8 descriptor = (ConstantUtf8) cp.getConstant(descriptor_index, Const.CONSTANT_Utf8);
				result.put(i, new ConstWrapper(c.getTag(), descriptor.getBytes()));
			} else {
				System.err.println("UnsupportedType:" + c.toString());
			}
		}
		return result;
	}
}
