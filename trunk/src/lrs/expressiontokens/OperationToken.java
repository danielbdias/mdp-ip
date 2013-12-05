package lrs.expressiontokens;

public class OperationToken extends Token {
	public OperationToken(String originalValue) {
		super(originalValue);

		if (originalValue.equals("+"))
			this.operation = OperationType.Sum;
		else if (originalValue.equals("-"))
			this.operation = OperationType.Subtration;
		else if (originalValue.equals("*"))
			this.operation = OperationType.Multiplication;
		else if (originalValue.equals("/"))
			this.operation = OperationType.Division;
	}

	private OperationType operation;

	public OperationType getOperation() {
		return operation;
	}
	
	public static boolean validToken(Object token) {
		String operationAsString = token.toString();
		
		if (operationAsString.equals("+") ||
			operationAsString.equals("-") ||
			operationAsString.equals("*") ||
			operationAsString.equals("/"))
			return true;
		
		return false;
	}
}
