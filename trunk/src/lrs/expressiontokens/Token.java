package lrs.expressiontokens;

public abstract class Token {
	public Token(Object originalValue) {
		this.originalValue = originalValue;
	}
	
	private Object originalValue = null;

	public Object getOriginalValue() {
		return originalValue;
	} 
}
