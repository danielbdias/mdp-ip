package add;

import java.util.Hashtable;

public class SimpleProd extends BinaryOperation {

	public static final Double ZERO=new Double(0.0);
	public static final Double ONE=new Double(1.0);
	public static final Polynomial ZEROPOLYNOMIAL=new Polynomial(0d,new Hashtable(),null);
	public static final Polynomial ONEPOLYNOMIAL=new Polynomial(1d,new Hashtable(),null);
	//public int hashCode = 3;

	public SimpleProd() {
	
		super();
	}
	
	public String toString() { return "SIMPLE_PROD: " + hashCode(); }

	// ADDs
	public Integer computeResult(Integer f1, Integer f2, ContextADD fTree) {
		System.out.println("SimpleProd: Do not call for ADDs");
    	System.exit(1);
    	return null;
	}

	// Additive ADDs 
	@Override
	public AditArc computeResult(AditArc arc1, AditArc arc2, ContextAditADD context) {
		//System.out.println("simpleproduct");
		
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

		// for parameretized Adit ADD
    	
        	
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
        	
        	if(context.isTerminalNodePar(arc2)&&(arc2.c.doubleValue()==1)){
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
        	
        	if(context.isTerminalNodePar(arc1) &&	context.isTerminalNodePar(arc2)){
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
		return new AditArcPair(f1,f2);	
	}


	@Override
	public AditArc modifyResult(AditArc fr, ContextAditADD context,AditArc f1, AditArc f2) {
		//it is not necessary for SimpleProduct because it does not work with normalized nodes
		return fr;
	}

	@Override
	public Object computeResult(Integer table1, Integer table2, ContextTable context) {
		System.out.println("Simple Product not implemented for Tables");
		System.exit(0);
		return null;
	}

	

}
