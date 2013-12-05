package lrs.expressiontokens;

public class ParameterToken extends Token {

	public ParameterToken(String originalValue) {
		super(originalValue);
		
		this.parameterString = originalValue;
		
		if (originalValue.startsWith("p"))
			originalValue = originalValue.substring(1);
		
		this.parameterNumber = Integer.parseInt(originalValue);
	}

	private int parameterNumber = 0;
	private String parameterString = null;

	public int getParameterNumber() {
		return parameterNumber;
	}
	
	public String getParameterString() {
		return parameterString;
	}

	public static boolean validToken(Object token) {
		String tokenAsString = token.toString();
		
		if (tokenAsString.matches("p[0-9]+"))
			return true;
		
		return false;
	}
}