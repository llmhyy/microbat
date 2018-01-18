/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.common.core.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author LLT
 * 
 */
public class TextFormatUtils {
	private TextFormatUtils() {
	}
	
	public static <T>String printListSeparateWithNewLine(Collection<T> values) {
		return StringUtils.join(values, "\n");
	}
	
	public static <K, V> String printMap(Map<K, V> values) {
		return printMap(values, "\n");
	}
	
	public static <K, V> String printMap(Map<K, V> values, String separator) {
		if (CollectionUtils.isEmpty(values)) {
			return StringUtils.EMPTY;
		}
		StringBuilder sb = new StringBuilder();
		for (Entry<K, V> entry : values.entrySet()) {
			sb.append(printObj(entry.getKey())).append(" : ").append(printObj(entry.getValue())).append(separator);
		}
		return sb.toString();
	}
	
	public static String printObj(Object obj) {
		if (obj == null) {
			return StringUtils.EMPTY;
		}
		if (obj.getClass().isArray()) {
			return printArray(obj);
		}
		if (obj instanceof Collection<?>) {
			return printCol((Collection<?>)obj);
		}
		if (obj instanceof Map) {
			return printMap((Map<?, ?>) obj);
		}
		return obj.toString();
	}
	
	private static <T> String printArray(Object obj) {
		return printArray(obj, ", ");
	}
	
	private static <T> String printArray(Object obj, String separator) {
		if (obj == null) {
			return "null";
		}
		int maxIdx = Array.getLength(obj) - 1;
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i <= maxIdx; i++) {
			sb.append(printObj(Array.get(obj, i)));
			if (i != maxIdx) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static String printCol(Collection<?> obj) {
		return printCol(obj, ", ");
	}
	
	public static String printCol(Collection<?> obj, String separator) {
		if (CollectionUtils.isEmpty(obj)) {
			return StringUtils.EMPTY;
		}
		StringBuilder sb = new StringBuilder();
		int maxIdx = obj.size() - 1;
		sb.append("[");
		int i = 0;
		for (Object ele : obj) {
			sb.append(printObj(ele));
			if (i != maxIdx) {
				sb.append(separator);
			}
			i++;
		}
		return sb.toString();
	}
	
	public static String printTimeString(long time) {
		TimeUnit timeUnit = TimeUnit.MILLISECONDS;
		long diffSec = timeUnit.toSeconds(time);
		long diffMin = timeUnit.toMinutes(time);
		StringBuilder sb = new StringBuilder();
		sb.append(time).append(" ms");
		if (diffMin >= 1) {
			sb.append("(").append(diffMin).append("m")
				.append(" ").append(diffSec - TimeUnit.MINUTES.toSeconds(diffMin)).append("s")
				.append(")");
		} else if (diffSec >= 1) {
			sb.append("(").append(diffSec).append("s").append(")");
		}
		return sb.toString();
	}

	public static String printFileList(List<File> files) {
		StringBuilder sb = new StringBuilder("[");
		for (File file : files) {
			sb.append(file.getName()).append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
}
