package add;

import graph.Graph;

public class InternalNodeKeyAdit extends InternalNodeKey{
	
	
    private AditArc high;
    private AditArc lower;
    
    
    public InternalNodeKeyAdit(Integer var,Double ch, Double cl, Integer Fh, Integer Fl) {
		
    	
    	this.var=var;
    	this.high=new AditArc(ch,Fh);
    	this.lower=new AditArc(cl,Fl);
    	//this.min=min;
    }
    public InternalNodeKeyAdit(Integer var,AditArc ArcFh, AditArc ArcFl) {
	    	
    	this.var=var;
    	this.high=ArcFh;
    	this.lower=ArcFl;
    	//this.min=min;
    }
    
    public String toString() {
    	return "AditNodeKey: < x" + var + ", " + lower.c + " + " + lower.F + ", " + high.c + " + " + high.F + " >";
    }
    
    public AditArc getHigh(){
    	return this.high;
    }
    public AditArc getLower(){
    	return this.lower;
    }
    
    
    
    public boolean equals(Object other) {
        // Not strictly necessary, but often a good optimization
        if (this == other)
          return true;
        if (!(other instanceof InternalNodeKeyAdit))
          return false;
        InternalNodeKeyAdit otherA = (InternalNodeKeyAdit) other;
        
        return ((this.var.equals(otherA.var))
            && (this.high.equals(otherA.high))
            && (this.lower.equals(otherA.lower))
         );
            
      }
        
    public int hashCode() {
   		return this.var.hashCode()+this.high.hashCode()+this.lower.hashCode();

    }

    
   
	@Override
	public void toGraph(Graph g, Context context) {
	
		Integer id=context.getIdCache(this);
		//System.out.println("Adding " + id + " x" + this.var + ":" + this.lower + " / " + this.high);
	    g.addNodeLabel("#"+id, " x" + this.var/* + " : " + this.lower + " / " + this.high*/);
	    g.addNodeShape("#"+id, "ellipse");
	    g.addNodeStyle("#"+id, "filled");
	    g.addUniLink("#"+id, "#"+this.lower.F, "black", "dashed", ",  " + context._df.format(this.lower.c) + " +  ,");
	    g.addUniLink("#"+id, "#"+this.high.F, "black", "solid", ",  " + context._df.format(this.high.c)  + " +  ,");
	    NodeKey low = context.getNodeInverseCache(this.lower.F);
	    if ( !context.isTerminalNode(this.lower)){
	    	// Is not terminal node
	          low.toGraph(g, context);
	    }
	    else{// if(context.workingWithParameterized){
	    	//is a terminal node Par Adit ADD or AditADD
	    	this.lower.toGraph(g, context);
	    	
	    }
	    /*else{//terminal node AditADD
	    	this.lower.toGraph(g, context);
	    }*/
	    
	    //else{
	    //	g.addNodeLabel("#"+id, /*"#"+id + " : x"+*/"0" /* + " : " + this.lower + " / " + this.high*/);
		//    g.addNodeShape("#"+id, "ellipse");
		//    g.addNodeStyle("#"+id, "filled");
	    //}
	    	
	    if (!this.lower.F.equals(this.high.F)) {

			NodeKey hi = context.getNodeInverseCache(this.high.F);
			// Error here: high is not the node for this.high???
			//System.out.println(this.high + " == " + high.)
			if (!context.isTerminalNode(this.high)) {
				hi.toGraph(g, context);
			}
			else {
				this.high.toGraph(g, context);
			}
			//else{
			//	g.addNodeLabel("#"+id, /*"#"+id + " : x"+*/"0" /* + " : " + this.lower + " / " + this.high*/);
			//    g.addNodeShape("#"+id, "ellipse");
			//    g.addNodeStyle("#"+id, "filled");
			//}

		}
	  }



}
