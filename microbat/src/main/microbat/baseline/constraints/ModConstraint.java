package microbat.baseline.constraints;

import java.util.BitSet;

import microbat.baseline.Configs;

public class ModConstraint extends Constraint {

	public ModConstraint(BitSet variablesIncluded, int conclusionIndex) {
		super(variablesIncluded, conclusionIndex, Configs.UNCERTAIN);
	}

}
