package add;

public class Min extends BinaryOperation {

	//public int hashCode = 101;

	public Min() {
		
		super();
	}
	
	public String toString() { return "MIN: " + hashCode(); }

	public Integer computeResult(Integer f1, Integer f2, ContextADD context) {
		double f1max= ((NodeKey)context.getNodeInverseCache(f1)).getMax();
		double f1min= ((NodeKey)context.getNodeInverseCache(f1)).getMin();
		double f2max= ((NodeKey)context.getNodeInverseCache(f2)).getMax();
		double f2min= ((NodeKey)context.getNodeInverseCache(f2)).getMin();
		if(f1max<=f2min){
			return f1;
		}
		if(f2max<=f1min){
			return f2;
		}
		return null;
	}
	@Override
	public AditArc computeResult(AditArc f1, AditArc f2, ContextAditADD context) {
		if(f1.F.compareTo(f2.F)==0 ){
			double max=Math.max(f1.c.doubleValue(), f2.c.doubleValue());
			if (max==f1.c.doubleValue()){
				return f2;
			}
			else{
				return f1;
			}
			
		}
		return null;
	}


	@Override
	public AditArcPair getNormCacheKey(AditArc f1, AditArc f2, ContextAditADD context) {
		AditArc f1prime,f2prime;
		f1prime= new AditArc(new Double(0),f1.F);
		f2prime=new AditArc(new Double(f2.c.doubleValue()-f1.c.doubleValue()),f2.F);
		return new AditArcPair(f1prime,f2prime);	
	}


	@Override
	public AditArc modifyResult(AditArc fr, ContextAditADD context, AditArc f1, AditArc f2) {
		AditArc arc1=context.getTerminalNode(f1.c);
		AditArc newArcSum=(AditArc)context.apply(arc1,fr, Context.SUM);
		return newArcSum;
	}

	@Override
	public Object computeResult(Integer table1, Integer table2, ContextTable context) {
        System.out.println("not used MIN over Parameterized Table");
        System.exit(0);
		return null;
	}


}
