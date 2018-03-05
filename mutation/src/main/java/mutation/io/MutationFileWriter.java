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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mutation.mutator.MutationVisitor.MutationNode;
import mutation.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.utils.StringUtils;

/**
 * @author LLT
 *
 */
public class MutationFileWriter extends AbstractMutationFileWriter {
	private static Logger log = LoggerFactory.getLogger(MutationFileWriter.class);
	
	public MutationFileWriter(String srcFolder, String tmpFolder) {
		super(srcFolder, tmpFolder);
	}
	
	public MutationFileWriter(String srcFolder) {
		super(srcFolder);
	}
	
	public Map<File, String> write(List<MutationNode> data, String className, int lineNo) {
		Map<File, String> files = new HashMap<>();
		File javaFile = getJavaSrcFile(className);
		
		int count = 1;
		for (MutationNode muNode : data) {
			for (int i = 0; i < muNode.getMutatedNodes().size(); i++) {
				Node node = muNode.getMutatedNodes().get(i);
				File folder = FileUtils.createFolder(muSrcFolder, 
						String.format("%s_%d_%d_%d", className,
							lineNo, muNode.getOrgNode().getBeginColumn(), count++));
				File file = new File(folder, javaFile.getName());
				List<?> lines;
				try {
					lines = org.apache.commons.io.FileUtils.readLines(javaFile);
					List<String> newContent = createNewContent(lines, muNode.getOrgNode(), node);
					org.apache.commons.io.FileUtils.writeLines(file, newContent);
					files.put(file, muNode.getMutationType(i));
				} catch (IOException e) {
					log.error("Cannot write mutation file");
					log.error(e.getMessage());
				}
			}
		}
		return files;
	}

	private List<String> createNewContent(List<?> lines, Node orgNode, Node node) {
		List<String> newContent = new ArrayList<String>(lines.size());
		int startLine = toFileLineIdx(orgNode.getBeginLine());
		int endLine = toFileLineIdx(orgNode.getEndLine());
		copy(lines, newContent, 0, startLine);
		/* replace */
		String beforeNode = extractStrBeforeNode(lines, orgNode);
		String afterNode = extractStrAfterNode(lines, orgNode);
		String[] nLines = toString(node);
		if (nLines.length == 1) {
			newContent.add(StringUtils.join("", beforeNode, nLines[0], afterNode));
		} else {
			newContent.add(StringUtils.spaceJoin(beforeNode, nLines[0]));
			for (int i = 1; i < nLines.length; i++) {
				newContent.add(nLines[i]);
			}
			newContent.add(StringUtils.spaceJoin(nLines[nLines.length - 1], afterNode));
		}
		/**/
		copy(lines, newContent, endLine + 1, lines.size());
		return newContent;
	}
}
