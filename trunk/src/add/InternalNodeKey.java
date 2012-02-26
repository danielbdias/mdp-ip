package add;

import graph.Graph;

public abstract class InternalNodeKey extends NodeKey {
	 protected Integer var;
	 public abstract Object getHigh();
	 public abstract Object getLower();
	 public Integer getVar(){
	    	return this.var;
	    }
}
