package add;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;

public class Prod extends BinaryOperation {

	public static final Double ZERO=new Double(0.0);
	public static final Double ONE=new Double(1.0);
	public static final Polynomial ZEROPOLYNOMIAL=new Polynomial(0d,new Hashtable(),null);
	public static final Polynomial ONEPOLYNOMIAL=new Polynomial(1d,new Hashtable(),null);
	//public int hashCode = 2;
	

	public Prod() {
	
		super();
		
	}
	
  
  /**
   * In this method we suppose that we multiply VDD  x  PADD always in this order 
   */ 
  	public Integer computeResult(Integer f1, Integer f2, ContextADD context) {
		
		
		if((context.isTerminalNode(f1) && context.isTerminalNode(f2)) && context.workingWithParameterized){
			if (context.isTerminalNodeADD(f1)){
				Polynomial polynomial=new Polynomial(((TerminalNodeKeyADD)context.getNodeInverseCache(f1)).getValue(),new Hashtable(),context);
				f1=context.getTerminalNode(polynomial);
			}
			if (context.isTerminalNodeADD(f2)){
				Polynomial polynomial=new Polynomial(((TerminalNodeKeyADD)context.getNodeInverseCache(f2)).getValue(),new Hashtable(),context);
				f2=context.getTerminalNode(polynomial);
				System.out.println("Must never happen because we always multiply VDD  x  PADD");
				System.exit(0);			
			}
		}

	
    	if(context.isTerminalNodeADD(f1) &&
    		context.isTerminalNodeADD(f2)){
    		double c1= ((TerminalNodeKeyADD)context.getNodeInverseCache(f1)).getValue().doubleValue();
    		double c2= ((TerminalNodeKeyADD)context.getNodeInverseCache(f2)).getValue().doubleValue();
    		Integer fr=context.getTerminalNode(c1*c2);
    		return fr;    		 
    	}
    	
    	if(context.isTerminalNodeADD(f2)){
    		Double c2=((TerminalNodeKeyADD)context.getNodeInverseCache(f2)).getValue();
    		if (c2.equals(ZERO)){
    			return f2;
    		}
    	}
    	if(context.isTerminalNodeADD(f1)){
    		Double c1=((TerminalNodeKeyADD)context.getNodeInverseCache(f1)).getValue();
    		if (c1.equals(ZERO)){
    			if(!context.workingWithParameterized){
    			   return f1;
    			}
    			else{
    				Polynomial polynomial=new Polynomial(ZERO,new Hashtable(),context);
    				return context.getTerminalNode(polynomial);
    			}
    		}
    	}
    	if(context.isTerminalNodeADD(f2)){
    		Double c2=((TerminalNodeKeyADD)context.getNodeInverseCache(f2)).getValue();
    		if (c2.equals(ONE)){
    			return f1;
    		}
    	}
    	if(context.isTerminalNodeADD(f1)){
    		Double c1=((TerminalNodeKeyADD)context.getNodeInverseCache(f1)).getValue();
    		if (c1.equals(ONE)){
    			return f2;
    		}
    	}
    	// for parameretized ADD
    	if(context.isTerminalNodePar(f1) &&
        		context.isTerminalNodePar(f2)){
    			Polynomial c1= ((TerminalNodeKeyPar)context.getNodeInverseCache(f1)).getPolynomial();
    			Polynomial c2= ((TerminalNodeKeyPar)context.getNodeInverseCache(f2)).getPolynomial();
    			Polynomial c3=c1.prodPolynomial(c2, context);
        		Integer fr=context.getTerminalNode(c3);
        		return fr;    		 
        	}
        	
        	if(context.isTerminalNodePar(f2)){
        		Polynomial c2=((TerminalNodeKeyPar)context.getNodeInverseCache(f2)).getPolynomial();
        		if (c2.equals(ZEROPOLYNOMIAL)){
        			return f2;
        		}
        	}
        	if(context.isTerminalNodePar(f1)){
        		Polynomial c1=((TerminalNodeKeyPar)context.getNodeInverseCache(f1)).getPolynomial();
        		if (c1.equals(ZEROPOLYNOMIAL)){
        			return f1;
        		}
        	}
        	if(context.isTerminalNodePar(f2)){ //if return null then it is not necessary ask
        		Polynomial c2=((TerminalNodeKeyPar)context.getNodeInverseCache(f2)).getPolynomial();
        		if (c2.equals(ONEPOLYNOMIAL)){
        			return f1;
        			//return null;//because I dont know if the f1 has Parameterized leafs or not
        		}
        	}
        	if(context.isTerminalNodePar(f1)){
        		Polynomial c1=((TerminalNodeKeyPar)context.getNodeInverseCache(f1)).getPolynomial();
        		if (c1.equals(ONEPOLYNOMIAL)){
        			return f2;
        		}
        	}
    	
    	
    	
		
    	
    	
    	return null;
	}

  	 /**
     * In this method we suppose that we multiply VDD  x  PADD always in this order 
     */ 
  	
   ///Additive ADD//////////////////////////////////////////////////////////////////
	@Override
	public AditArc computeResult(AditArc arc1, AditArc arc2, ContextAditADD context) {
		
		if((context.isTerminalNode(arc1) && context.isTerminalNode(arc2)) && context.workingWithParameterized){
			if (context.isTerminalNodeAditADD(arc1)){//terminal node Addit is not in the inverse Cache
				
				arc1=context.getTerminalNode(arc1.c, new Polynomial(0d,new Hashtable(),context));
			}
			if (context.isTerminalNodeAditADD(arc2)){
				System.out.println("Must never happen because we always multiply VDD  x  PADD");
				System.exit(0);			
			}
		}
		
		if (context.isTerminalNodeAditADD(arc1)&& context.isTerminalNodeAditADD(arc2)){
			Double cr=arc1.c*arc2.c;
			return context.getTerminalNode(cr);
		}
		
		if (context.isTerminalNodeAditADD(arc2)&&(arc2.c.doubleValue()==0)){
			return context.getTerminalNode(new Double(0));
		}
		if (context.isTerminalNodeAditADD(arc1)&&(arc1.c.doubleValue()==0)){
			return context.getTerminalNode(new Double(0));
		}
        //new part
		if (context.isTerminalNodeAditADD(arc2)&&(arc2.c.doubleValue()==1)){
			return arc1;
		}
		if (context.isTerminalNodeAditADD(arc1)&&(arc1.c.doubleValue()==1)){
			return arc2;
		}
		// for parameretized ADD
    	
        	
        	if(context.isTerminalNodePar(arc2)&&(arc2.c.doubleValue()==0)){
        		Polynomial pol2=((TerminalNodeKeyPar)context.getNodeInverseCache(arc2.F)).getPolynomial();
        		if (pol2.equals(ZEROPOLYNOMIAL)){
        			return arc2;
        		}
        	}
    
          	if(context.isTerminalNodePar(arc1)&&(arc1.c.doubleValue()==0)){
        		Polynomial pol1=((TerminalNodeKeyPar)context.getNodeInverseCache(arc1.F)).getPolynomial();
        		if (pol1.equals(ZEROPOLYNOMIAL)){
        			return arc1;
        		}
        	}
        	
        	if(context.isTerminalNodePar(arc2)&&(arc2.c.doubleValue()==1)){//if return null then it is not necessary ask
        		Polynomial pol2=((TerminalNodeKeyPar)context.getNodeInverseCache(arc2.F)).getPolynomial();
        		if (pol2.equals(ZEROPOLYNOMIAL)){
        			return arc1;
        			//return null;//because I dont know if the f1 has Parameterized leafs or not
        		}
        	}
        	if(context.isTerminalNodePar(arc1)&&(arc1.c.doubleValue()==1)){
        		Polynomial pol1=((TerminalNodeKeyPar)context.getNodeInverseCache(arc1.F)).getPolynomial();
        		if (pol1.equals(ZEROPOLYNOMIAL)){
        			return arc2;
        		}
        	}
        	//it is better to put this condition after the others, because with polynomials the calculus with 1 is wrong
        	if(context.isTerminalNodePar(arc1) &&	context.isTerminalNodePar(arc2)){
    			//Double cr=arc1.c*arc2.c;
    			Polynomial pol1= ((TerminalNodeKeyPar)context.getNodeInverseCache(arc1.F)).getPolynomial();
    			Polynomial pol2= ((TerminalNodeKeyPar)context.getNodeInverseCache(arc2.F)).getPolynomial();
    		    Polynomial pol11=new Polynomial(arc1.c,pol1.terms,context);
    		    Polynomial pol22=new Polynomial(arc2.c, pol2.terms,context);
    			Polynomial pol3=pol11.prodPolynomial(pol22, context);
    			return context.getTerminalNode(pol3);        		
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

	public String toString() { return "PROD: " + hashCode(); }

	@Override
	public AditArc modifyResult(AditArc fr, ContextAditADD context,AditArc arc1, AditArc arc2) {
	
				
		
		//System.out.println("en modifyResult");
		if (!context.workingWithParameterized){
			AditArc arcc1=context.getTerminalNode(arc1.c);
			AditArc arcc2=context.getTerminalNode(arc2.c);
			AditArc arcf1=new AditArc(new Double(0),arc1.F);	
			AditArc arcf2=new AditArc(new Double(0),arc2.F);	
			AditArc newArcProd1=(AditArc) context.apply(arcc1, arcf2, Context.SIMPLE_PROD);
			AditArc newArcProd2=(AditArc)context.apply(arcc2, arcf1, Context.SIMPLE_PROD);
			AditArc newArcSum=(AditArc)context.apply(newArcProd1,newArcProd2, Context.SUM);
			AditArc newArcSumTot=(AditArc)context.apply(newArcSum,fr, Context.SUM);
			return new AditArc(new Double(arc1.c.doubleValue()*arc2.c.doubleValue())+newArcSumTot.c.doubleValue(),newArcSumTot.F);
		}
		else{
        //TODO: for Par Adit ADD    
			AditArc arcc1=context.getTerminalNode(arc1.c, new Polynomial(0d,new Hashtable(),context));
			AditArc arcc2=context.getTerminalNode(arc2.c,new Polynomial(0d,new Hashtable(),context));
			
			AditArc arcf1=new AditArc(new Double(0),arc1.F);	
			AditArc arcf2=new AditArc(new Double(0),arc2.F);
			
			AditArc newArcProd1=(AditArc) context.apply(arcf2,arcc1,Context.SIMPLE_PROD);			
			AditArc newArcProd2=(AditArc)context.apply(arcf1,arcc2,Context.SIMPLE_PROD);
			
			AditArc newArcSum=(AditArc)context.apply(newArcProd1,newArcProd2, Context.SUM);
			AditArc newArcSumTot=(AditArc)context.apply(newArcSum,fr, Context.SUM);
			return new AditArc(new Double(arc1.c.doubleValue()*arc2.c.doubleValue())+newArcSumTot.c.doubleValue(),newArcSumTot.F);
			
		}
	}

	//For tables
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
			Polynomial pol1=null;
			Double double1=null;
			if(context.getValueTable(varAssigment,table1) instanceof Polynomial){
				pol1=(Polynomial) context.getValueTable(varAssigment,table1);
			}
			else if(context.workingWithParameterized){
				pol1=new Polynomial((Double)context.getValueTable(varAssigment,table1),new Hashtable(),context);
				//context.getTerminalNode(pol1);
			}
			else{
			    double1=new Double((Double)context.getValueTable(varAssigment,table1));     	
			}
			if(context.workingWithParameterized){
				Polynomial pol2=(Polynomial) context.getValueTable(varAssigment,table2);	
				valuesNew.add(pol1.prodPolynomial(pol2, context));
			}
			else{
				Double double2=(Double) context.getValueTable(varAssigment,table2);
				valuesNew.add(double1*double2);
			}
		}
		Table newTable=new Table(varsNew,valuesNew);
		Integer idNew=context.getNextUnllocatedId();
	   	context.putNodeCache(newTable, idNew);
     	return idNew;
	}

}
