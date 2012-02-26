package add;

import graph.Graph;

public abstract class NodeKey {

	protected Double min;
    protected Double max;
    //for RTDP BRTDP/////
    protected double probWeightH;
    protected double probWeightL;
    
    
    public NodeKey(){
    	
    }
	/*public NodeKey(Double min, Double max) {
		
		this.min=min;
		this.max=max;
	}*/

	public Double getMin(){
		return this.min;
	}
	
	public Double getMax(){
		return this.max;
	}
	/*public void setMin(Double min){
		this.min=min;
	}
	public void setMax(Double max){
		this.max=max;
	}*/
	public abstract void toGraph(Graph g, Context fTree); 

////////For RTDP BRTDP/////////////////
	  public void setprobWeightH(double probWeightH){
		  this.probWeightH=probWeightH;
	  }
	  public void setprobWeightL(double probWeightL){
		  this.probWeightL=probWeightL;
	  }
	  
	  public double getprobWeightH(){
		  return this.probWeightH;
	  }
	  public double getprobWeightL(){
		  return this.probWeightL;
	  }
	
	
}
