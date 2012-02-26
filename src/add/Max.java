package add;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;

public class Max extends BinaryOperation {
	

	public Max(){
		super();
	}
	
	public String toString() { return "MAX: " + hashCode(); }

	public Integer computeResult(Integer f1, Integer f2, ContextADD context) {
		double f1max= ((NodeKey)context.getNodeInverseCache(f1)).getMax();
		double f1min= ((NodeKey)context.getNodeInverseCache(f1)).getMin();
		double f2max= ((NodeKey)context.getNodeInverseCache(f2)).getMax();
		double f2min= ((NodeKey)context.getNodeInverseCache(f2)).getMin();
		if(f1max<=f2min){
			return f2;
		}
		if(f2max<=f1min){
			return f1;
		}
		return null;
	}


	@Override
	public AditArc computeResult(AditArc f1, AditArc f2, ContextAditADD context) {
		if(f1.F.compareTo(f2.F)==0 ){
			double max=Math.max(f1.c.doubleValue(), f2.c.doubleValue());
			if (max==f1.c.doubleValue()){
				return f1;
			}
			else{
				return f2;
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
	public Object computeResult(Integer idTable1, Integer idTable2, ContextTable context) {
		Table table1=(Table)context.inverseNodesCache.get(idTable1);
		Table table2=(Table)context.inverseNodesCache.get(idTable2);
		TreeSet varsNew=new TreeSet(table1.vars);
		varsNew.addAll(table2.vars);
		int sizeNew=(int)Math.pow(2, varsNew.size());
		ArrayList valuesNew=new ArrayList(sizeNew);
		for(int i=0;i<sizeNew;i++){
			Hashtable varAssigment=context.getVarAssigment(i,varsNew.size(),varsNew); // var-->assigment
			Double val1=(Double)context.getValueTable(varAssigment,table1);
			Double val2=(Double)context.getValueTable(varAssigment,table2);	
			valuesNew.add(Math.max(val1, val2));
		}
		Table newTable=new Table(varsNew,valuesNew);
		Integer idNew=context.getNextUnllocatedId();
	   	context.putNodeCache(newTable, idNew);
     	return idNew;
		
	}

}
