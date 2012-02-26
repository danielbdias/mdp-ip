package logic.add;

import graph.Graph;

import java.util.*;

public class ADDBNode extends ADDNode {
    
    public boolean _bVal;
    
    public ADDBNode(int lid, boolean bval) {
		_nLocalID = lid;
		_bVal     = bval;
    }
    
    public void toGraph(Object context, Graph g) {
		g.addNodeLabel("#" + _nLocalID, ""+_bVal);
		g.addNodeColor("#" + _nLocalID, "lightsalmon"); // red, darkred
		g.addNodeShape("#" + _nLocalID, "square");
		g.addNodeStyle("#" + _nLocalID, "filled");

    }

    public String toString(Object context, int depth) {
	    return "[ #" + _nLocalID + " <" + ((_bVal) ? "T" : "F" ) + "> ] ";
    }
}
