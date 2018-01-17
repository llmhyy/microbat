/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.dto;

import java.util.HashMap;

import sav.common.core.SystemVariables;

/**
 * @author LLT
 *
 */
public class SystemPreferences {
	private HashMap<String, String> variables = new HashMap<String, String>();
	
	public SystemPreferences() {
		variables = new HashMap<String, String>();
	}
	
	public String get(String key, String def) {
		return getValue(variables.get(key), def); 
	}
	
	public String get(String key) {
		return variables.get(key); 
	}
	
	public void put(String key, String value) {
		variables.put(key, value);
	}
	
	public String set(String key, String value) {
		return variables.put(key, value); 
	}
	
	private String getValue(String value, String defIfNull) {
		if (value == null) {
			return defIfNull;
		}
		return value;
	}

	public Boolean getBoolean(SystemVariables var) {
		String value = get(var);
		return value == null ? null : Boolean.valueOf(value);
	}

	public String get(SystemVariables var) {
		return get(var.getName(), var.getDefValue());
	}
	
	public void put(SystemVariables var, String value) {
		put(var.getName(), value);
	}

	public void putBoolean(SystemVariables var, boolean value) {
		put(var, String.valueOf(value));
	}
	
	public void putEnum(SystemVariables var, Enum<?> value) {
		put(var.getName(), value.name());
	}
}
