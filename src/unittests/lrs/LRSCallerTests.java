package unittests.lrs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lrs.LRSCaller;
import lrs.LinearConstraintExpression;
import lrs.RationalNumber;

import org.junit.Test;

import util.PolytopePoint;

public class LRSCallerTests {

	final double DELTA = 0.0;
	
	@Test
	public void testCallLRSToGetVertex() {
		String[] variables = new String[] { "p1", "p2", "p3" };
		
		ArrayList<RationalNumber> weights = null;
		
		LinearConstraintExpression[] expressions = new LinearConstraintExpression[6];		
		
		weights = new ArrayList<RationalNumber>();
		weights.add(RationalNumber.ONE);
		weights.add(RationalNumber.ZERO);
		weights.add(RationalNumber.ZERO);
		
		expressions[0] = new LinearConstraintExpression(RationalNumber.ONE, weights, variables);
		
		weights = new ArrayList<RationalNumber>();
		weights.add(RationalNumber.ZERO);
		weights.add(RationalNumber.ONE);
		weights.add(RationalNumber.ZERO);
		
		expressions[1] = new LinearConstraintExpression(RationalNumber.ONE, weights, variables);
		
		weights = new ArrayList<RationalNumber>();
		weights.add(RationalNumber.ZERO);
		weights.add(RationalNumber.ZERO);
		weights.add(RationalNumber.ONE);
		
		expressions[2] = new LinearConstraintExpression(RationalNumber.ONE, weights, variables);
		
		weights = new ArrayList<RationalNumber>();
		weights.add(RationalNumber.MINUS_ONE);
		weights.add(RationalNumber.ZERO);
		weights.add(RationalNumber.ZERO);
		
		expressions[3] = new LinearConstraintExpression(RationalNumber.ONE, weights, variables);
		
		weights = new ArrayList<RationalNumber>();
		weights.add(RationalNumber.ZERO);
		weights.add(RationalNumber.MINUS_ONE);
		weights.add(RationalNumber.ZERO);
		
		expressions[4] = new LinearConstraintExpression(RationalNumber.ONE, weights, variables);
		
		weights = new ArrayList<RationalNumber>();
		weights.add(RationalNumber.ZERO);
		weights.add(RationalNumber.ZERO);
		weights.add(RationalNumber.MINUS_ONE);
		
		expressions[5] = new LinearConstraintExpression(RationalNumber.ONE, weights, variables);
		
		List<PolytopePoint> result = LRSCaller.callLRSToGetVertex(expressions, variables);
		
		//The number of vertices must be eight
		assertEquals(8, result.size());
		
		//point (1, 1, 1)
		compareVertex(result.get(0), new double[] { 1.0, 1.0, 1.0 }, variables);
		
		//point (-1, 1, 1)
		compareVertex(result.get(1), new double[] { -1.0, 1.0, 1.0 }, variables);
		
		//point (1, -1, 1)
		compareVertex(result.get(2), new double[] { 1.0, -1.0, 1.0 }, variables);
		
		//point (-1, -1, 1)
		compareVertex(result.get(3), new double[] { -1.0, -1.0, 1.0 }, variables);
		
		//point (1, 1, -1)
		compareVertex(result.get(4), new double[] { 1.0, 1.0, -1.0 }, variables);
		
		//point (-1, 1, -1)
		compareVertex(result.get(5), new double[] { -1.0, 1.0, -1.0 }, variables);
		
		//point (1, -1, -1)
		compareVertex(result.get(6), new double[] { 1.0, -1.0, -1.0 }, variables);
		
		//point (-1, -1, -1)
		compareVertex(result.get(7), new double[] { -1.0, -1.0, -1.0 }, variables);
	}
	
	private void compareVertex(PolytopePoint result, double[] vertex, String[] variables) {
		for (int i = 0; i < variables.length; i++)
			assertEquals(vertex[i], result.getVertexDimension(variables[i]), DELTA);	
	}
}