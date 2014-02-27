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
		
		String[] parameters = poly.getParameterFromPolynomial("p");
		
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
			String polytopeCacheKey = this.mdp.getPolytopeCacheKey(parameters);
			
			List<PolytopePoint> vertices = this.mdp.cachedPolytopes.get(polytopeCacheKey);
			
			TreeSet<String> alphas = new TreeSet<String>();
			
			Hashtable terms = new Hashtable();
			
			for (Object idAsObject : poly.getTerms().keySet()) {
				Integer id = (Integer) idAsObject;
				String label = this.mdp.context.getLabelProd(id);
				String param = "p" + label;
				
				for (int i = 0; i < vertices.size(); i++) {
					PolytopePoint vertex = vertices.get(i);
					
					Integer tempId = id * 100 + i;
					terms.put(tempId, vertex.getVertexDimension(param));
					
					String alphaName = "alpha_"+ i + "_" + label;
					
					mdp.context.putLabelsProdId(alphaName, tempId);
					
					alphas.add("p" + alphaName);
				}
			}
			
			if (alphas.size() > 0) {
			
				ArrayList constraints = new ArrayList();

				ArrayList constraint = null;
				
				for (String alpha : alphas) {
					constraint = new ArrayList();
					constraint.add(alpha);
					constraint.add("<");
					constraint.add("=");
					constraint.add(new BigInteger("1"));

					constraints.add(constraint);

					constraint = new ArrayList();
					constraint.add(alpha);
					constraint.add(">");
					constraint.add("=");
					constraint.add(new BigInteger("0"));

					constraints.add(constraint);
				}
				
				String cacheKey = "";
				
				constraint = new ArrayList();
				
				for (String alpha : alphas) {
					constraint.add("+");
					constraint.add(alpha);
					
					cacheKey += (alpha + ".");
				}
				
				constraint.remove(0);
				
				constraint.add("=");
				constraint.add(new BigInteger("1"));

				constraints.add(constraint);
				
				mdp.constraintsPerPoly.put(cacheKey, constraints);
			}
			
			return new Polynomial(poly.getC(), terms, this.mdp.context);
		}
		
		return poly;
	}
}
