package microbat.debugpilot.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import debuginfo.NodeFeedbacksPair;
import microbat.debugpilot.propagation.PropagatorType;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;

public class PropagatorSettings {
	
	public static final PropagatorType DEFAULT_PROPAGATOR_TYPE = PropagatorType.SPP_CF;
	public static final boolean DEFAULT_USE_LOCATL_SERVER = false;
	public static final String DEFAULT_SERVER_HOST = "172.26.191.20";
	public static final int DEFAULT_SERVER_PORT = 8084;
	
	protected PropagatorType propagatorType = PropagatorSettings.DEFAULT_PROPAGATOR_TYPE;
	protected boolean useLocalServer = PropagatorSettings.DEFAULT_USE_LOCATL_SERVER;
	protected String serverHost = PropagatorSettings.DEFAULT_SERVER_HOST;
	protected int serverPort = PropagatorSettings.DEFAULT_SERVER_PORT;
	
	protected Trace trace = null;
	protected List<TraceNode> slicedTrace = null;
	protected Set<VarValue> correctVars = null;
	protected Set<VarValue> wrongVars = null;
	protected Collection<NodeFeedbacksPair> feedbacks = new ArrayList<>();
	
	public PropagatorSettings() {
		
	}
	
	public PropagatorType getPropagatorType() {
		return propagatorType;
	}

	public void setPropagatorType(PropagatorType propagatorType) {
		this.propagatorType = propagatorType;
	}

	public boolean isUseLocalServer() {
		return useLocalServer;
	}

	public void setUseLocalServer(boolean useLocalServer) {
		this.useLocalServer = useLocalServer;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public List<TraceNode> getSlicedTrace() {
		return slicedTrace;
	}

	public void setSlicedTrace(List<TraceNode> slicedTrace) {
		this.slicedTrace = slicedTrace;
	}

	public Set<VarValue> getCorrectVars() {
		return correctVars;
	}

	public void setCorrectVars(Set<VarValue> correctVars) {
		this.correctVars = correctVars;
	}

	public Set<VarValue> getWrongVars() {
		return wrongVars;
	}

	public void setWrongVars(Set<VarValue> wrongVars) {
		this.wrongVars = wrongVars;
	}

	public Collection<NodeFeedbacksPair> getFeedbacks() {
		return feedbacks;
	}

	public void setFeedbacks(Collection<NodeFeedbacksPair> feedbacks) {
		this.feedbacks.clear();
		this.feedbacks.addAll(feedbacks);
	}

	
}
