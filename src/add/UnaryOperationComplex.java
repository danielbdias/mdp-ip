package add;

public abstract class UnaryOperationComplex extends UnaryOperation {
	//public int hashCode = 0;
	abstract Object computeResult(Integer idVar,UnaryOperationComplex op,Object id,Context context);
}
