/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
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
		String filePath = ClassUtils.getJFilePath(sourceFolder, ClassUtils.getCompilationUnitForSimpleCase(className));
		File file = new File(filePath);
		if (!file.exists()) {
			return getJavaFileForAnotherSeparateClass(sourceFolder, className);
		}
		return file;
	}

	/**
	 * handle for the case that this class is not an inner class but defined in a same java file of
	 * another public class 
	 */
	private File getJavaFileForAnotherSeparateClass(String sourceFolder, String className) {
		String packageFolder = ClassUtils.getPackageFolderPath(sourceFolder, className);
		File pkgFolder = new File(packageFolder);
		if (pkgFolder == null || !pkgFolder.isDirectory()) {
			// something wrong
			return null;
		}
		File[] allFiles = pkgFolder.listFiles();
		String classDefinitionStr = "class " + ClassUtils.getSimpleName(className);
		for (File file : allFiles) {
			if (!file.getName().endsWith(".java")) {
				continue;     
			}
			try {
				if (FileUtils.readFileToString(file).contains(classDefinitionStr)) {
					return file;
				}
			} catch (IOException e) {
				// ignore
			}
		}
		return null;
	}
}
