package add;

import graph.Graph;

public class TerminalNodeKeyADD extends TerminalNodeKey {
	
    private Double value;
    
    public TerminalNodeKeyADD(Double value) {
    	this.min=value;
		this.max=value;
    	this.value=value;
   	}
    
    public Double getValue(){
    	return this.value;
    }
    
    
    public boolean equals(Object other) {
        // Not strictly necessary, but often a good optimization
        if (this == other)
          return true;
        if (!(other instanceof TerminalNodeKeyADD))
          return false;
        TerminalNodeKeyADD otherA = (TerminalNodeKeyADD) other;
        
    	return (Math.abs(value - otherA.value) <= 1e-10d);
      }
    
    public int hashCode() {
 	    
		return (int)((Double.doubleToLongBits(value) >>> 25)+1);

    }

	@Override
	public void toGraph(Graph g, Context context) {
	
		
	    g.addNodeLabel("#"+context.getIdCache(this), ContextADD._df.format(this.value));
	    g.addNodeShape("#"+context.getIdCache(this), "box");
	    g.addNodeStyle("#"+context.getIdCache(this), "filled");
	  	}

}
