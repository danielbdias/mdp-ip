package lrs.expressiontokens;

import java.math.*;

public class NumberToken extends Token {

	public NumberToken(Object originalValue) {
		super(originalValue);

		this.value = this.convertToDouble(originalValue);
	}

	private double value = Double.NaN;

	public double getValue() {
		return value;
	}
	
	private double convertToDouble(Object originalValue) {
		if (originalValue instanceof Double)
			return (Double) originalValue;
		else if (originalValue instanceof Integer)
			return (Double) originalValue;
		else if (originalValue instanceof BigDecimal)
			return ((BigDecimal) originalValue).doubleValue();
		else if (originalValue instanceof BigInteger)
			return ((BigInteger) originalValue).doubleValue();
		
		return Double.NaN;
	}
	
	public static boolean validToken(Object token) {
		if (token instanceof Double ||
			token instanceof Integer ||
			token instanceof BigDecimal ||
			token instanceof BigInteger)
			return true;
		
		return false;
	}
}
