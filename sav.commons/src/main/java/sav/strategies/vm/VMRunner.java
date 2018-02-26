/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package sav.strategies.vm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sav.common.core.ModuleEnum;
import sav.common.core.SavException;
import sav.common.core.SavRtException;
import sav.common.core.utils.CollectionBuilder;
import sav.common.core.utils.StringUtils;

/**
 * @author LLT
 * 
 */
public class VMRunner {
	private static final int NO_TIME_OUT = -1;
	private static Logger log = LoggerFactory.getLogger(VMRunner.class);
	protected static final String cpToken = "-cp";
	/*
	 * from jdk 1.5, we can use new JVM option: -agentlib 
	 * Benefits of using the new -agentlib args is, it doesn't contain any whitespace, so
	 * you don't need to worry if you need to quote it or not. But if you do
	 * want to use the old flags, be careful about when to quote the value and
	 * when to not quote.
	 */
	protected static final String debugToken = "-agentlib:jdwp=transport=dt_socket,suspend=y,address=%s";
	protected static final String enableAssertionToken = "-ea";
	protected static final String noVerifyToken = "-noverify";
	/* timeout in millisecond */
	private long timeout = NO_TIME_OUT;
	private boolean isLog = true;
	protected Timer timer = null;
	
	protected Process process;
	private String processError;
	private boolean printOutExecutionTrace = false;
	
	public boolean startVm(VMConfiguration config) throws SavException {
		this.isLog = config.isVmLogEnable();
		List<String> commands = buildCommandsFromConfiguration(config);
		return startVm(commands, false);
	}
	
	public String getCommandLinesString(VMConfiguration config) throws SavException {
		List<String> commands = buildCommandsFromConfiguration(config);
		return StringUtils.join(commands, " ");
	}

	private List<String> buildCommandsFromConfiguration(VMConfiguration config)
			throws SavException {
		CollectionBuilder<String, Collection<String>> builder = CollectionBuilder
						.init(new ArrayList<String>())
						.append(VmRunnerUtils.buildJavaExecArg(config));
		buildVmOption(builder, config);
		buildProgramArgs(config, builder);
		List<String> commands = builder.getResult();
		return commands;
	}
	
	protected void buildProgramArgs(VMConfiguration config,
			CollectionBuilder<String, Collection<String>> builder) {
		builder.append(cpToken)
				.append(config.getClasspathStr())
				.append(config.getLaunchClass());
		for (String arg : config.getProgramArgs()) {
			builder.append(arg);
		}
	}

	public boolean startAndWaitUntilStop(VMConfiguration config)
			throws SavException {
		List<String> commands = buildCommandsFromConfiguration(config);
		return startAndWaitUntilStop(commands);
	}
	
	public String getProccessError() throws SavRtException {
		return processError;
	}

	public void setupInputStream(final InputStream is, final StringBuffer sb, final boolean error) {
		final InputStreamReader streamReader = new InputStreamReader(is);
		new Thread(new Runnable() {
			public void run() {
				BufferedReader br = new BufferedReader(streamReader);
				String line = null;
				try {
					while ((line = br.readLine()) != null) {
//						if (error) {
//							log.warn(line);
//						}
						if (printOutExecutionTrace) {
							System.out.println(line);
						}
						if (!line.contains("Class JavaLaunchHelper is implemented in both")) {
							sb.append(line).append("\n");
						}
					}
				} catch (IOException e) {
					// do nothing
				} finally {
					IOUtils.closeQuietly(streamReader);
					IOUtils.closeQuietly(br);
					IOUtils.closeQuietly(is);
				}
			}
		}).start();
	}

	protected void buildVmOption(CollectionBuilder<String, ?> builder, VMConfiguration config) {
		builder
		.appendIf(enableAssertionToken, config.isEnableAssertion())
		.appendIf(noVerifyToken, config.isNoVerify())
		.appendIf(String.format(debugToken, config.getPort()), config.isDebug());
	}

	public boolean startVm(List<String> commands, boolean waitUntilStop)
			throws SavException {
		StringBuffer sb = new StringBuffer();
		logCommands(commands);
		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		try {
			process = processBuilder.start();
			setupErrorStream(process.getErrorStream(), sb);
			/* Note: The input stream must be read if available to be closed, 
			 * otherwise, the process will never end. So, keep doing this even if 
			 * the printStream is not set */
			setupInputStream(process.getInputStream(), new StringBuffer(), false);
			setupOutputStream(process.getOutputStream());
			timer = null;
			if (timeout != NO_TIME_OUT) {
				timer = new Timer();
			    timer.schedule(new TimerTask() {

			        @Override
			        public void run() {
			            stop();
			            log.info("destroy thread due to timeout!");
			        }

			    }, timeout); 
			}
			if (waitUntilStop) {
				waitUntilStop(process);
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
				processError = sb.toString();
				return isExecutionSuccessful();
			}
			return true;
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new SavException(ModuleEnum.JVM, e, "cannot start jvm process");
		}
	}

	public boolean isExecutionSuccessful() {
		try {
			return process.exitValue() == 0;
		} catch (Exception e) {
			return false;
		}
	}
	
	protected void stop() {
		process.destroy();
	}
	
	protected void setupErrorStream(InputStream errorStream, StringBuffer sb) {
		setupInputStream(errorStream, sb, true);
	}

	protected void setupOutputStream(OutputStream outputStream) {
		// override if needed.
	}

	private void logCommands(List<String> commands) {
		System.out.println(StringUtils.join(commands, " "));
		if (isLog && log.isDebugEnabled()) {
			log.debug("start cmd..");
			log.debug(StringUtils.join(commands, " "));
		}
	}
	
	public void waitUntilStop(Process process) throws SavException {
		waitUntilStopByWaitfor(process);
	}
	
	public void waitUntilStopByWaitfor(Process process) throws SavException {
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			throw new SavException(ModuleEnum.JVM, e);
		}
	}

	public void waitUntilStopByALoop(Process process)
			throws SavException {
		while (true) {
			try {
				process.exitValue();
				return;
			} catch (IllegalThreadStateException ex) {
				// means: not yet terminated
			} 
		}
	}
	
	public void setTimeout(int timeout, TimeUnit unit) {
		setTimeout(unit.toMillis(timeout));
	}
	
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	public static boolean start(VMConfiguration config) throws SavException {
		VMRunner vmRunner = new VMRunner();
		return vmRunner.startVm(config);
	}
	
	public boolean startAndWaitUntilStop(List<String> commands)
			throws SavException {
		return startVm(commands, true);
	}
	
	protected boolean isProcessRunning() {
	    try {
	        process.exitValue();
	        return false;
	    } catch (Exception e) {
	        return true;
	    }
	}
	
	public void cancelTimer() {
		if (timer != null) {
			timer.cancel();
		}
	}
	
	public static VMRunner getDefault() {
		return new VMRunner();
	}
	
	public void setLog(boolean isLog) {
		this.isLog = isLog;
	}
	
	public Process getProcess() {
		return process;
	}
	
	public void setPrintOutExecutionTrace(boolean printOutExecutionTrace) {
		this.printOutExecutionTrace = printOutExecutionTrace;
	}
}
