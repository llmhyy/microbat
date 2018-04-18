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
import java.util.ArrayList;
import java.util.List;

import mutation.mutator.insertdebugline.DebugLineData;
import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 * 
 */
public class DebugLineFileWriter extends AbstractMutationFileWriter implements IMutationWriter {

	public DebugLineFileWriter(String srcFolder) {
		super(srcFolder);
	}

	public File write(List<DebugLineData> data, String className) {
		File javaFile = getJavaSrcFile(className);
		File muFile = new File(muSrcFolder, javaFile.getName());

		try {
			List<?> lines = org.apache.commons.io.FileUtils.readLines(javaFile);
			List<String> newContent = new ArrayList<String>();
			int preIdx = 0;
			for (DebugLineData debugLine : data) {
				switch (debugLine.getInsertType()) {
				case ADD:
					Node insertNode = debugLine.getInsertNode();
					copy(lines, newContent, preIdx,
							toFileLineIdx(insertNode.getBeginLine()));
					newContent.add(insertNode.toString());
					preIdx = toFileLineIdx(insertNode.getBeginLine());
					break;
				case REPLACE:
					/* we might have some text before and after the node, just keep them all
					 * in new separate line
					 * */
					Node orgNode = debugLine.getOrgNode();
					copy(lines, newContent, preIdx, toFileLineIdx(orgNode.getBeginLine()));
					String beforeNode = extractStrBeforeNode(lines, orgNode);
					addIfNotEmpty(newContent, beforeNode);
					/* add new node */
					for (Node newNode : debugLine.getReplaceNodes()) {
						String[] stmt = toString(newNode, orgNode);
						CollectionUtils.addAll(newContent, stmt);
					}
					/* keep content at the same line but right after the org node */
					String afterNode = extractStrAfterNode(lines, orgNode);
					addIfNotEmpty(newContent, afterNode);
					preIdx = toFileLineIdx(orgNode.getEndLine()) + 1;
					break;
				}
				debugLine.setDebugLine(newContent.size());
			}
			copy(lines, newContent, preIdx, lines.size());
			org.apache.commons.io.FileUtils.writeLines(muFile, newContent);
		} catch (IOException e) {
			throw new SavRtException(e);
		}
		return muFile;
	}

	private int addIfNotEmpty(List<String> lines, String newLine) {
		if (!newLine.isEmpty()) {
			lines.add(newLine);
			return 1;
		}
		return 0;
	}

}
