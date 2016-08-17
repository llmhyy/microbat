package sav.commons.testdata;

import java.util.ArrayList;
import java.util.List;

public class Teacher {
	private String name;
	private List<Student> students = new ArrayList<>();
	
	public Teacher(String name) {
		super();
		this.name = name;
	}

	public List<Student> getStudents() {
		return students;
	}

	public void setStudents(List<Student> students) {
		this.students = students;
	}
	
	public void addStudent(Student s){
		this.students.add(s);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
