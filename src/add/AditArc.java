package add;


import graph.Graph;

public class AditArc {
	Double c;
	Integer F;
	public AditArc(Double c, Integer F){
		this.c=c;
		this.F=F;
	}
	public AditArc(AditArc to_copy) {
		this.c = to_copy.c;
		this.F = to_copy.F;
	}
	
	public Double getC(){
		return c;
	}
	public Integer getF(){
		return F;
	}
	
	 public boolean equals(Object other) {
	        // Not strictly necessary, but often a good optimization
	        if (this == other)
	          return true;
	        if (!(other instanceof AditArc))
	          return false;
	        AditArc otherA = (AditArc) other;
	        
	        return (
	            (this.F.equals(otherA.F))&& 
	            (Math.abs(this.c.doubleValue() -otherA.c.doubleValue()) <= 1e-10d)
	           );
	            
	    }
	        
	    public int hashCode() {
	   		return (int)(F.hashCode()+((Double.doubleToLongBits(c) >>> 25)+1));

	    }
		public void toGraph(Graph g, Context context) {
			toGraph(g, context,null);
		}
		
		public void toGraph(Graph g, Context context, String parent_name) {

			if(((ContextAditADD)context).isTerminalNode(this)){
				if(!context.workingWithParameterized){
					if (parent_name != null) {
						g.addNodeLabel(parent_name, "ROOT");
						g.addNodeShape(parent_name, "diamond");
						g.addNodeStyle(parent_name, "filled");
						g.addUniLink(parent_name, "#"+"0", "black", "dashed", context._df.format(this.c));
					}

					g.addNodeLabel("#"+"0", "0");
					g.addNodeShape("#"+"0", "box");
					g.addNodeStyle("#"+"0", "filled");
				}
				else{
					if (parent_name != null) {
						g.addNodeLabel(parent_name, "ROOT");
						g.addNodeShape(parent_name, "diamond");
						g.addNodeStyle(parent_name, "filled");
						g.addUniLink(parent_name, "#"+this.F, "black", "dashed", context._df.format(this.c));
					}
					TerminalNodeKeyPar terminalNodePar = (TerminalNodeKeyPar)context.getNodeInverseCache(this.F);
					g.addNodeLabel("#"+this.F,terminalNodePar.getPolynomial().toString(context,"p"));
					g.addNodeShape("#"+this.F, "box");
					g.addNodeStyle("#"+this.F, "filled");
					
				}
			}
			else{

				InternalNodeKeyAdit node=(InternalNodeKeyAdit)context.getNodeInverseCache(this.F);

				g.addNodeLabel("ROOT", "ROOT");
				g.addNodeShape("ROOT", "diamond");
				g.addNodeStyle("ROOT", "filled");
				g.addUniLink("ROOT", "#"+this.F, "black", "solid", context._df.format(this.c));

				node.toGraph(g, context);
			}

		}
	
		public String toString() {
			return "< " + c + ", " + F + " >";
		}	
	
}
