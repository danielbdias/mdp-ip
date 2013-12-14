package lrs;

import java.security.InvalidParameterException;
import java.util.*;

import lrs.expressiontokens.*;

public class ExpressionParser {
	private static final int PARAMETER_TOKEN = 0;
	private static final int NUMBER_TOKEN = 1;
	private static final int SIGNAL_TOKEN = 2;
	private static final int COMPARISON_TOKEN = 3;
	
	public static List<Token> parseExpression(ArrayList constraint) {
		Object[] constraintAsArray = new Object[constraint.size()];
		
		for (int i = 0; i < constraintAsArray.length; i++)
			constraintAsArray[i] = constraint.get(i);
		
		return parseExpression(constraintAsArray);
	}
	
	public static List<Token> parseExpression(Object[] constraint) {
		ArrayList<Token> tokens = new ArrayList<Token>();
		
		int lastTokenType = -1;
		String currentToken = "";
		
		for (int i = 0; i < constraint.length; i++) {
			Object token = constraint[i];
			
			int tokenType = getTokenType(token);
			
			if (lastTokenType == COMPARISON_TOKEN && tokenType != COMPARISON_TOKEN)
				tokens.add(new ComparisonToken(currentToken));
			
			if (tokenType != COMPARISON_TOKEN)
				currentToken = "";
			
			if (tokenType == NUMBER_TOKEN)
				tokens.add(new NumberToken(token));
			else if (tokenType == SIGNAL_TOKEN)
				tokens.add(new OperationToken(token.toString()));
			else if (tokenType == PARAMETER_TOKEN)
				tokens.add(new ParameterToken(token.toString()));
			else if (tokenType == COMPARISON_TOKEN)
				currentToken += token.toString();
			
			lastTokenType = tokenType;
		}
		
		if (lastTokenType == COMPARISON_TOKEN)
			tokens.add(new ComparisonToken(currentToken));
		
		return tokens;
	}

	private static int getTokenType(Object token) {
		
		if (NumberToken.validToken(token))
			return NUMBER_TOKEN;
		else if (ParameterToken.validToken(token))
			return PARAMETER_TOKEN;
		else if (OperationToken.validToken(token))
			return SIGNAL_TOKEN;
		else if (ComparisonToken.validToken(token))
			return COMPARISON_TOKEN;
		
		throw new InvalidParameterException("Invalid token ! Token: " + token.toString());
	}
}