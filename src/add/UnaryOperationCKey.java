package add;

public class UnaryOperationCKey {
	
    private Integer idVar;
    private UnaryOperation op;
    private Object id;
    
    

	public UnaryOperationCKey(Integer idVar, UnaryOperation op, Object id) {
		//id can be an integer 
	
		this.idVar=idVar;
		this.op=op;
		this.id=id;
		
	}

	public int hashCode() {
		return idVar.hashCode() +  op.hashCode()+id.hashCode();
	}
	
    public boolean equals(Object other) {
        // Not strictly necessary, but often a good optimization
        if (this == other)
          return true;
        if (!(other instanceof UnaryOperationCKey)){
        	
          return false;
        }
        UnaryOperationCKey otherA = (UnaryOperationCKey) other;
        return ( this.idVar.equals(otherA.idVar) 
        		&& this.op == otherA.op && this.id.equals(otherA.id));
      }
    public String toString() {
    	return "UnaryNodeKey: < x" + idVar + " op: " + op.toString() + " id: " + id.toString();
    }

}
