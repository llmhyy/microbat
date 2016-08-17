package sav.commons.testdata;


public class Main {
//	private boolean tag = false;
	
	public static void main(Integer element){
		Main main = new Main();
		String input = "<a href=\"test\">test</a>";
		String output = main.removeTag(input);
		System.out.println(output);
	}
	
	private Tag tag = new Tag();
	
	public String removeTag(String htmlText){
		String output = "";
//		int[] a = new int[htmlText.length()];
		
		Teacher t1 = new Teacher("t1");
		Teacher t2 = new Teacher("t2");
		
		Student s1 = new Student("s1");
		Student s2 = new Student("s2");
		Student s3 = new Student("s3");
		
		t1.addStudent(s1);
		s1.addTeacher(t1);
		
		t1.addStudent(s2);
		s2.addTeacher(t1);
		
		t2.addStudent(s2);
		s2.addTeacher(t2);
		
		t2.addStudent(s3);
		s3.addTeacher(t2);
		
		char[] cList = htmlText.toCharArray();
		for(int i=0; i<cList.length; i++){
//			Tag tag = this.tag;
			
			char c = cList[i];
			char tmp = c;
			if(tag.isTag()){
				tmp = '*';
			}
			
			output = modifyOutput(output, c, tmp);
			
//			a[cList.length-1-i] = (int)c;
//			if(a[i] > 0){
//				output = output + cList[i];
//			}
		}
		return output;
	}
	
	private String modifyOutput(String output, char c, char tmp){
//		output = output;
//		c = c;
		
		String newOutput = output;
		
		if(c == '<'){
//			tag = true;
			tag.setTag(true);
		}
		else if(c == '>'){
//			tag = false;
			tag.setTag(false);
		}
		else if(!tag.isTag()){
			newOutput = output + c + tmp;
		}
		
//		if(!tag){
//			newOutput = output + c;
//		}
		
		return newOutput;
	}
}
