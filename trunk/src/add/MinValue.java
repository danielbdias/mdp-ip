package add;

import java.util.Iterator;

public class MinValue extends UnaryOperationSimple {
	//public int hashCode = 3;
	@Override	
	public Double computeResult(Object f1, ContextADD context) {
		return context.getNodeInverseCache((Integer)f1).getMin();
	}
	
	public String toString() { return "MIN_VALUE: " + hashCode(); }

	@Override
	public Double computeResult(Object f1, ContextAditADD context) {
		return ((AditArc)f1).c;
	}
	public Double computeResult(Object f1, ContextTable context) {
		Double min=Double.POSITIVE_INFINITY;
		Double value;
		Table table1=(Table)context.inverseNodesCache.get(f1);
		Iterator it=table1.values.iterator();
		while (it.hasNext()){
			value=(Double)it.next();
			min=Math.min(value, min);
		}
		return min;
	}
}
