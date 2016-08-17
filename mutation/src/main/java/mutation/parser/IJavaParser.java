/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.parser;

import java.io.File;

import japa.parser.ast.CompilationUnit;

/**
 * @author LLT
 *
 */
public interface IJavaParser {

	public CompilationUnit parse(String className);
	
	public CompilationUnit parse(File javaFile);
}
