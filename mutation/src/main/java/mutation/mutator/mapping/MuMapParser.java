/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.mutator.mapping;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import sav.common.core.SavRtException;

/**
 * @author LLT
 * 
 */
public class MuMapParser {
	private MuMapParser() {}

	public static Map<String, List<String>> parse(String filePath) {
		Map<String, List<String>> opMapConfig = new HashMap<String, List<String>>();
		try {
			List<?> lines = FileUtils.readLines(new File(filePath));
			for (Object lineObj : lines) {
				String line = (String) lineObj;
				String[] parts = line.split(":");
				if (parts.length != 2) {
					continue;
				}

				String op = parts[0].trim();
				parts[1] = parts[1].trim();
				String[] muOps = parts[1].split(" ");
				opMapConfig.put(op, Arrays.asList(muOps));
			}
		} catch (IOException e) {
			throw new SavRtException(e);
		}
		return opMapConfig;
	}
}
