package add;

import java.util.Iterator;

public class MaxValue extends UnaryOperationSimple {
	//public int hashCode = 4;
	@Override
	public Double computeResult(Object f1, ContextADD context) {
		return context.getNodeInverseCache((Integer)f1).getMax();
				
	}

	public String toString() { return "MAX_VALUE: " + hashCode(); }

	@Override
	public Double computeResult(Object f, ContextAditADD context) {

    	if(context.isTerminalNode(f)){
    		return ((AditArc) f).c;
    	}
    	Double max= (Double) context.getMaxCache().get(f);
    	if(max==null){
    		InternalNodeKeyAdit intNodeKey=(InternalNodeKeyAdit)context.getInverseNodesCache().get(((AditArc) f).F);
    		Double maxFh=computeResult(intNodeKey.getHigh(),context);
    		Double maxFl=computeResult(intNodeKey.getLower(),context);
    		Integer Fvar= intNodeKey.getVar();
    		double maxhl=Math.max(maxFh.doubleValue(), maxFl.doubleValue());
    		max=new Double(((AditArc) f).c.doubleValue()+maxhl);
    		context.getMaxCache().put(f, max);
    		
    	}
    	return max;
	}

	@Override
	public Double computeResult(Object f1, ContextTable context) {
		Double max=Double.NEGATIVE_INFINITY;
		Double value;
		Table table1=(Table)context.inverseNodesCache.get(f1);
		Iterator it=table1.values.iterator();
		while (it.hasNext()){
			value=(Double)it.next();
			max=Math.max(value, max);
		}
		return max;
	}

	

	
	
	
}
