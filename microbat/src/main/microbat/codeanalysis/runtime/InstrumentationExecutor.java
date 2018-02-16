package microbat.codeanalysis.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import sav.strategies.dto.AppJavaClassPath;

public class InstrumentationExecutor {
	private final static String TEST_CASE_RUNNER = "microbat.evaluation.junit.MicroBatTestRunner";
	
	private AppJavaClassPath appPath;
	
	
	
	public InstrumentationExecutor(AppJavaClassPath appPath){
		this.appPath = appPath;
	}
	
	public void run() {
//		Trace trace = 
	}
	
	
//	public void run(){
//		List<String> command = new ArrayList<>();
//		command.add(this.appPath.getJavaHome()+File.separator+"bin"+File.separator+"java");
//		
//		command.add("-noverify");
//		
//		command.add("-cp");
//		
//		StringBuffer buffer = new StringBuffer();
//		buffer.append(".");
//		for(String cp: appPath.getClasspaths()){
//			buffer.append(";");
//			buffer.append(cp);
//		}
//		String cp = buffer.toString();
//		command.add(cp);
//		
//		buffer = new StringBuffer();
//		buffer.append("-javaagent:");
//		//TODO set agent lib
//		buffer.append(appPath.getAgentLib());
//		buffer.append("=");
//		
//		buffer.append("launch_class=");
//		if(appPath.getOptionalTestClass()!=null){
//			buffer.append(appPath.getOptionalTestClass());
//		}
//		else{
//			buffer.append(appPath.getLaunchClass());			
//		}
//		
//		//Java Home
//		buffer.append(",");
//		buffer.append("java_home=");
//		buffer.append(appPath.getJavaHome());
//		
//		//Class Path
//		buffer.append(",");
//		buffer.append("class_path=.");
//		for(String classPath: appPath.getClasspaths()){
//			buffer.append(";");
//			buffer.append(classPath);
//		}
//		
//		//Working Directory
//		buffer.append(",");
//		buffer.append("working_dir=");
//		buffer.append(appPath.getWorkingDirectory());
//
//		//Bootstrap 
////		buffer.append(",");
////		buffer.append("bootstrap_path=.");
////		for(String path: appPath.getAgentBootstrapPathList()){
////			buffer.append(";");
////			buffer.append(path);
////		}
//		
//		String agentInfo = buffer.toString();
//		command.add(agentInfo);
//		
//		command.add(appPath.getLaunchClass());
//		
//		if(appPath.getOptionalTestMethod()!=null){
//			command.add(appPath.getOptionalTestClass());
//			command.add(appPath.getOptionalTestMethod());
//		}
//		
//		String com = createRunningCommand(command);
//		System.out.println(com);
//		
//		ProcessBuilder builder = new ProcessBuilder(command);
//		builder.directory(new File(this.appPath.getWorkingDirectory()));
//		try {
//			Process process = builder.start();
//			String output = output(process.getInputStream());
//			System.out.println(output);
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	private String createRunningCommand(List<String> command) {
		StringBuffer buffer = new StringBuffer();
		for(String str: command){
			buffer.append(str);
			buffer.append(" ");
		}
		
		return buffer.toString();
	}

	private static String output(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + System.getProperty("line.separator"));
			}
		} finally {
			br.close();
		}
		return sb.toString();
	}

	public AppJavaClassPath getAppPath() {
		return appPath;
	}

	public void setAppPath(AppJavaClassPath appPath) {
		this.appPath = appPath;
	}
}
