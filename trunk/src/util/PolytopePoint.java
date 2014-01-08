package util;

import java.util.HashMap;
import java.util.Set;

public class PolytopePoint {
	public PolytopePoint() {
		this(new HashMap<String, Double>());
	}
	
	public PolytopePoint(HashMap<String, Double> vertex) {
		this.internalVertex = vertex;
	}
	
	private HashMap<String, Double> internalVertex = null;
	
	public double getVertexDimension(String parameter) {
		return this.internalVertex.get(parameter);
	}
	
	public void setVertexDimension(String parameter, double value) {
		this.internalVertex.put(parameter, value);
	}
	
	public HashMap<String, Double> asHashMap() {
		return new HashMap<String, Double>(this.internalVertex);
	}
	
	public Set<String> keySet() {
		return this.internalVertex.keySet();
	}
	
	public boolean containsParameter(String parameter) {
		return this.internalVertex.containsKey(parameter);
	}
}
