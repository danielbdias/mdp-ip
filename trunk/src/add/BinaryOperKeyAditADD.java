package add;

public class BinaryOperKeyAditADD {
	private AditArc f1;
    private AditArc f2;
    private BinaryOperation op;

	public BinaryOperKeyAditADD(AditArc f1,AditArc  f2, BinaryOperation op) {
		
		this.f1=f1;
		this.f2=f2;
		this.op=op;
		
	}

	public int hashCode() {
		return f1.hashCode() + f2.hashCode() + op.hashCode();
	}
	
    public boolean equals(Object other) {
        // Not strictly necessary, but often a good optimization
        if (this == other)
          return true;
        if (!(other instanceof BinaryOperKeyAditADD))
          return false;
        BinaryOperKeyAditADD otherA = (BinaryOperKeyAditADD) other;
        return (this.f1.equals(otherA.f1) && this.f2.equals(otherA.f2) 
        		&& this.op.equals(otherA.op));
      }
}
