package add;

public class BinaryOperKeyADD {

	private Integer f1;
    private Integer f2;
    private BinaryOperation op;

	public BinaryOperKeyADD(Integer f1,Integer f2, BinaryOperation op) {

		this.f1=f1;
		this.f2=f2;
		this.op=op;
		
	}

	public int hashCode() {
		return f1.intValue() + f2.intValue() + op.hashCode();  // TODO: define hashCode()
	}
	
    public boolean equals(Object other) {
        // Not strictly necessary, but often a good optimization
        if (this == other)
          return true;
        if (!(other instanceof BinaryOperKeyADD))
          return false;
        BinaryOperKeyADD otherA = (BinaryOperKeyADD) other;
        return (this.f1.equals(otherA.f1) && this.f2.equals(otherA.f2) 
        		&& this.op.hashCode() == otherA.op.hashCode());
      }
}
