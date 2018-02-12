/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.commons.testdata;

/**
 * @author khanh
 *
 */
import java.util.Scanner;

public class Cipher {
	static char ONE = '1';
	static char ZERO = '0';
	public static void decode(String[] args){

		Scanner in = new Scanner(System.in);
		int N = in.nextInt();
		int K = in.nextInt();
		in.nextLine();
		
		String line = in.nextLine();
		boolean[] encryption = new boolean[N];
		for(int i = 0; i < N; i++){
			encryption[i] = (line.charAt(i) == ONE);
		}
		boolean[] prefixXor = new boolean[encryption.length];
		
		boolean[] message = new boolean[N];
		message[0] = encryption[0];
		prefixXor[0] = message[0];
		
		for(int i = 1; i < N; i++){
			int end = Math.max(i - K + 1, 0);
			
			if(end == 0){
				message[i] = prefixXor[i-1] ^ encryption[i];
				prefixXor[i] = encryption[i];
			}
			else{
				message[i] = prefixXor[i-1] ^ prefixXor[end-1] ^ encryption[i];
				prefixXor[i] = prefixXor[i-1] ^ message[i];
			}
		}
		
		char[] messageString = new char[N];
		for(int i = 0; i < N; i++){
			messageString[i] = (message[i])? ONE: ZERO;
		}
		
		System.out.println(messageString);
	}
}
