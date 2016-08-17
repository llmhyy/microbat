/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.paper.selectivesampling;

import org.junit.Test;

import sav.commons.testdata.paper.selectivesampling.StudentEvaluate.Student;

/**
 * @author LLT
 *
 */
public class StudentEvaluateTest2 {
	
	@Test
	public void test1() {
		Student s1 = new Student(1, 94);
		Student s2 = new Student(2, 60);
		Student s3 = new Student(3, 100);
		StudentEvaluate.lalala(s1, s2, s3);
	}
	
	@Test
	public void test2() {
		Student s1 = new Student(3, 75);
		Student s2 = new Student(2, 90);
		Student s3 = new Student(1, 80);
		StudentEvaluate.lalala(s1, s2, s3);
	}
	
	@Test
	public void test3() {
		Student s1 = null;
		Student s2 = null;
		Student s3 = null;
		StudentEvaluate.lalala(s1, s2, s3);
	}
	
	@Test
	public void test4() {
		Student s1 = new Student(99, -33);
		Student s2 = new Student(-100, 12);
		Student s3 = new Student(0, 0);
		StudentEvaluate.lalala(s1, s2, s3);
	}
}
