package lrs;

import java.security.InvalidParameterException;
import java.util.*;

import util.Pair;

import lrs.expressiontokens.*;

public class ConstraintsConverter {
	
	public static LinearConstraintExpression[] convertConstraintsToLrsFormat(ArrayList constraints){
		LinearConstraintExpression[] parsedConstraints = new LinearConstraintExpression[constraints.size()];
		
		List<List<Token>> expressions = new ArrayList<List<Token>>();
		
		Set<String> parametersFound = new HashSet<String>();
		
		for (Object constraint : constraints) {
			List<Token> parsedExpression = ExpressionParser.parseExpression((ArrayList) constraint);
			expressions.add(parsedExpression);
			findNewParameters(parsedExpression, parametersFound);
		}
		
		String[] parameters = parametersFound.toArray(new String[0]);
		
		for (int i = 0; i < constraints.size(); i++) {
			List<Token> expression = expressions.get(i);
			parsedConstraints[i] = convertExpressionToLinearConstraintExpression(expression, parameters);
		}
			
		return parsedConstraints;
	}

	private static void findNewParameters(List<Token> parsedExpression, Set<String> parametersFound) {
		for (Token token : parsedExpression) {
			if (token instanceof ParameterToken) {
				ParameterToken parameter = (ParameterToken) token;
				parametersFound.add(parameter.getParameterString());
			}
		}
	}
	
	private static LinearConstraintExpression convertExpressionToLinearConstraintExpression(List<Token> expression, String[] parameters) {
		int comparerIndex = getComparerIndex(expression);
		
		//divide a expressão em dois, antes do comparador e depois do comparador
		List<Token> leftExpression = expression.subList(0, comparerIndex - 1);
		List<Token> rightExpression = expression.subList(comparerIndex + 1, expression.size() - 1);
		
		//para cada parte, faz um split por + e - e valida se são expressões lineares válidas
		List<Pair> leftSplitedExpression = splitExpression(leftExpression);
		List<Pair> rightSplitedExpression = splitExpression(rightExpression);
		
		//faz as trasformações necessárias
		ComparisonToken comparison = (ComparisonToken) expression.get(comparerIndex);
		
		int signal = 1;

		//o que fazer quando a inequação for apenas menor ou maior que algo? como converter para menor que zero?
		//resp: encontra o complemento e inverte o sinal
		if (comparison.getOperation() == ComparisonType.Greater || comparison.getOperation() == ComparisonType.LessOrEqual)
			signal = -1;
		
		RationalNumber constant = computeConstant(leftSplitedExpression, rightSplitedExpression, signal);
		
		ArrayList<RationalNumber> variableWeight = computeVariableWeights(leftSplitedExpression, rightSplitedExpression, parameters, signal);
		
		return new LinearConstraintExpression(constant, variableWeight);
	}

	private static RationalNumber computeConstant(List<Pair> leftSplitedExpression, List<Pair> rightSplitedExpression, int signal) {
		RationalNumber constant = RationalNumber.ZERO;
		
		for (Pair pair : leftSplitedExpression) {
			RationalNumber weight = (RationalNumber) pair.get_o1();
			int parameterIndex = (Integer) pair.get_o2();
			
			if (parameterIndex == -1) { //é uma constante
				if (signal == -1)
					weight = weight.multiply(RationalNumber.MINUS_ONE);
					
				constant = constant.add(weight);
			}
		}
		
		for (Pair pair : rightSplitedExpression) {
			RationalNumber weight = (RationalNumber) pair.get_o1();
			int parameterIndex = (Integer) pair.get_o2();
			
			if (parameterIndex == -1) { //é uma constante
				if (signal == 1)
					weight = weight.multiply(RationalNumber.MINUS_ONE);
					
				constant = constant.add(weight);
			}
		}
		
		return constant;
	}

	private static ArrayList<RationalNumber> computeVariableWeights(List<Pair> leftSplitedExpression, List<Pair> rightSplitedExpression, String[] parameters, int signal) {
		ArrayList<RationalNumber> variableWeight = new ArrayList<RationalNumber>();
		
		for (int i = 0; i < parameters.length; i++)
			variableWeight.add(RationalNumber.ZERO);
		
		for (Pair pair : leftSplitedExpression) {
			RationalNumber weight = (RationalNumber) pair.get_o1();
			int parameterIndex = (Integer) pair.get_o2();
			
			if (parameterIndex > 0) { //não é uma constante
				if (signal == -1)
					weight = weight.multiply(RationalNumber.MINUS_ONE);
					
				RationalNumber actualWeight = variableWeight.get(parameterIndex);
				
				actualWeight = actualWeight.add(weight);
				
				variableWeight.set(parameterIndex, actualWeight);
			}
		}
		
		for (Pair pair : rightSplitedExpression) {
			RationalNumber weight = (RationalNumber) pair.get_o1();
			int parameterIndex = (Integer) pair.get_o2();
			
			if (parameterIndex > 0) { //não é uma constante
				if (signal == 1)
					weight = weight.multiply(RationalNumber.MINUS_ONE);
					
				RationalNumber actualWeight = variableWeight.get(parameterIndex);
				
				actualWeight = actualWeight.add(weight);
				
				variableWeight.set(parameterIndex, actualWeight);
			}
		}
		
		return variableWeight;
	}
	
	private static List<Pair> splitExpression(List<Token> expression) {
		// TODO Auto-generated method stub
		return null;
	}

	private static int getComparerIndex(List<Token> expression) {
		
		int comparerIndex = -1;
		
		//Validate if the expression has more than one comparer
		
		for (int i = 0; i < expression.size(); i++) {
			Token token = expression.get(i);
			
			if (token instanceof ComparisonToken)
			{
				if (comparerIndex != -1)
					throwExpressionError(expression);
				
				comparerIndex = i;
			}
		}
		
		if (comparerIndex == -1)
			throwExpressionError(expression);
		
		return comparerIndex;
	}

	private static String convertExpressionToString(List<Token> expression) {
		String expressionAsString = "";
	
		for (Token token : expression)
			expressionAsString += (token.getOriginalValue().toString() + " ");
		
		return expressionAsString.substring(0, expressionAsString.length() - 1);
	}
	
	private static void throwExpressionError(List<Token> expression) {
		throw new InvalidParameterException("The expression '" + convertExpressionToString(expression) + "' is invalid.");
	}
}
