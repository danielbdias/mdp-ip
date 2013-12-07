package unittests.lrs;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.junit.Test;

import lrs.ConstraintsConverter;
import lrs.LinearConstraintExpression;
import lrs.RationalNumber;

public class ConstraintsConverterTests {

	@Test
	public void test_convertConstraintsToLrsFormat_case01() {
		ArrayList<Object> constraints = new ArrayList<Object>();
		
		ArrayList<Object> constraint = new ArrayList<Object>();
		
		//5.5 + p1 <= 10.0
		constraint.add(new BigDecimal(5.5));
		constraint.add("+");
		constraint.add("p1");
		constraint.add("<");
		constraint.add("=");
		constraint.add(new BigDecimal(10.0));
		
		constraints.add(constraint);
		
		LinearConstraintExpression[] expressions = ConstraintsConverter.convertConstraintsToLrsFormat(constraints);
		
		LinearConstraintExpression expression = expressions[0];
		
		validateRationalNumber(expression.getConstant(), 45, 10);
		validateRationalNumber(expression.getVariableWeights().get(0), -1, 1);
	}
	
	@Test
	public void test_convertConstraintsToLrsFormat_case02() {
		ArrayList<Object> constraints = new ArrayList<Object>();
		
		ArrayList<Object> constraint = new ArrayList<Object>();
		
		//5 + 10.1 * p2 <= 50 - p3
		constraint.add(new BigInteger("5"));
		constraint.add("+");
		constraint.add(new BigDecimal(10.1));
		constraint.add("*");
		constraint.add("p2");
		constraint.add("<");
		constraint.add("=");
		constraint.add(new BigInteger("50"));
		constraint.add("-");
		constraint.add("p3");
		
		constraints.add(constraint);
		
		LinearConstraintExpression[] expressions = ConstraintsConverter.convertConstraintsToLrsFormat(constraints);
		
		LinearConstraintExpression expression = expressions[0];
		
		validateRationalNumber(expression.getConstant(), 45, 1);
		validateRationalNumber(expression.getVariableWeights().get(0), -101, 10);
		validateRationalNumber(expression.getVariableWeights().get(1), -1, 1);
	}
	
	@Test
	public void test_convertConstraintsToLrsFormat_case03() {
		ArrayList<Object> constraints = new ArrayList<Object>();
		
		ArrayList<Object> constraint = new ArrayList<Object>();
		
		//p4 >= 0.5
		constraint.add("p4");
		constraint.add(">");
		constraint.add("=");
		constraint.add(new BigDecimal(0.5));
		
		constraints.add(constraint);
		
		LinearConstraintExpression[] expressions = ConstraintsConverter.convertConstraintsToLrsFormat(constraints);
		
		LinearConstraintExpression expression = expressions[0];
		
		validateRationalNumber(expression.getConstant(), -5, 10);
		validateRationalNumber(expression.getVariableWeights().get(0), 1, 1);
	}
	
	private void validateRationalNumber(RationalNumber number, int numerator, int denominator) {
		assertEquals(numerator, number.getNumerator());
		assertEquals(denominator, number.getDenominator());
	}
}
