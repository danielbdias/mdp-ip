package unittests.lrs;

import static org.junit.Assert.*;

import java.math.*;
import java.util.*;

import org.junit.Test;

import lrs.ExpressionParser;
import lrs.expressiontokens.*;

public class ExpressionParserTests {

	@Test
	public void test_parseExpression_case01() {
		//5.5 + p1 <= 10.0
		ArrayList<Object> constraint = new ArrayList<Object>();
		constraint.add(new BigDecimal(5.5));
		constraint.add("+");
		constraint.add("p1");
		constraint.add("<");
		constraint.add("=");
		constraint.add(new BigDecimal(10.0));
		
		List<Token> expression = ExpressionParser.parseExpression(constraint);
		
		validateNumberToken(expression, 0, 5.5);
		validateOperationToken(expression, 1, OperationType.Sum);
		validateParameterToken(expression, 2, "p1", 1);
		validateComparisonToken(expression, 3, ComparisonType.LessOrEqual);
		validateNumberToken(expression, 4, 10.0);
	}

	@Test
	public void test_parseExpression_case02() {
		//5 + 10.1 * p2 <= 50 - p3
		ArrayList<Object> constraint = new ArrayList<Object>();
		constraint.add(new BigInteger("5"));
		constraint.add("+");
		constraint.add(new BigDecimal(10.1));
		constraint.add("*");
		constraint.add("p2");
		constraint.add("<");
		constraint.add(new BigInteger("50"));
		constraint.add("-");
		constraint.add("p3");
		
		List<Token> expression = ExpressionParser.parseExpression(constraint);
		
		validateNumberToken(expression, 0, 5.0);
		validateOperationToken(expression, 1, OperationType.Sum);
		validateNumberToken(expression, 2, 10.1);
		validateOperationToken(expression, 3, OperationType.Multiplication);
		validateParameterToken(expression, 4, "p2", 2);
		validateComparisonToken(expression, 5, ComparisonType.Less);
		validateNumberToken(expression, 6, 50.0);
		validateOperationToken(expression, 7, OperationType.Subtration);
		validateParameterToken(expression, 8, "p3", 3);
	}
	
	@Test
	public void test_parseExpression_case03() {
		//p4 >= 0.5
		ArrayList<Object> constraint = new ArrayList<Object>();
		constraint.add("p4");
		constraint.add(">");
		constraint.add("=");
		constraint.add(new BigDecimal(0.5));
		
		List<Token> expression = ExpressionParser.parseExpression(constraint);
		
		validateParameterToken(expression, 0, "p4", 4);
		validateComparisonToken(expression, 1, ComparisonType.GreaterOrEqual);
		validateNumberToken(expression, 2, 0.5);
	}
	
	private void validateNumberToken(List<Token> expression, int index, double number) {
		assertTrue(expression.get(index) instanceof NumberToken);
		
		NumberToken token = (NumberToken) expression.get(index);
		assertEquals(number, token.getValue(), 0.0);
	}
	
	private void validateOperationToken(List<Token> expression, int index, OperationType type) {
		assertTrue(expression.get(index) instanceof OperationToken);
		
		OperationToken token = (OperationToken) expression.get(index);
		assertEquals(type, token.getOperation());
	}
	
	private void validateParameterToken(List<Token> expression, int index, String parameterString, int parameterNumber) {
		assertTrue(expression.get(index) instanceof ParameterToken);
		
		ParameterToken token = (ParameterToken) expression.get(index);
		assertEquals(parameterString, token.getParameterString());
		assertEquals(parameterNumber, token.getParameterNumber());
	}
	
	private void validateComparisonToken(List<Token> expression, int index, ComparisonType type) {
		assertTrue(expression.get(index) instanceof ComparisonToken);
		
		ComparisonToken token = (ComparisonToken) expression.get(index);
		assertEquals(type, token.getComparison());
	}
}
