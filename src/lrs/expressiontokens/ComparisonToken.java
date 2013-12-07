package lrs.expressiontokens;

public class ComparisonToken extends Token {
	public ComparisonToken(String originalValue) {
		super(originalValue);

		if (originalValue.equals("="))
			this.comparison = ComparisonType.Equal;
		else if (originalValue.equals("<="))
			this.comparison = ComparisonType.LessOrEqual;
		else if (originalValue.equals("<"))
			this.comparison = ComparisonType.Less;
		else if (originalValue.equals(">="))
			this.comparison = ComparisonType.GreaterOrEqual;
		else if (originalValue.equals(">"))
			this.comparison = ComparisonType.Greater;
	}

	private ComparisonType comparison;

	public ComparisonType getComparison() {
		return comparison;
	}
	
	public static boolean validToken(Object token) {
		String comparisonAsString = token.toString();
		
		if (comparisonAsString.equals("=") ||
			comparisonAsString.equals("<=") ||
			comparisonAsString.equals("<") ||
			comparisonAsString.equals(">=") ||
			comparisonAsString.equals(">"))
			return true;
		
		return false;
	}
}
