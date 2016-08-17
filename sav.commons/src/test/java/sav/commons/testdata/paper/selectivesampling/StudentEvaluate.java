/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.paper.selectivesampling;

/**
 * @author LLT
 * 
 */

public class StudentEvaluate {

	public static void evaluate(int[] ids, int[] scores) {
		Student alice = new Student(ids[0], scores[0]);
		Student bob = new Student(ids[1], scores[1]);
		Student cathy = new Student(ids[2], scores[2]);
		Student[] list = new Student[3];
		list[0] = alice;
		list[1] = bob;
		list[2] = cathy;
		standardize(list);

		assert (cathy.standardScore <= 100);
	}
	
	public static void lalala (Student s1, Student s2, Student s3) {
		Student[] list = new Student[]{s1,s2,s3};
		standardize(list);
		
		assert(s3.standardScore <= 100);
	}

	private static void standardize(Student[] students) {
		int max = Integer.MIN_VALUE;
		int i = 0;
		for (; i < students.length - 1; i++) {
			if (max < students[i].score) {
				max = students[i].score;
			}
		}
		for (Student stu : students) {
			int a = (100 - max) + stu.score;
			assert a > 0;
			stu.standardScore = Math.sqrt(a) * 10;
		}
	}
	
	public static class Student {
		int score;
		int ID;
		double standardScore;
		
		public Student(int id, int s) {
			ID = id;
			score = s;
		}
	}
}

