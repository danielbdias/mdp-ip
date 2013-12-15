package lrs;

import java.security.InvalidParameterException;
import java.util.*;

import util.Pair;
import lrs.expressiontokens.*;

/*
 * TODO:
 * 
 * hoje esse conversor aceita apenas restrições lineares (retricao) na seguinte (pseudo-)gramática:
 * cte := numero
 * sinal := +|-
 * param := ["p",numero] (e.g., p10)
 * expr_simp := cte|[sinal,cte]|param|[sinal|param]|[cte,*,param]|[sinal,cte,*,param]
 * expr_comp := expr_simp|[expr_simp,sinal,expr_comp]
 * restricao := [expr_comp,<=,expr_comp]|[expr_comp,>=,expr_comp]
 */

public class ConstraintsConverter {
	
	public static void correctConstraintsByParameters(List<LinearConstraintExpression> constraints, String[] parameters) {
		Set<String> parametersAsSet = new TreeSet<String>();
		for (String p : parameters) parametersAsSet.add(p);
		
		//add parameters with nonzero entries
		for (LinearConstraintExpression cte : constraints) {
			for (int i = 0; i < cte.getVariables().length; i++) {
				if (!cte.getVariableWeights().get(i).equals(RationalNumber.ZERO))
					parametersAsSet.add(cte.getVariables()[i]);
			}
		}
		
		//correct entry
		for (int i = 0; i < constraints.size(); i++) {
			LinearConstraintExpression cte = constraints.get(i);
			
			HashMap<String, RationalNumber> numberPerParameter = new HashMap<String, RationalNumber>();
						
			for (int j = 0; j < cte.getVariables().length; j++) {
				String param = cte.getVariables()[j];
				RationalNumber number = cte.getVariableWeights().get(j);
				
				if (parametersAsSet.contains(param)) 
					numberPerParameter.put(param, number);
			}
			
			ArrayList<RationalNumber> numbers = new ArrayList<RationalNumber>();
			
			for (int j = 0; j < parameters.length; j++) 
				numbers.add(numberPerParameter.get(parameters[j]));
			
			constraints.set(i, new LinearConstraintExpression(cte.getConstant(), numbers, parameters));
		}
	}
	
	public static LinearConstraintExpression[] convertConstraintsToLrsFormat(ArrayList constraints){
		LinearConstraintExpression[] parsedConstraints = new LinearConstraintExpression[constraints.size()];
		
		List<List<Token>> expressions = new ArrayList<List<Token>>();
		
		Set<String> parametersFound = new TreeSet<String>(); //ordena os parâmetros a medida que são inseridos
		
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

	public static LinearConstraintExpression convertSingleConstraintToLrsFormat(ArrayList constraint) {
		Object[] constraintAsArray = new Object[constraint.size()];
		
		for (int i = 0; i < constraintAsArray.length; i++)
			constraintAsArray[i] = constraint.get(i);
		 
		return convertSingleConstraintToLrsFormat(constraintAsArray);
	}

	public static LinearConstraintExpression convertSingleConstraintToLrsFormat(Object[] constraint) {
		List<Token> parsedExpression = ExpressionParser.parseExpression(constraint);
		
		Set<String> parametersFound = new TreeSet<String>();
		findNewParameters(parsedExpression, parametersFound);
		
		String[] parameters = parametersFound.toArray(new String[0]);
		
		return convertExpressionToLinearConstraintExpression(parsedExpression, parameters);
	}
	
	public static LinearConstraintExpression convertSingleConstraintToLrsFormat(Object[] constraint, String[] parameters) {
		List<Token> parsedExpression = ExpressionParser.parseExpression(constraint);	
		return convertExpressionToLinearConstraintExpression(parsedExpression, parameters);
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
		List<Token> leftExpression = expression.subList(0, comparerIndex);
		List<Token> rightExpression = expression.subList(comparerIndex + 1, expression.size());
		
		//para cada parte, faz um split por + e - e valida se são expressões lineares válidas
		List<Pair> leftSplitedExpression = splitExpression(leftExpression, parameters);
		List<Pair> rightSplitedExpression = splitExpression(rightExpression, parameters);
		
		//faz as trasformações necessárias
		ComparisonToken comparison = (ComparisonToken) expression.get(comparerIndex);
		
		if (comparison.getComparison() != ComparisonType.GreaterOrEqual && comparison.getComparison() != ComparisonType.LessOrEqual)
			throwExpressionError(expression); //sinais não suportados pelo parser
		
		int signal = (comparison.getComparison() == ComparisonType.GreaterOrEqual ? 1 : -1);
		
		RationalNumber constant = computeConstant(leftSplitedExpression, rightSplitedExpression, signal);
		
		ArrayList<RationalNumber> variableWeight = computeVariableWeights(leftSplitedExpression, rightSplitedExpression, parameters, signal);
		
		return new LinearConstraintExpression(constant, variableWeight, parameters);
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
			
			if (parameterIndex >= 0) { //não é uma constante
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
			
			if (parameterIndex >= 0) { //não é uma constante
				if (signal == 1)
					weight = weight.multiply(RationalNumber.MINUS_ONE);
					
				RationalNumber actualWeight = variableWeight.get(parameterIndex);
				
				actualWeight = actualWeight.add(weight);
				
				variableWeight.set(parameterIndex, actualWeight);
			}
		}
		
		return variableWeight;
	}
	
	private static List<Pair> splitExpression(List<Token> expression, String[] parameters) {
		List<List<Token>> splittedExpression = splitExpressionByOperations(expression);

		List<Pair> results = new ArrayList<Pair>();
		
		for (List<Token> subExpression : splittedExpression)
			results.add(parseSubExpression(subExpression, expression, parameters));
		
		return results;
	}

	private static Pair parseSubExpression(List<Token> subExpression, List<Token> expression, String[] parameters) {
		Token firstToken = subExpression.get(0);
		
		if (firstToken instanceof ParameterToken)
			return parseParameterSubExpression(subExpression, expression, parameters);
		else if (firstToken instanceof NumberToken)
			return parseNumberSubExpression(subExpression, expression, parameters);
		else if (firstToken instanceof OperationToken)
			return parseOperationSubExpression(subExpression, expression, parameters);
		else
			throwExpressionError(expression);		
		
		return null;
	}

	private static Pair parseOperationSubExpression(List<Token> subExpression, List<Token> expression, String[] parameters) {
		if (! (subExpression.get(0) instanceof OperationToken)) throwExpressionError(expression);
		if (subExpression.size() < 2) throwExpressionError(expression);
		
		OperationToken token = (OperationToken) subExpression.get(0);
		if (token.getOperation() != OperationType.Sum && token.getOperation() != OperationType.Subtration) throwExpressionError(expression);
		
		Token secondToken = subExpression.get(1);
		
		int signal = (token.getOperation() == OperationType.Subtration ? -1 : 1);
		
		Pair parsedPair = null;
		
		if (secondToken instanceof ParameterToken)
			parsedPair = parseParameterSubExpression(subExpression.subList(1, subExpression.size()), expression, parameters);
		else if (secondToken instanceof NumberToken)
			parsedPair = parseNumberSubExpression(subExpression.subList(1, subExpression.size()), expression, parameters);
		else
			throwExpressionError(expression);
		
		RationalNumber number = (RationalNumber) parsedPair.get_o1();
		if (signal == -1) number = number.multiply(RationalNumber.MINUS_ONE);
		
		return new Pair(number, parsedPair.get_o2());
	}

	private static Pair parseNumberSubExpression(List<Token> subExpression, List<Token> expression, String[] parameters) {
		if (! (subExpression.get(0) instanceof NumberToken)) throwExpressionError(expression);
		
		NumberToken token = (NumberToken) subExpression.get(0);
		RationalNumber number = RationalNumber.fromDouble(token.getValue());
		
		if (subExpression.size() == 1) {
			return new Pair(number, -1);
		}
		else if (subExpression.size() == 3) {
			if (! (subExpression.get(1) instanceof OperationToken)) throwExpressionError(expression);
			
			OperationToken secondToken = (OperationToken) subExpression.get(1);
			if (secondToken.getOperation() != OperationType.Multiplication) throwExpressionError(expression);
			
			if (! (subExpression.get(2) instanceof ParameterToken)) throwExpressionError(expression);
			Pair parsedPair = parseParameterSubExpression(subExpression.subList(2, subExpression.size()), expression, parameters);
			
			return new Pair(number, parsedPair.get_o2());
		}
		else
			throwExpressionError(expression);
		
		return null;
	}

	private static Pair parseParameterSubExpression(List<Token> subExpression, List<Token> expression, String[] parameters) {
		if (subExpression.size() != 1) throwExpressionError(expression);
		
		if (! (subExpression.get(0) instanceof ParameterToken)) throwExpressionError(expression);

		ParameterToken token = (ParameterToken) subExpression.get(0);
		
		String parameterString = token.getParameterString();
		
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].equals(parameterString))
				return new Pair(RationalNumber.ONE, i);
		}
		
		//caso não tenha encontrado o parâmetro é sinal que houve um erro (ou a expressão está mal-formada ou os parâmetros não foram detectados adequadamente) !
		throwExpressionError(expression);
		
		return null;
	}

	private static List<List<Token>> splitExpressionByOperations(List<Token> expression) {
		List<List<Token>> result = new ArrayList<List<Token>>();
		
		List<Token> segment = new ArrayList<Token>();
		
		for (Token token : expression) {
			if (token.getOriginalValue().equals("-") || token.getOriginalValue().equals("+")) {
				if (segment.size() > 0) {
					result.add(segment);
					segment = new ArrayList<Token>();
				}
			}
			
			segment.add(token);
		}
		
		if (segment.size() > 0)
			result.add(segment);
			
		return result;
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