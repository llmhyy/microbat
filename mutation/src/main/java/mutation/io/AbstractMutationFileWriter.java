/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation.io;

import japa.parser.ast.Node;

import java.io.File;
import java.io.IOException;
import java.util.List;

import sav.common.core.SavRtException;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.StringUtils;
import mutation.utils.FileUtils;

/**
 * @author LLT
 *
 */
public class AbstractMutationFileWriter {
	protected static final int JAVA_PARSER_TAB_SIZE = 8;
	protected String muSrcFolder;
	protected String scrFolder;
	
	public AbstractMutationFileWriter(String srcFolder) {
		this.scrFolder = srcFolder;
		try {
			String projName = srcFolder.substring(0, srcFolder.indexOf("/src"));
			projName = projName.substring(projName.lastIndexOf("/")+1, projName.length());
			
			if(projName.length() < 5){
				projName = "mutation";
			}
			
			File file = File.createTempFile(projName, "");
			String path = file.toString();
			path = path.substring(0, path.indexOf(projName)+projName.length());
			file = new File(path);
			file.delete();
			file.mkdir();
			
			muSrcFolder = file.getAbsolutePath();
		} catch (IOException e) {
			throw new SavRtException("cannot create temp dir");
		}
		
		
//		muSrcFolder = FileUtils.createTempFolder("mutatedSource")
//				.getAbsolutePath();
	}
	
	protected File getJavaSrcFile(String className) {
		return new File(ClassUtils.getJFilePath(scrFolder, className));
	}

	protected String[] toString(Node node) {
		return node.toString().split("\n");
	}
	
	protected String extractStrAfterNode(List<?> lines, Node node) {
		return subString((String) lines.get(toFileLineIdx(node.getEndLine())),
				node.getEndColumn() + 1);
	}
	
	protected String extractStrBeforeNode(List<?> lines, Node node) {
		String line = (String) lines.get(toFileLineIdx(node.getBeginLine()));
		return subString(line, 1, node.getBeginColumn());
	}

	protected String subString(String line, int javaParserStartCol,
			int javaParserEndCol) {
		char[] chars = line.toCharArray();
		int start = getMappedColIdx(chars, javaParserStartCol, 0, 1);
		int end = getMappedColIdx(chars, javaParserEndCol, start, javaParserStartCol);
		return line.substring(start, end);
	}
	
	protected String subString(String line, int javaParserStartCol) {
		char[] chars = line.toCharArray();
		int start = getMappedColIdx(chars, javaParserStartCol, 0, 1);
		if (start >= line.length()) {
			return StringUtils.EMPTY;
		}
		return line.substring(start);
	}

	protected int getMappedColIdx(char[] chars, int javaParserCol, int startIdx, int startPos) {
		int pos = startPos;
		for (int i = startIdx; i < chars.length; i++) {
			char ch = chars[i];
			if (pos == javaParserCol) {
				return i;
			}
			if (ch == '\t') {
				pos += JAVA_PARSER_TAB_SIZE;
			} else {
				pos++;
			}
		}
		if (pos == javaParserCol) {
			return chars.length;
		}
		throw new SavRtException(
				StringUtils.spaceJoin("cannot map column index between inputStream and javaparser, line = ",
						String.valueOf(chars), ", column=", javaParserCol));
	}
	
	/**
	 * copy content, exclude line at endIdx
	 * */
	protected void copy(List<?> from, List<String> to, int startIdx, int endIdx) {
		for (int i = startIdx; i < endIdx; i++) {
			to.add((String) from.get(i));
		}
	}

	protected int toFileLineIdx(int javaLineIdx) {
		return javaLineIdx - 1;
	}
}
