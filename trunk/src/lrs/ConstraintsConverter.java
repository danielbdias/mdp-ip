package lrs;

import java.util.*;

public class ConstraintsConverter {

	public static LinearConstraintExpression[] convertConstraintsToLrsFormat(ArrayList constraints){
		LinearConstraintExpression[] parsedConstraints = new LinearConstraintExpression[constraints.size()];
		
		for (int i = 0; i < constraints.size(); i++)
			parsedConstraints[i] = parseConstraint(constraints.get(i));
		
		return parsedConstraints;
	}

	private static LinearConstraintExpression parseConstraint(Object constraint) {
		return null;
	}
}
