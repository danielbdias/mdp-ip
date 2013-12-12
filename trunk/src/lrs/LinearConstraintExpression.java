package lrs;

import java.util.*;

public class LinearConstraintExpression {
	
	public LinearConstraintExpression(RationalNumber constant, ArrayList<RationalNumber> variableWeights, String[] variables) {
		this.constant = constant;
		this.variableWeights = variableWeights;
		this.variables = variables;
	}

	private RationalNumber constant = null;
	
	private ArrayList<RationalNumber> variableWeights = null;
	
	private String[] variables = null;

	public RationalNumber getConstant() {
		return constant;
	}

	public ArrayList<RationalNumber> getVariableWeights() {
		return variableWeights;
	}
	
	public String[] getVariables() {
		return variables;
	}
	
	@Override
	public String toString() {
		String result = this.constant.toString();
		
		for (RationalNumber number : this.variableWeights)
			result += (" " + number.toString());
		
		return result;
	}
}
