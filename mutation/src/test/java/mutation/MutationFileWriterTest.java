/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package mutation;

import java.io.File;
import java.util.List;

import mutation.io.DebugLineFileWriter;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author LLT
 *
 */
public class MutationFileWriterTest {
	private WriterMock writer;

	@Before
	public void setup() {
		writer = new WriterMock(null);
	}

	@Test
	public void subString_tab() throws Exception {
		List<?> lines = FileUtils.readLines(new File(
				"./src/test/java/testdata/filewriter/FileWriterTestData.java"));
		String line = (String) lines.get(18);
		String subString = writer.subString(line, 1, 32);
		Assert.assertEquals(line.substring(0, 17), subString);
	}
	
	@Test
	public void subString_empty() throws Exception {
		String subString = writer.subString("subString_empty", 1, 1);
		Assert.assertTrue(subString.isEmpty());
	}
	
	private static class WriterMock extends DebugLineFileWriter {
		public WriterMock(String srcFolder) {
			super(srcFolder);
		}

		@Override
		public String subString(String line, int javaParserStartCol,
				int javaParserEndCol) {
			return super.subString(line, javaParserStartCol, javaParserEndCol);
		}
	}
}

