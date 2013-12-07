package lrs;

public class RationalNumber {
	
	public static final RationalNumber ZERO = new RationalNumber(0, 1);
	
	public static final RationalNumber ONE = new RationalNumber(1, 1);
	
	public static final RationalNumber MINUS_ONE = new RationalNumber(-1, 1);
	
	public RationalNumber(int numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	private int numerator = 0;
	
	private int denominator = 0;
	
	public int getNumerator() {
		return numerator;
	}
	
	public int getDenominator() {
		return denominator;
	}
	
	public RationalNumber add(RationalNumber another) {
		int numerator = this.numerator * another.denominator + another.numerator * this.denominator;
		int denominator = this.denominator * another.denominator;
		
		return new RationalNumber(numerator, denominator);
	}
	
	public RationalNumber subtract(RationalNumber another) {
		int numerator = this.numerator * another.denominator - another.numerator * this.denominator;
		int denominator = this.denominator * another.denominator;
		
		return new RationalNumber(numerator, denominator);
	}
	
	public RationalNumber multiply(RationalNumber another) {
		int numerator = this.numerator * another.numerator;
		int denominator = this.denominator * another.denominator;
		
		return new RationalNumber(numerator, denominator);
	}
	
	public RationalNumber divide(RationalNumber another) {
		int numerator = this.numerator * another.denominator;
		int denominator = this.denominator * another.numerator;
		
		return new RationalNumber(numerator, denominator);
	}
	
	@Override
	public String toString() {
		if (this.denominator == 1)
			return Integer.toString(this.numerator);
		else
			return String.format("%d/%d", this.numerator, this.denominator);
	}
	
	public static RationalNumber fromDouble(double number) {
		String numberAsString = Double.toString(number);
		
		int pointIndex = numberAsString.indexOf('.');
		
		String numeratorAsString = null;
		String denominatorAsString = null;
		
		if (pointIndex <= 0) {
			numeratorAsString = numberAsString;
			denominatorAsString = "";
		}
		else {
			denominatorAsString = numberAsString.substring(pointIndex + 1);
			
			int tempDenominator = Integer.parseInt(denominatorAsString);
			
			if (tempDenominator == 0) {
				numeratorAsString = numberAsString.substring(0, pointIndex);
				denominatorAsString = "";
			}
			else
				numeratorAsString = numberAsString.replace(".", "");
		}
		
		int numerator = Integer.parseInt(numeratorAsString);
		int denominator = (int) Math.pow(10.0, denominatorAsString.length());
		
		return new RationalNumber(numerator, denominator);
	}
}