package add;

import java.util.Hashtable;

public class PrincipalTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ContextTable T1=new ContextTable();
		Integer label1=T1.getIdLabelProd("1");
		Integer label2=T1.getIdLabelProd("2");
		Integer label4=T1.getIdLabelProd("2");
		Integer label5=T1.getIdLabelProd("8");
		Integer labelborrar=T1.getIdLabelProd("2020");
		Integer label3=T1.getIdLabelProd("3,4");
		
		
		Hashtable terms=new Hashtable();
		terms.put(label1, 7d);
		terms.put(label2, 8d);
		terms.put(label3, 3d);
		Polynomial pol1= new  Polynomial(new Double(5),terms,T1);
		System.out.println("pol1: "+pol1.toString(T1,"p"));
		Hashtable terms2=new Hashtable();
		terms2.put(label4, 3d);
		terms2.put(label5, 5d);
		Polynomial pol2= new  Polynomial(new Double(4),terms2,T1);
		System.out.println("pol2: "+pol2.toString(T1,"p"));
		Polynomial pol3=pol1.sumPolynomial(pol2);
		
		Polynomial pol4=pol1.subPolynomial(pol2);
		
		
		Polynomial pol5=pol1.prodPolynomial(pol2,T1);
		//System.out.println(pol5.toString(T1));
		
		Polynomial polC=new Polynomial(5.0,new Hashtable(),T1);
		Polynomial polJ=new Polynomial(6.0,new Hashtable(),T1);
		
		 Integer NodeA=T1.getTerminalNode(pol1);
		 Integer NodeB=T1.getTerminalNode(polC);
		 Integer NodeJ=T1.getTerminalNode(polJ);
		 
		 Integer NodeC=T1.getInternalNode(new Integer(2), NodeA, NodeB);
		 System.out.println("NodeC:  "+((Table)T1.getInverseNodesCache().get(NodeC)).toString(T1));
		 Integer NodeD=T1.getInternalNode(new Integer(1), NodeB, NodeJ);
		 System.out.println("NodeD:  "+((Table)T1.getInverseNodesCache().get(NodeD)).toString(T1));
		 Integer NodeK=(Integer)T1.apply(NodeC, NodeD, Context.SUM);
		 System.out.println("NodeK:  "+((Table)T1.getInverseNodesCache().get(NodeK)).toString(T1));
		 Integer NodeF=(Integer)T1.apply(2, Context.RESTRICT_LOW, NodeK);
		 System.out.println("NodeF:  "+((Table)T1.getInverseNodesCache().get(NodeF)).toString(T1));
		 
		 Integer A=(Integer)T1.getTerminalNode(5d);
		 Integer B=(Integer)T1.getTerminalNode(10d);
		 Integer C=(Integer)T1.getTerminalNode(7d);
		 Integer D=(Integer)T1.getTerminalNode(2d);
		 
		 Integer E=T1.getInternalNode(new Integer(1), A, B);
		 Integer F=T1.getInternalNode(new Integer(2), C, D);
		 
		 Integer NodeZ=(Integer)T1.apply(E, F, Context.MAX);
		 System.out.println("NodeZ:  "+((Table)T1.getInverseNodesCache().get(NodeZ)).toString(T1));
		 T1.toGraph(NodeZ);
	}

}
