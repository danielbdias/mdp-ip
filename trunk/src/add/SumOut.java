package add;

public class SumOut extends UnaryOperationComplex {
	//public int hashCode = 12;

	public String toString() { return "SUM_OUT: " + hashCode(); }

	@Override
	//it is for ADD and AditADD
	Object computeResult(Integer idVar, UnaryOperationComplex op, Object id, Context context) {
		Object redH=context.reduceRestrict(idVar,Context.RESTRICT_HIGH,id);
		//context.view(redH);
		Object redL=context.reduceRestrict(idVar, Context.RESTRICT_LOW, id);
		//context.view(redL);
		return context.apply(redH, redL, context.SUM);
		}
}
