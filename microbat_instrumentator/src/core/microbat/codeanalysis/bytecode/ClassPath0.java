package microbat.codeanalysis.bytecode;

import java.io.IOException;
import java.io.InputStream;

import org.apache.bcel.util.ClassPath;

public class ClassPath0 extends ClassPath {
	public ClassPath0(String cp) {
		super(cp);
	}

	@Override
	public InputStream getInputStream(final String name, final String suffix) throws IOException {
		return getClassFile(name, suffix).getInputStream();
	}
}
