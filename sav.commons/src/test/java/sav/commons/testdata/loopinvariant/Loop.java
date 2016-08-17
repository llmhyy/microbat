/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata.loopinvariant;

/**
 * This example is from discussion with Tung
 * @author khanh
 *
 */
public class Loop {

	public static boolean testLoop(int x, int y, int N){
		if (N < 0)
			return true;
		
		while (true) {
			if (x <= N)
				y++;
			else if (x >= N + 1)
				y--;
			else
				return true;

			if (y < 0)
				break;
			x++;
		}

		if (N >= 0)
			if (y == -1)
				if (x >= 2 * N + 3)
					return false;

		return true;
	}
	
	public static boolean validateTestLoop(int x, int y, int N, boolean result) {
		return result;
	}
}
