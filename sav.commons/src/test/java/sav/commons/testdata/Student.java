package sav.commons.testdata;

import java.util.ArrayList;
import java.util.List;

public class Student {
	private String name;
	private List<Teacher> teachers = new ArrayList<>();
	
	public Student(String name) {
		super();
		this.name = name;
	}

	public List<Teacher> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<Teacher> teachers) {
		this.teachers = teachers;
	}
	
	public void addTeacher(Teacher t){
		this.teachers.add(t);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
