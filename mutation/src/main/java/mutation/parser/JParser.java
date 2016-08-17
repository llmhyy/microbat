/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.parser;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import sav.common.core.SavRtException;
import sav.common.core.utils.ClassUtils;


/**
 * @author LLT
 *
 */
public class JParser implements IJavaParser {
	private String sourceFolder;
	private Map<String, CompilationUnit> cacheCuMap;
	
	/**
	 * only mutated classes will be cache 
	 */
	public JParser(String sourceFolder, Collection<String> mutatedClasses) {
		cacheCuMap = new HashMap<String, CompilationUnit>();
		this.sourceFolder = sourceFolder;
		for (String mutatedClass : mutatedClasses) {
			cacheCuMap.put(mutatedClass, parse(mutatedClass));
		}
	}

	@Override
	public CompilationUnit parse(File javaFile) {
		try {
			return JavaParser.parse(javaFile);
		} catch (Exception e) {
			throw new SavRtException(e);
		}
	}
	
	@Override
	public CompilationUnit parse(String className) {
		CompilationUnit compilationUnit = cacheCuMap.get(className);
		if (compilationUnit == null) {
			File javaFile = getJavaFile(className);
			return parse(javaFile);
		}
		return compilationUnit;
	}
	
	private File getJavaFile(String className) {
		String filePath = ClassUtils.getJFilePath(sourceFolder, className);
		return new File(filePath);
	}

}
