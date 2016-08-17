package sav.common.core.formula;

import java.util.ArrayList;
import java.util.List;

import sav.common.core.formula.utils.DisplayVisitor;

/**
 * The abstract atomic expression used in this boolean library. The subclasses
 * of this class can represents different atomic expressions in its related
 * theories(e.g., the Linear Integer Arithmetic LIA for short) and must override
 * the unimplemented abstract methods.
 * 
 * @author Spencer Xiao
 * 
 */
public abstract class Atom implements Formula {

	public Formula restrict(List<Atom> vars, List<Integer> vals) {
		for (int index = 0; index < vars.size(); index++) {
			Atom atom = vars.get(index);
			if (this.equals(atom)) {
				if (vals.get(index) == 0) {
					return Formula.FALSE;
				} else {
					return Formula.TRUE;
				}
			}
		}

		return this;
	}

	public List<Atom> getAtomics() {
		List<Atom> atoms = new ArrayList<Atom>();
		atoms.add(this);
		return atoms;
	}

	public Formula simplify() {
		return this;
	}
	
	@Override
	public String toString() {
		DisplayVisitor visitor = new DisplayVisitor();
		accept(visitor);
		return visitor.getResult();
	}
}
