package add;

public class PrincipalAditADD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//ADDContext T1=new ADDContext();
		ContextAditADD  T1=new ContextAditADD();
		
        AditArc arc1=T1.getTerminalNode(0d);
        AditArc arc2=T1.getTerminalNode(1d);
        T1.view(arc2); 
        AditArc arc3=T1.getInternalNode(2, arc1, arc2);
       // T1.view(arc3);
        AditArc arc2a=T1.getTerminalNode(14d);
        /*AditArc arc4=T1.getInternalNode(2, arc1, arc2a);
        AditArc arc5=T1.getInternalNode(1, arc3,arc4);
        AditArc arc5_reduce = T1.reduce(arc5);
        T1.view(arc5_reduce);
        Object Hr=T1.apply(new Integer(2), Context.RESTRICT_LOW, arc5_reduce);
        T1.view(Hr);*/
		//Object ResultSumXi=getSumADD(T1, 10);
	    //T1.view(ResultSumXi);
	    //Object Res=T1.apply(ResultSumXi, ResultSumXi, Context.PROD);
	   // T1.view(Res);
	  	//Object ResultSumXiExp=getSumADDExponential(T1, 1);
	    //T1.view(ResultSumXiExp);
	    //System.out.println(T1.apply(ResultSumXiExp, Context.MAXVALUE));
	    //System.out.println(T1.apply(ResultSumXiExp, Context.MINVALUE));
	    //Object result=T1.apply(arc2a, ResultSumXiExp, ADDContext.PROD);
	    //T1.view(result);
	    AditArc A=T1.getTerminalNode(22d);
        AditArc B=T1.getTerminalNode(10d);
        AditArc C=T1.getInternalNode(1, A, B);
        C=T1.reduce(C);
        T1.view(C);
        Object D=T1.apply(C, arc2a, Context.PROD);
        T1.view(D);
	    
	    
	}

	public static  Object getSumADD(Context T1, int n) {
		Object result = T1.getTerminalNode(0d);
		for (int var_id = 0; var_id <= n; var_id++) {
			Object Node4=T1.getTerminalNode(0);
			Object  Node5=T1.getTerminalNode(1);
			Object Node=T1.getInternalNode(new Integer(var_id), Node5, Node4);
			result=T1.apply(result, Node, ContextADD.SUM);
		}
		return result;
	}
	public static Object  getSumADDExponential(Context T1, int n) {
		Object result = T1.getTerminalNode(0);
		double val=1;
		for (int var_id = 0; var_id <= n; var_id++) {
			Object Node4=T1.getTerminalNode(0);
			Object Node5=T1.getTerminalNode(val);
			Object Node=T1.getInternalNode(new Integer(var_id), Node5, Node4);
			//T1.view(Node);
			result=T1.apply(result, Node, ContextADD.SUM);
			val=val*2;
	}
		return result;
	}

/*	public static Object getSubADD(Context T1, int n) {
		Object result = T1.getTerminalNode(0);
		for (int var_id = 0; var_id <= n; var_id++) {
			Object Node4=T1.getTerminalNode(0);
			Object Node5=T1.getTerminalNode(1);
			Object Node=T1.getInternalNode(new Integer(var_id), Node5, Node4);
			//T1.view(Node);
			result=T1.apply(result, Node, ADDContext.SUB);
		}
		return result;
	}
*/
	
}
