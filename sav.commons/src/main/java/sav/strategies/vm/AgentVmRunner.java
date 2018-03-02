/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sav.common.core.utils.CollectionBuilder;
import sav.common.core.utils.StringUtils;

/**
 * @author LLT
 *
 */
public class AgentVmRunner extends VMRunner {
	private String agentOptionSeparator = "=";
	private String agentParamsSeparator = ",";
	protected String agentJarPath;
	private Map<String, String> agentParams;
	private List<String> programArgs;
	
	public AgentVmRunner(String agentJarPath, String agentOptionSeparator, String agentParamsSeparator) {
		this(agentJarPath);
		this.agentOptionSeparator = agentOptionSeparator;
		this.agentParamsSeparator = agentParamsSeparator;
	}

	public AgentVmRunner(String agentJarPath) {
		this.agentJarPath = agentJarPath;
		agentParams = new HashMap<String, String>();
		programArgs = new ArrayList<String>();
	}
	
	@Override
	protected void buildVmOption(CollectionBuilder<String, ?> builder,
			VMConfiguration config) {
		super.buildVmOption(builder, config);
		StringBuilder sb = new StringBuilder("-javaagent:").append(agentJarPath);
		List<String> agentParams = getAgentParams();
		if (agentParams != null) {
			sb.append("=")
				.append(StringUtils.join(agentParams, getAgentParamsSeparator()));
		}
		builder.append(sb.toString());
	}

	protected String getAgentParamsSeparator() {
		return agentParamsSeparator;
	}
	
	@Override
	protected void buildProgramArgs(VMConfiguration config,
			CollectionBuilder<String, Collection<String>> builder) {
		super.buildProgramArgs(config, builder);
		for (String arg : programArgs) {
			builder.append(arg);
		}
	}
	
	public void addAgentParam(String opt, String value) {
		if (value == null || value.isEmpty()) {
			return;
		}
		agentParams.put(opt, value);
	}
	
	public void removeAgentParam(String opt) {
		agentParams.remove(opt);
	}
	
	public void addAgentParam(String opt, boolean value) {
		agentParams.put(opt, String.valueOf(value));
	}
	
	public void addAgentParam(String opt, int value) {
		agentParams.put(opt, String.valueOf(value));
	}
	
	public List<String> getProgramArgs() {
		return programArgs;
	}
	
	public void setProgramArgs(List<String> arguments) {
		this.programArgs = arguments;
	}

	public void addProgramArg(String opt, String... values) {
		addProgramArg(opt, Arrays.asList(values));
	}
	
	public void addProgramArg(String opt, List<String> values) {
		programArgs.add("-" + opt);
		for (String value : values) {
			programArgs.add(value);
		}
	}

	private List<String> getAgentParams() {
		ArrayList<String> params = new ArrayList<String>();
		if (!agentParams.isEmpty()) {
			for (Entry<String, String> entry : agentParams.entrySet()) {
				params.add(newAgentOption(entry.getKey(), entry.getValue()));
			}
		}
		appendAgentParams(params);
		return params;
	}

	protected void appendAgentParams(ArrayList<String> params) {
		// override if needed.
	}

	protected String newAgentOption(String opt, String value) {
		return StringUtils.join(getAgentOptionSeparator(), opt, value);
	}

	protected String getAgentOptionSeparator() {
		return agentOptionSeparator;
	}

	public void setAgentOptionSeparator(String agentOptionSeparator) {
		this.agentOptionSeparator = agentOptionSeparator;
	}

	public void setAgentParamsSeparator(String agentParamsSeparator) {
		this.agentParamsSeparator = agentParamsSeparator;
	}
}
