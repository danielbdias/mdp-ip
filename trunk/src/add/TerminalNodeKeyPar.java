package add;

import graph.Graph;

public class TerminalNodeKeyPar extends TerminalNodeKey {
	  private Polynomial polynomial;
	    
	    public TerminalNodeKeyPar(Polynomial polynomial) {
			 	this.polynomial=polynomial;
			 	this.max=0.0;
			 	this.min=0.0;
	   	}
	       
	 
	    public boolean equals(Object other) {
	        // Not strictly necessary, but often a good optimization
	        if (this == other)
	          return true;
	        if (!(other instanceof TerminalNodeKeyPar))
	          return false;
	        TerminalNodeKeyPar otherA = (TerminalNodeKeyPar) other;
	        
	    	return (polynomial.equals(otherA.polynomial));
	      }
	    
	    public int hashCode() {
		    
			return polynomial.hashCode();

	    }

		@Override
		public void toGraph(Graph g, Context context) {
			// TODO Auto-generated method stub
			
		    g.addNodeLabel("#"+context.getIdCache(this), this.polynomial.toString(context,"p")/*+" id:"+context.getIdCache(this)/*+"hashCode:"+this.polynomial.hashCode()*/);
		    g.addNodeShape("#"+context.getIdCache(this), "box");
		    g.addNodeStyle("#"+context.getIdCache(this), "filled");
		  	}


		public Polynomial getPolynomial() {
			return polynomial;
		}

	

}
