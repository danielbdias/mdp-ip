package mdp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.TreeSet;

import util.PolytopePoint;
import add.Polynomial;

public class ReplaceByAlphaPolyTransform implements PolynomialTransform {
	
	public ReplaceByAlphaPolyTransform(MDP mdp) {
		super();
		
		this.mdp = mdp;
	}

	private MDP mdp = null;
	
	@Override
	public Polynomial transform(Polynomial poly) {
		//note: this does not work in sysadmin domain
		
		String[] parameters = this.mdp.getParameterFromPolynomial(poly);
		
		boolean changePoly = (parameters.length > 0);
		
		if (changePoly) {
			for (Object idAsObject : poly.getTerms().keySet()) {
				Integer id = (Integer) idAsObject;
				String label = this.mdp.context.getLabelProd(id);
				
				if (label.startsWith("alpha")) {
					changePoly = false;
					break;
				}
			}
		}
		
		if (changePoly) {
			if (parameters.length != 1) {
				System.err.println("transform only works for problems with one parameter per state variable !");
				System.exit(-1);
			}
			
			String polytopeCacheKey = this.mdp.getPolytopeCacheKey(parameters);
			
			List<PolytopePoint> vertices = this.mdp.cachedPolytopes.get(polytopeCacheKey);
			
			Hashtable terms = new Hashtable();
			
			for (Object idAsObject : poly.getTerms().keySet()) {
				Integer id = (Integer) idAsObject;
				String label = this.mdp.context.getLabelProd(id);
				String param = "p" + label;
				
				List<String> paramAlphas = mdp.alphasPerPolytope.get(param);
				
				Double oldWeight = (Double) poly.getTerms().get(id);
				
				for (int i = 0; i < vertices.size(); i++) {
					PolytopePoint vertex = vertices.get(i);
					String alphaName = paramAlphas.get(i);
					
					Integer tempId = mdp.context.getIdLabelProd(alphaName);
					terms.put(tempId, oldWeight * vertex.getVertexDimension(param));
				}
			}
			
			Polynomial newPoly = new Polynomial(poly.getC(), terms, this.mdp.context);
			
//			System.out.println(poly);
//			System.out.println(newPoly);
			
			return newPoly;
		}
		
		return poly;
	}
}
