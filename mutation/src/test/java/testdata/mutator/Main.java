package testdata.mutator;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Main {
	private static boolean tag = false;
	
	public String removeTag(String htmlText){
		String output = "";
		
		char[] cList = htmlText.toCharArray();
		char[] tmp = cList;
		
		for(int i=0; i<htmlText.length(); i++){
			tmp[i] = cList[i];
			char c = tmp[i];
			
			output = modifyOutput(output, c);
			
			System.currentTimeMillis();
		}
		return output;
	}
	
	private String modifyOutput(String output, char c){
		
		String newOutput = output;
		
		if(c == '<'){
			tag = true;
		}
		else if(c == '>'){
			tag = false;
		}
		else if(!tag){
			newOutput = newOutput + c;
		}
		
		return newOutput;
	}
	
	
	
	public static void main(String[] args){
		Main main = new Main();
		String input = "<a href=\">\">test</a>";
		String output = main.removeTag(input);
		System.out.println(output);
	}
	
	interface InnerClass{
		public void run();
	}
	
	public static String readFile() throws IOException{
		File file = new File("resource//test.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String content = "";
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    
		    content = sb.toString();
		    
		} finally {
		    br.close();
		}
		
		return content;
	}
}
