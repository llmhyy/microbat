package mutation.mutator;

import java.io.File;
import java.io.IOException;

import sav.common.core.Constants;
import sav.common.core.SavRtException;

public class MutationUtils {

	public static String getMutationFolderPathPrjRelevant(String srcFolder, String evaluationFolder) {
		try {
			srcFolder.replace("/", Constants.FILE_SEPARATOR);
			int srcStartIdx = srcFolder.indexOf(Constants.FILE_SEPARATOR + "src");
			String projName = "mutation";
			if (srcStartIdx >= 0) {
				projName = srcFolder.substring(0, srcStartIdx);
				projName = projName.substring(projName.lastIndexOf(Constants.FILE_SEPARATOR)+1, projName.length());
				if(projName.length() < 5){ //?
					projName = "mutation";
				}
			}
			
			File tmpFile = new File(evaluationFolder);
			if(!tmpFile.exists()){
				tmpFile.mkdirs();
			}
			
			File file;
			if(evaluationFolder != null){
				file = File.createTempFile(projName, "", new File(evaluationFolder));
			}
			else{
				file = File.createTempFile(projName, "");
			}
			 
			String path = file.toString();
			path = path.substring(0, path.indexOf(projName)+projName.length());
			file = new File(path);
			file.delete();
			file.mkdir();
			
			return file.getAbsolutePath();
		} catch (IOException e) {
			throw new SavRtException("cannot create temp dir");
		}
	}
}
