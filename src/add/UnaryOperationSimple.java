package add;

public abstract class UnaryOperationSimple extends UnaryOperation {
	//public int hashCode = 0;
	public abstract Double computeResult(Object f1, ContextADD context); 
	public abstract Double computeResult(Object f1, ContextAditADD context);
	public abstract Double computeResult(Object f1, ContextTable context);
}
