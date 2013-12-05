package lrs;

import java.util.*;

public class LinearConstraintExpression {
	
	public LinearConstraintExpression(RationalNumber constant, ArrayList<RationalNumber> variableWeights) {
		this.constant = constant;
		this.variableWeights = variableWeights;
	}

	private RationalNumber constant = null;
	
	private ArrayList<RationalNumber> variableWeights = null;

	public RationalNumber getConstant() {
		return constant;
	}

	public ArrayList<RationalNumber> getVariableWeights() {
		return variableWeights;
	}
	
	@Override
	public String toString() {
		String result = this.constant.toString();
		
		for (RationalNumber number : this.variableWeights)
			result += (" " + number.toString());
		
		return result;
	}
}
