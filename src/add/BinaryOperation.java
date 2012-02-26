package add;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;


public abstract class BinaryOperation extends Operation{
	
	public int hashCode = 0;
	public abstract Integer computeResult(Integer f1, Integer f2, ContextADD f1Tree);
	public abstract AditArc computeResult(AditArc f1, AditArc f2, ContextAditADD context);
	public abstract AditArcPair getNormCacheKey(AditArc f1, AditArc f2, ContextAditADD context);
	public abstract AditArc modifyResult(AditArc fr, ContextAditADD context, AditArc f1, AditArc f2);
	public abstract Object computeResult(Integer table1, Integer table2, ContextTable context);
	
	
	


	
}
