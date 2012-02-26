package add;


import graph.Graph;

public class InternalNodeKeyADD extends InternalNodeKey {
	
    private Integer high;
    private Integer lower;

    
    public InternalNodeKeyADD(Integer var, Integer high, Integer lower, double max, double min) {
		// TODO Auto-generated constructor stub
    	super();
    	this.var=var;
    	this.high=high;
    	this.lower=lower;
    	this.min = new Double(min);
    	this.max = new Double(max);
	}
    
    
    public Integer getHigh(){
    	return this.high;
    }
    public Integer getLower(){
    	return this.lower;
    }
    
    
    
    public boolean equals(Object other) {
        // Not strictly necessary, but often a good optimization
        if (this == other)
          return true;
        if (!(other instanceof InternalNodeKeyADD))
          return false;
        InternalNodeKeyADD otherA = (InternalNodeKeyADD) other;
        return (this.var.equals(otherA.var)
            && this.high.equals(otherA.high)&& this.lower.equals(otherA.lower));
      }
    
    public int hashCode() { 
    	  int hash = 1;
    	    hash = hash * 31 + var.hashCode();
    	    hash = hash * 31 +high.hashCode();
    	    hash = hash * 31 +lower.hashCode();
    	    return hash;
    		 }
	@Override
	public void toGraph(Graph g, Context fTree) {
		// TODO Auto-generated method stub
		Integer id=fTree.getIdCache(this);
		//System.out.println("Adding " + id + " x" + this.var + ":" + this.lower + " / " + this.high);
	    g.addNodeLabel("#"+id, /*"#"+id + " : x"+*/"x" + this.var+ " wH:"+ContextADD._df.format(this.probWeightH) +" wL:"+ContextADD._df.format(this.probWeightL)/* + " : " + this.lower + " / " + this.high*/);
	    g.addNodeShape("#"+id, "ellipse");
	    g.addNodeStyle("#"+id, "filled");
	    g.addUniLink("#"+id, "#"+this.lower, "black", "dashed", Graph.EMPTY_STR);
	    g.addUniLink("#"+id, "#"+this.high, "black", "solid", Graph.EMPTY_STR);
	    NodeKey low = fTree.getNodeInverseCache(this.lower);
	    low.toGraph(g, fTree);
	    if (!this.lower.equals(this.high)){
		    NodeKey high = fTree.getNodeInverseCache(this.high);
		    // Error here: high is not the node for this.high???
		    //System.out.println(this.high + " == " + high.)
		    high.toGraph(g, fTree);
	    }
	  }


}
