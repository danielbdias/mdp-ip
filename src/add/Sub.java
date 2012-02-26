package add;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;



public class Sub extends BinaryOperation{
	public static final Double ZERO=new Double(0.0);
	public static final Polynomial ZEROPOLYNOMIAL=new Polynomial(0d,new Hashtable(),null);
	

	public Sub() {
		
		super();
	}
	
	public String toString() { return "SUB: " + hashCode(); }

	public Integer computeResult(Integer f1, Integer f2, ContextADD context) {
		
		if((context.isTerminalNodeADD(f1) && context.isTerminalNodePar(f2)) || (context.isTerminalNodePar(f1) && context.isTerminalNodeADD(f2)) ){
			if((context.isTerminalNodeADD(f1) && context.isTerminalNodePar(f2))){
				System.out.println("VDD-PADD");
			}
			else{
				System.out.println("PADD-VDD");
			}
			System.out.println("Must never happen because we never sum VDD+PADD or PADD+VDD, they must be the same type");
			System.exit(0);			
	   }
		
    	if(context.isTerminalNodeADD(f1) &&	context.isTerminalNodeADD(f2)){
    		double c1= ((TerminalNodeKeyADD)context.getNodeInverseCache(f1)).getValue().doubleValue();
    		double c2= ((TerminalNodeKeyADD)context.getNodeInverseCache(f2)).getValue().doubleValue();
    		Integer fr=context.getTerminalNode(c1-c2);
    		return fr;    		 
    	}
    	
    	if(context.isTerminalNodeADD(f2)){
    		Double c2=((TerminalNodeKeyADD)context.getNodeInverseCache(f2)).getValue();
    		if (c2.equals(ZERO)){
    			return f1;
    		}
    	}
    	// Parameterized ADD//////////
    	
       	if(context.isTerminalNodePar(f1) &&	context.isTerminalNodePar(f2)){
        		Polynomial c1= ((TerminalNodeKeyPar)context.getNodeInverseCache(f1)).getPolynomial();
        		Polynomial c2= ((TerminalNodeKeyPar)context.getNodeInverseCache(f2)).getPolynomial();
        		Polynomial c3=c1.subPolynomial(c2);
        		Integer fr=context.getTerminalNode(c3);
        		return fr;    
       	}
        	
        	if(context.isTerminalNodePar(f2)){
        		Polynomial c2=((TerminalNodeKeyPar)context.getNodeInverseCache(f2)).getPolynomial();
        		if (c2.equals(ZEROPOLYNOMIAL)){
        			return f1;
        		}
        	}
  
    	return null;
	}
	
	//For Adit ADD///////////////////////////////////////////////////////
	@Override
	public AditArc computeResult(AditArc arc1, AditArc arc2, ContextAditADD context) {
		if((context.isTerminalNodeAditADD(arc1) && context.isTerminalNodePar(arc2)) || (context.isTerminalNodePar(arc1) && context.isTerminalNodeAditADD(arc2)) ){
			System.out.println("Must never happen because we never sum VDD+PADD or PADD+VDD, they must be the same type");
			System.exit(0);			
	   }
		if (context.isTerminalNodeAditADD(arc1)&& context.isTerminalNodeAditADD(arc2)){
			Double cr=arc1.c-arc2.c;
			return context.getTerminalNode(cr);
		}
		
		
		if (context.isTerminalNodeAditADD(arc2)){
			Double cr=arc1.c-arc2.c;
			return new AditArc(cr,arc1.F);
		}
		
   	// Parameterized ADD//////////
    	
       	if(context.isTerminalNodePar(arc1) &&	context.isTerminalNodePar(arc2)){
       		    Double cr=arc1.c-arc2.c;
        		Polynomial pol1= ((TerminalNodeKeyPar)context.getNodeInverseCache(arc1.F)).getPolynomial();
        		Polynomial pol2= ((TerminalNodeKeyPar)context.getNodeInverseCache(arc2.F)).getPolynomial();
        		Polynomial pol3=pol1.subPolynomial(pol2);
        		return context.getTerminalNode(cr,pol3);
        		
       	}
        	
        	if(context.isTerminalNodePar(arc2)){
        		Double cr=arc1.c-arc2.c;
        		Polynomial pol2=((TerminalNodeKeyPar)context.getNodeInverseCache(arc2.F)).getPolynomial();
        		if (pol2.equals(ZEROPOLYNOMIAL)){
        			return new AditArc(cr,arc1.F);
          		}
        	}	
		
		if((arc1.F.compareTo(arc2.F)==0 )&& ((arc1.c.doubleValue()!=0 || arc2.c.doubleValue()!=0)) ){
			Double cr=arc1.c-arc2.c;
			if(!context.workingWithParameterized){
				return context.getTerminalNode(cr);
			}
			else{
				return context.getTerminalNode(cr,ZEROPOLYNOMIAL);
			}
		}	
		
		return null;
	}


	@Override
	public AditArcPair getNormCacheKey(AditArc f1, AditArc f2, ContextAditADD context) {
		
		AditArc f1prime,f2prime;
		f1prime= new AditArc(new Double(0),f1.F);
		f2prime=new AditArc(new Double(0),f2.F);
		return new AditArcPair(f1prime,f2prime);		
	}


	@Override
	public AditArc modifyResult(AditArc fr, ContextAditADD context, AditArc f1, AditArc f2) {
		
		return new AditArc(new Double(f1.c.doubleValue()-f2.c.doubleValue())+fr.c,fr.F);
	}
	
//	For tables
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
			if(context.workingWithParameterized){
				Polynomial pol1=(Polynomial) context.getValueTable(varAssigment,table1);
				Polynomial pol2=(Polynomial) context.getValueTable(varAssigment,table2);	
				valuesNew.add(pol1.subPolynomial(pol2));
			}
			else{
				Double double1=(Double) context.getValueTable(varAssigment,table1);
				Double double2=(Double) context.getValueTable(varAssigment,table2);	
				valuesNew.add(double1-double2);
			}
		}
		Table newTable=new Table(varsNew,valuesNew);
		Integer idNew=context.getNextUnllocatedId();
	   	context.putNodeCache(newTable, idNew);
     	return idNew;
	}
	

}
