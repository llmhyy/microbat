package microbat.instrumentation.trace.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import microbat.model.variable.Variable.VariableType;
import sav.common.core.utils.ClassUtils;
import sav.common.core.utils.CollectionUtils;
import sav.common.core.utils.Iterators;
import sav.common.core.utils.StringUtils;

public class AccessVariableInfo {
	private int lineNo;
	private List<Integer> pcs;
	private List<Variable> readVars;
	private List<Variable> writtenVars;
	private Variable returnVar;
	
	public AccessVariableInfo(int lineNo) {
		this.lineNo = lineNo;
		pcs = new ArrayList<>(5);
	}
	
	public void addPc(int pc) {
		pcs.add(pc);
	}

	public void addReadVar(Variable var) {
		if (readVars == null) {
			readVars = new ArrayList<>(5);
		}
		readVars.add(var);
	}
	
	public void addWrittenVar(Variable var) {
		if (writtenVars == null) {
			writtenVars = new ArrayList<>();
		}
		writtenVars.add(var);
	}
	
	public List<Variable> getReadVars() {
		return readVars;
	}

	public void setReadVars(List<Variable> readVars) {
		this.readVars = readVars;
	}

	public List<Variable> getWrittenVars() {
		return writtenVars;
	}

	public void setWrittenVars(List<Variable> writtenVars) {
		this.writtenVars = writtenVars;
	}
	
	public Variable getReturnVar() {
		return returnVar;
	}

	public void setReturnVar(Variable returnVar) {
		this.returnVar = returnVar;
	}

	public int getLineNo() {
		return lineNo;
	}

	public int getInsertPc() {
		// TODO LLT: check multi stmt in line which has break/return/continue.
		return pcs.get(pcs.size() - 1) + 1;
	}
	
	public String getReadString() {
		return buildVarsStr(readVars);
	}
	
	public String getWrittenString() {
		return buildVarsStr(writtenVars);
	}
	
	private Map<String, Variable> allVars;
	@SuppressWarnings("unchecked")
	public Map<String, Variable> getAllVarNames() {
		if (allVars != null) {
			return allVars;
		}
		allVars = new HashMap<>();
		Iterators<Variable> it = new Iterators<>(readVars.iterator(), writtenVars.iterator());
		while (it.hasNext()) {
			Variable var = it.next();
			String varName = var.getName();
			if (var.getVarType() == VariableType.FIELD) {
				FieldVar fieldVar = (FieldVar) var;
				if (fieldVar.isStatic()) {
					String declaringType = fieldVar.getDeclaringType();
					varName = declaringType == null ? var.getName() : ClassUtils.toClassMethodStr(declaringType, var.getName());
				} else {
					varName = FIELD_VAR_PREFIX + var.getName();
				}
			} 
			allVars.put(varName, var);
		}
		return allVars;
	}
	
	private static final String VAR_SEPARATOR = ";";
	private static final String VAR_PROP_SEPARATOR = ":";
	private static final String FIELD_VAR_PREFIX = "this.";
	private static String buildVarsStr(List<Variable> vars) {
		if (CollectionUtils.isEmpty(vars)) {
			return StringUtils.EMPTY;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vars.size(); i++) {
			Variable var = vars.get(i);
			sb.append(var.getVarType()).append(VAR_PROP_SEPARATOR)
				.append(var.getType()).append(VAR_PROP_SEPARATOR)
				.append(var.getName());
			switch (var.getVarType()) {
			case ARRAY_ELEMENT:
			case CONSTANTS:
				break;
			case FIELD:
				FieldVar fieldVar = (FieldVar) var;
				sb.append(VAR_PROP_SEPARATOR).append(fieldVar.isStatic());
				break;
			case LOCAL:
				LocalVar localVar = (LocalVar) var;
				sb.append(VAR_PROP_SEPARATOR).append(localVar.getLocationClass())
					.append(VAR_PROP_SEPARATOR).append(localVar.getLineNumber())
					.append(VAR_PROP_SEPARATOR).append(localVar.isParameter());
				break;
			case VIRTUAL:
				break;
			default:
				throw new IllegalArgumentException("Unhandled type: " + var.getVarType());
			}
			if (i != (vars.size() - 1)) {
				sb.append(VAR_SEPARATOR);
			}
		}
		
		return sb.toString();
	}
}
