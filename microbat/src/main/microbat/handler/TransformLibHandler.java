package microbat.handler;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import microbat.Activator;
import microbat.preference.MicrobatPreference;

public class TransformLibHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String javaHomePath = Activator.getDefault().getPreferenceStore().getString(MicrobatPreference.JAVA7HOME_PATH);
		String jarFile = javaHomePath + File.separator + "jre" + File.separator + "lib" + File.separator + "rt.jar";
		
		File rtJarFile = new File(jarFile);
		if(rtJarFile.exists()){
			try {
				JarFile jar = new JarFile(rtJarFile);
				Enumeration<? extends JarEntry> enumeration = jar.entries();
				while(enumeration.hasMoreElements()){
					ZipEntry zipEntry = enumeration.nextElement();
					if(zipEntry.getName().endsWith(".class")){
						String className = zipEntry.getName();
						if(className.contains("java/util/ArrayList")){
							
							System.currentTimeMillis();
						}
					}
				}
				 
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			
		}
		
		return null;
	}

	
}
