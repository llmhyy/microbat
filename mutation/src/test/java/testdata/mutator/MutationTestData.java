/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package testdata.mutator;

/**
 * @author LLT
 *
 */
public class MutationTestData {
	
	public double run() {
        int a = 3 + 2 - 1;
        double b = 3.5;
        double c = a - b / 2 + 4;
        int d = 10;
        int t = 100;
        
        while (a < 3)
        {
            a = (int)(b - 2 + a / 3);
            c = c - 3;
            if (c < 10)
                break;
            else
                c = c + 1;
        }
        return a + b + c;
    }
}
