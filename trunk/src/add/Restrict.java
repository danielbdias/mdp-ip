package add;

public class Restrict extends UnaryOperationComplex {
	//public int hashCode = 8;

	public String toString() { return "RESTRICT: " + hashCode(); }

	@Override
	Object computeResult(Integer idVar, UnaryOperationComplex op, Object id, Context context) {
		
		return context.reduceRestrict(idVar,op,id);
	}

	
	
	

}
