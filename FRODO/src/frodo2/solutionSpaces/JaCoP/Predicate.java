/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2020  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

FRODO is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FRODO is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


How to contact the authors: 
<https://frodo-ai.tech>
*/

package frodo2.solutionSpaces.JaCoP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.jacop.core.StoreCloneable;
import org.jacop.constraints.AbsXeqYCloneable;
import org.jacop.constraints.AndCloneable;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.constraints.DecomposedConstraint;
import org.jacop.constraints.DecomposedConstraintCloneableInterface;
import org.jacop.constraints.DistanceCloneable;
import org.jacop.constraints.EqCloneable;
import org.jacop.constraints.IfThenElseCloneable;
import org.jacop.constraints.MaxCloneable;
import org.jacop.constraints.MinCloneable;
import org.jacop.constraints.NotCloneable;
import org.jacop.constraints.OrCloneable;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.XdivYeqZCloneable;
import org.jacop.constraints.XeqCCloneable;
import org.jacop.constraints.XeqYCloneable;
import org.jacop.constraints.XexpYeqZCloneable;
import org.jacop.constraints.XgtCCloneable;
import org.jacop.constraints.XgtYCloneable;
import org.jacop.constraints.XgteqCCloneable;
import org.jacop.constraints.XgteqYCloneable;
import org.jacop.constraints.XltCCloneable;
import org.jacop.constraints.XltYCloneable;
import org.jacop.constraints.XlteqCCloneable;
import org.jacop.constraints.XlteqYCloneable;
import org.jacop.constraints.XmodYeqZCloneable;
import org.jacop.constraints.XmulCeqZCloneable;
import org.jacop.constraints.XmulYeqCCloneable;
import org.jacop.constraints.XmulYeqZCloneable;
import org.jacop.constraints.XneqCCloneable;
import org.jacop.constraints.XneqYCloneable;
import org.jacop.constraints.XplusCeqZCloneable;
import org.jacop.constraints.XplusClteqZCloneable;
import org.jacop.constraints.XplusYeqCCloneable;
import org.jacop.constraints.XplusYeqZCloneable;
import org.jacop.constraints.XplusYlteqZCloneable;
import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.IntervalDomain;
import org.jacop.core.Store;
import org.jacop.core.Var;

/**
 * Predicate wrapper that translates a predicate expression from XCSP into constraints
 * available in JaCoP. It accepts only functional representation. Possibly, 
 * some auxiliary variables will be created.
 * 
 * @author Radoslaw Szymanek and Krzysztof Kuchcinski for the initial version developed for JaCoP 2.4; 
 * @author Arnaud Jutzeler and Thomas Leaute for the modified version ported to FRODO and JaCoP 3. 
 */

public class Predicate extends DecomposedConstraint<Constraint> implements ConstraintCloneableInterface<Predicate>, DecomposedConstraintCloneableInterface {

	/** The XCSP parameters of the constraint */
	String constraintParameters;

	/** Whether to print out debug information */
	final boolean debug = false;

	/** The functional representation of the predicate */
	String description;

	/** The constraint store */
	StoreCloneable store;

	/** The XCSP parameters of the predicate */
	String predicateParameters;

	/** The auxiliary constraints created */
	ArrayList<Constraint> decompositionConstraints;
	
	/** The auxiliary variables created */
	ArrayList<Var> auxilaryVariables;
	
	/** The utility variable (if this is a soft constraint) */
	IntVarCloneable utilVar;
	
	/**
	 * It creates/imposes constraints into the store as soon as the Predicate
	 * constraint is being imposed.
	 * @param constraintParameters parameters to the constraint.
	 * @param predicateParameters parameters specified within a predicate definition.
	 * @param description description of the constraint specified as the predicate.
	 * @param store the constraint store in which context the constraints are being created/imposed.
	 */
	public Predicate(String constraintParameters, String predicateParameters, String description, StoreCloneable store) {

		this.store = store;

		this.constraintParameters = constraintParameters;
		this.predicateParameters = predicateParameters;
		this.description = description;

	}

	/** @see java.lang.Object#toString() */
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder ("Predicate");
		
		builder.append("\n\t constraintParameters = ").append(this.constraintParameters);
		builder.append("\n\t predicateParameters = ").append(this.predicateParameters);
		builder.append("\n\t description = ").append(this.description);
		
		return builder.toString();
	}

	/** @see org.jacop.constraints.DecomposedConstraint#decompose(org.jacop.core.Store) */
	@Override
	public ArrayList<Constraint> decompose(Store store) 
			throws FailException {

		if (decompositionConstraints != null)
			return decompositionConstraints;
		
		decompositionConstraints = new ArrayList<Constraint>();
		auxilaryVariables = new ArrayList<Var>();
		
		StringTokenizer tokenizer = new StringTokenizer(predicateParameters, " ");
		StringTokenizer tokenizerConstraint = new StringTokenizer(
				constraintParameters, " ");

		HashMap<String, Object> variableMaping = new HashMap<String, Object>();

		while (tokenizer.hasMoreTokens()) {

			String nextToken = tokenizer.nextToken();

			if (nextToken.equals("int"))
				continue;
			
			String name = tokenizerConstraint.nextToken();
			
			Var temp = store.findVariable(name);
			
			if (temp == null) {
				try {
					variableMaping.put(nextToken, Integer.valueOf(name));
				} catch (NumberFormatException e) {
					throw new NumberFormatException ("Unexpected parameter `" + name + 
							"' in predicate-based constraint is neither a valid constant nor a matching variable in scope");
				}
			} else
				variableMaping.put(nextToken, temp);

		}

		if (debug)
			System.out.println(variableMaping);

		// Remove all spaces, line terminators and tabs
		description = description.replace(" ", "").replace("\n", "").replace("\t", "");
		
		tokenizer = new StringTokenizer(description, "(,)");

		String nextToken = tokenizer.nextToken();

		Object token = parse(nextToken, tokenizer, (StoreCloneable) store, variableMaping);
		assert token != null;

		if (debug)
			System.out.println(token);

		if (token instanceof Constraint) // hard constraint
			decompositionConstraints.add((Constraint) token);
		
		else if (token instanceof IntVarCloneable) // soft constraint
			this.utilVar = (IntVarCloneable) token;
		
		else if (token instanceof Integer) // constant soft constraint
			this.utilVar = new IntVarCloneable (this.store, (Integer) token, (Integer) token);
		
		else if ("true".equals(token)) { }
		
		else if ("false".equals(token)) 
			this.store.impose(new XeqCCloneable(new IntVarCloneable(this.store, 0, 0), 1));
		
		else 
			System.err.println("Unrecognized token: " + token);

		return decompositionConstraints;
	}

	
    /**
     * It allows to obtain the constraint specified by the predicate
     * without imposing it.
     *
     * @param store the constraint store in which context the constraint is being created.
     * @return the constraint represented by this predicate constraint (expression).
	 * @throws FailException thrown if a variable with an empty domain was encountered
     */

	public PrimitiveConstraint getConstraint(StoreCloneable store) 
		throws FailException {

		StringTokenizer tokenizer = new StringTokenizer(predicateParameters, " ");

		StringTokenizer tokenizerConstraint = new StringTokenizer(constraintParameters, " ");

		HashMap<String, Object> variableMaping = new HashMap<String, Object>();

		while (tokenizer.hasMoreTokens()) {

			String nextToken = tokenizer.nextToken();

			if (nextToken.equals("int"))
				continue;

			String name = tokenizerConstraint.nextToken();

			Var temp = store.findVariable(name);
			if (temp == null)
				variableMaping.put(nextToken, Integer.valueOf(name));
			else
				variableMaping.put(nextToken, temp);

		}

		if (debug)
			System.out.println(variableMaping);

		description = description.replace(" ", "");

		tokenizer = new StringTokenizer(description, "(,)");

		String nextToken = tokenizer.nextToken();

		Object token = parse(nextToken, tokenizer, store, variableMaping);

		if (debug)
			System.out.println(token);

		return (PrimitiveConstraint) token; /// @bug Can't the token also be an IntVar or an Integer?...

	}

	/**
	 * @param token 			the current token
	 * @param tokenizer 		the tokenizer
	 * @param store 			the constraint store
	 * @param variableMaping 	a mapping from name to variable or constant
	 * @return a constraint
	 * @throws FailException 	thrown if one of the encountered variables has an empty domain
	 */
	private Object parse(String token, StringTokenizer tokenizer, StoreCloneable store, HashMap<String, Object> variableMaping) 
			throws FailException {
		
		try {
			return Integer.valueOf(token);
		} catch (Exception ex) {
			// if not an integer then just go on.
		}

        if (variableMaping.get(token) != null)
			return variableMaping.get(token);
		else {

			if (token.equals("abs")) {
				
				String nextToken = tokenizer.nextToken();
				
				if (nextToken.equals("sub")) { // |o1 - o2|

					Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
					Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
					
					// Convert o1 and o2 to IntVars
					IntVarCloneable v1 = null, v2 = null;
					if (o1 instanceof Integer) {
						if (o2 instanceof Integer)
							return Math.abs((Integer) o1 - (Integer) o2);
						Integer i1 = (Integer) o1;
						v2 = (IntVarCloneable) o2;
						if (i1 == 0) { // actually |v2|
							
							if (v2.dom().isEmpty()) 
								throw Store.failException;
							
							if (v2.min() >= 0) 
								return v2;
							else {
								IntVarCloneable auxilary = (v2.max() <= 0 ? new IntVarCloneable(store, -v2.max(), -v2.min())
										: new IntVarCloneable(store, 0, Math.max(Math.abs(v2.min()), Math.abs(v2.max()))));
								auxilaryVariables.add(auxilary);
								decompositionConstraints.add(new AbsXeqYCloneable(v2, auxilary));
								return auxilary;
							}
						}
						v1 = new IntVarCloneable(store, i1, i1);
					} else if (o2 instanceof Integer) {
						v1 = (IntVarCloneable) o1;
						Integer i2 = (Integer) o2;
						if (i2 == 0) { // actually |v1|

							if (v1.dom().isEmpty()) 
								throw Store.failException;
							
							if (v1.min() >= 0) 
								return v1;
							else {
								IntVarCloneable auxilary = (v1.max() <= 0 ? new IntVarCloneable(store, -v1.max(), -v1.min())
										: new IntVarCloneable(store, 0, Math.max(Math.abs(v1.min()), Math.abs(v1.max()))));
								auxilaryVariables.add(auxilary);
								decompositionConstraints.add(new AbsXeqYCloneable(v1, auxilary));
								return auxilary;
							}
						}
						v2 = new IntVarCloneable(store, i2, i2);
					} else {
						v1 = (IntVarCloneable) o1;
						v2 = (IntVarCloneable) o2;
					}
					
					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					int auxMin = Math.max(0, Math.max(v1.min() - v2.max(), v2.min() - v1.max()));
					IntVarCloneable aux = new IntVarCloneable(store, auxMin,
							Math.max(Math.abs(v1.min() - v2.max()), Math.abs(v1.max() - v2.min())));
					auxilaryVariables.add(aux);
					decompositionConstraints.add(new DistanceCloneable(v1, v2, aux));
					return aux;

				} else {

					Object o1 = parse(nextToken, tokenizer, store, variableMaping);

					if (o1 instanceof Integer) 
						return Integer.valueOf(Math.abs((Integer) o1));

					else if (o1 instanceof IntVarCloneable) {
						IntVarCloneable v1 = (IntVarCloneable) o1;

						if (v1.dom().isEmpty()) 
							throw Store.failException;
						
						if (v1.min() >= 0)
							return v1;
						else {
							IntVarCloneable auxilary = (v1.max() <= 0 ? new IntVarCloneable(store, -v1.max(), -v1.min())
									: new IntVarCloneable(store, 0, Math.max(Math.abs(v1.min()), Math.abs(v1.max()))));
							auxilaryVariables.add(auxilary);
							decompositionConstraints.add(new AbsXeqYCloneable(v1, auxilary));
							return auxilary;
						}
					}

					System.err.println("Failed to parse abs(" + o1 + ")");
					return null;
				}
			}
			
			if (token.equals("neg")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof Integer) 
					return Integer.valueOf(- (Integer) o1);

				else if (o1 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					IntVarCloneable auxilary = new IntVarCloneable(store, -v1.max(), -v1.min());
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XmulCeqZCloneable(v1, -1, auxilary));
					return auxilary;
				}
				
				System.err.println("Failed to parse neg(" + o1 + ")");
				return null;
			}

			if (token.equals("sub")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					IntVarCloneable auxilary = new IntVarCloneable(store, v1.min() - v2.max(), v1.max() - v2.min());
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XplusYeqZCloneable(v2, auxilary, v1));
					return auxilary;
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;
					if (c2 == 0) 
						return v1;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					IntVarCloneable auxilary = new IntVarCloneable(store, v1.min() - c2, v1.max() - c2);
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XplusCeqZCloneable(auxilary, c2, v1));
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					Integer c1 = (Integer) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					IntVarCloneable auxilary = new IntVarCloneable(store, c1 - v2.max(), c1 - v2.min());
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XplusYeqCCloneable(v2, auxilary, c1));
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Integer.valueOf((Integer)o1 - (Integer)o2);

				System.err.println("Failed to parse sub(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("add")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					IntVarCloneable auxilary = new IntVarCloneable(store, v1.min() + v2.min(), v1.max() + v2.max());
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XplusYeqZCloneable(v1, v2, auxilary));
					return auxilary;
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;
					if (c2 == 0) 
						return v1;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					IntVarCloneable auxilary = new IntVarCloneable(store, v1.min() + c2, v1.max() + c2);
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XplusCeqZCloneable(v1, c2, auxilary));
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					Integer c1 = (Integer) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;
					if (c1 == 0)
						return v2;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					IntVarCloneable auxilary = new IntVarCloneable(store, c1 + v2.min(), c1 + v2.max());
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XplusCeqZCloneable(v2, c1, auxilary));
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Integer.valueOf((Integer)o1 + (Integer)o2);

				System.err.println("Failed to parse add(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("mul")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;
					
					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					// Compute the bounds
					int min = v1.min() * v2.min();
					int max = min;
					for (int tmp : Arrays.asList(v1.min() * v2.max(), v1.max() * v2.min(), v1.max() * v2.max())) {
						min = Math.min(min, tmp);
						max = Math.max(max, tmp);
					}
					
					IntVarCloneable auxilary = new IntVarCloneable(store, min, max);
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XmulYeqZCloneable(v1, v2, auxilary));
					return auxilary;
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;
					
					if (c2 == 1) 
						return v1;
					
					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					// Compute the bounds
					int min = v1.min() * c2;
					int max = min;
					int tmp = v1.max() * c2;
					min = Math.min(min, tmp);
					max = Math.max(max, tmp);
					
					IntVarCloneable auxilary = new IntVarCloneable(store, min, max);
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XmulCeqZCloneable(v1, c2, auxilary));
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					Integer c1 = (Integer) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;
					
					if (c1 == 1) 
						return v2;
					
					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					// Compute the bounds
					int min = v2.min() * c1;
					int max = min;
					int tmp = v2.max() * c1;
					min = Math.min(min, tmp);
					max = Math.max(max, tmp);
					
					IntVarCloneable auxilary = new IntVarCloneable(store, min, max);
					auxilaryVariables.add(auxilary);
					decompositionConstraints.add(new XmulCeqZCloneable(v2, c1, auxilary));
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Integer.valueOf((Integer)o1 * (Integer)o2);

				System.err.println("Failed to parse mul(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("div")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					v2.domain.subtract(0); // v2 != 0
					int min2 = (v2.min() == 0 ? 1 : v2.min());
					int max2 = (v2.max() == 0 ? -1 : v2.max());
					
					// Compute the bounds for the auxiliary variable aux = v1 / v2
					int min = v1.min() / min2;
					int max = min;
					for (int tmp : Arrays.asList(v1.min() / max2, v1.max() / min2, v1.max() / max2)) {
						min = Math.min(min, tmp);
						max = Math.max(max, tmp);
					}
					IntVarCloneable aux = new IntVarCloneable(store, min, max);

					auxilaryVariables.add(aux);
					decompositionConstraints.add(new XdivYeqZCloneable(v1, v2, aux));
					
					return aux;
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;
					
					if (c2 == 1) 
						return v1;
					
					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					// Compute the bounds for the auxiliary variable aux = v1 / c2
					int min = v1.min() / c2;
					int max = min;
					int tmp = v1.max() / c2;
					min = Math.min(min, tmp);
					max = Math.max(max, tmp);
					IntVarCloneable aux = new IntVarCloneable(store, min, max);

					auxilaryVariables.add(aux);
					decompositionConstraints.add(new XdivYeqZCloneable(v1, new IntVarCloneable(store, c2, c2), aux));
					
					return aux;
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					Integer c1 = (Integer) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					v2.domain.subtract(0); // v2 != 0
					int min2 = (v2.min() == 0 ? 1 : v2.min());
					int max2 = (v2.max() == 0 ? -1 : v2.max());
					
					// Compute the bounds for the auxiliary variable aux = c1 / v2
					int min = c1 / min2;
					int max = min;
					int tmp = c1 / max2;
					min = Math.min(min, tmp);
					max = Math.max(max, tmp);
					IntVarCloneable aux = new IntVarCloneable(store, min, max);

					auxilaryVariables.add(aux);
					decompositionConstraints.add(new XdivYeqZCloneable(new IntVarCloneable(store, c1, c1), v2, aux));
					
					return aux;
				} else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Integer.valueOf((Integer)o1 / (Integer)o2);

				System.err.println("Failed to parse div(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("mod")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					v2.domain.subtract(0); // v2 != 0
					int min2 = (v2.min() == 0 ? 1 : v2.min());
					int max2 = (v2.max() == 0 ? -1 : v2.max());
					
					// Check if we can guarantee that v1 mod v2 = v1
					if (v1.min() >= 0 && v1.max() < min2) 
						return v1;
					
					IntVarCloneable T3; // t3 = |v2|
					if (min2 > 0) 
						T3 = v2;
					else {
						T3 = (v2.max() <= 0 ? new IntVarCloneable(store, -v2.max(), -v2.min())
								: new IntVarCloneable(store, 0, Math.max(Math.abs(min2), Math.abs(max2))));
						auxilaryVariables.add(T3);
						decompositionConstraints.add(new AbsXeqYCloneable(v2, T3));
					}
					
					IntVarCloneable auxilary = new IntVarCloneable(store, 0, T3.max() - 1); // a < t3
					IntVarCloneable T2 = new IntVarCloneable(store, v1.min() - auxilary.max(), v1.max()); // t2 = v1 - a
					
					// Compute the bounds for t1 = t2 / v2
					int min = T2.min() / min2;
					int max = min;
					for (int tmp : Arrays.asList(T2.min() / max2, T2.max() / min2, T2.max() / max2)) {
						min = Math.min(min, tmp);
						max = Math.max(max, tmp);
					}
					
					IntVarCloneable T1 = new IntVarCloneable(store, min, max);
					
					auxilaryVariables.add(T1);
					auxilaryVariables.add(T2);
					auxilaryVariables.add(auxilary);
					
					decompositionConstraints.add(new XmulYeqZCloneable(T1, v2, T2));
					decompositionConstraints.add(new XplusYeqZCloneable(T2, auxilary, v1));
					decompositionConstraints.add(new XltYCloneable(auxilary, T3));

					return auxilary;
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;
					
					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					// Check if we can guarantee that v1 mod c2 = v1
					if (v1.min() >= 0 && v1.max() < c2) 
						return v1;

					IntVarCloneable auxilary = new IntVarCloneable(store, 0, Math.abs(c2) - 1); // a < |c2|
					IntVarCloneable T2 = new IntVarCloneable(store, v1.min() - auxilary.max(), v1.max()); // t2 = v1 - a
					
					// Compute the bounds for t1 = t2 / c2
					int min = T2.min() / c2;
					int max = min;
					int tmp = T2.max() / c2;
					min = Math.min(min, tmp);
					max = Math.max(max, tmp);
					
					IntVarCloneable T1 = new IntVarCloneable(store, min, max);
					
					auxilaryVariables.add(T1);
					auxilaryVariables.add(T2);
					auxilaryVariables.add(auxilary);

					decompositionConstraints.add(new XmulCeqZCloneable(T1, c2, T2));
					decompositionConstraints.add(new XplusYeqZCloneable(T2, auxilary, v1));
					decompositionConstraints.add(new XltCCloneable(auxilary, Math.abs(c2)));

					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					Integer c1 = (Integer) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					v2.domain.subtract(0); // v2 != 0
					int min2 = (v2.min() == 0 ? 1 : v2.min());
					int max2 = (v2.max() == 0 ? -1 : v2.max());
					
					// Check if we can guarantee that c1 mod v2 = c1
					if (c1 >= 0 && c1 < min2) 
						return c1;

					IntVarCloneable T3; // t3 = |v2|
					if (min2 > 0) 
						T3 = v2;
					else {
						T3 = (v2.max() <= 0 ? new IntVarCloneable(store, -v2.max(), -v2.min())
								: new IntVarCloneable(store, 0, Math.max(Math.abs(min2), Math.abs(max2))));
						auxilaryVariables.add(T3);
						decompositionConstraints.add(new AbsXeqYCloneable(v2, T3));
					}
					
					IntVarCloneable auxilary = new IntVarCloneable(store, 0, T3.max() - 1); // a < t3
					IntVarCloneable T2 = new IntVarCloneable(store, c1 - auxilary.max(), c1); // t2 = c1 - a
					
					// Compute the bounds for t1 = t2 / v2
					int min = T2.min() / min2;
					int max = min;
					for (int tmp : Arrays.asList(T2.min() / max2, T2.max() / min2, T2.max() / max2)) {
						min = Math.min(min, tmp);
						max = Math.max(max, tmp);
					}
					
					IntVarCloneable T1 = new IntVarCloneable(store, min, max);
					
					auxilaryVariables.add(T1);
					auxilaryVariables.add(T2);
					auxilaryVariables.add(auxilary);
					
					decompositionConstraints.add(new XmulYeqZCloneable(T1, v2, T2));
					decompositionConstraints.add(new XplusYeqCCloneable(T2, auxilary, c1));
					decompositionConstraints.add(new XltYCloneable(auxilary, T3));

					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Integer.valueOf((Integer)o1 % (Integer)o2);

				System.err.println("Failed to parse mod(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("pow")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof Integer) {
					String val = String.valueOf(o1);
					if (variableMaping.get(val) == null)
						variableMaping.put(val, new IntVarCloneable(store, (Integer) o1, (Integer) o1));
					o1 = variableMaping.get(val);
				}

				if (o2 instanceof Integer) {
					String val = String.valueOf(o2);
					if (variableMaping.get(val) == null)
						variableMaping.put(val, new IntVarCloneable(store, (Integer) o2, (Integer) o2));
					o2 = variableMaping.get(val);
				}

				IntVarCloneable v1 = (IntVarCloneable) o1;
				IntVarCloneable v2 = (IntVarCloneable) o2;
				IntVarCloneable auxilary = null;
				
				if (v1.dom().isEmpty()) 
					throw Store.failException;
				
				// If the variables can take negative values, we need to add specific variables and constraints as JaCoP's native XexpYeqZ constraint
				// does not handle negative values.
				if(v1.min() < 0){
					
					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					IntVarCloneable v3 = new IntVarCloneable(store, 2, 2); // v3 = 2
					auxilaryVariables.add(v3);
					
					IntVarCloneable reminder = new IntVarCloneable(store, 0, 1); // reminder = v2 mod 2
					auxilaryVariables.add(reminder);
					
					IntVarCloneable abs1 = new IntVarCloneable(store, 0, Math.max(Math.abs(v1.min()), Math.abs(v1.max()))); // abs1 = |v1|
					auxilaryVariables.add(abs1);
					
					IntVarCloneable posPow = new IntVarCloneable(store, 0,
							(int) Math.pow(abs1.max(), Math.max(Math.abs(v2.min()), Math.abs(v2.max())))); // posPow = abs1 ^ |v2|
					auxilaryVariables.add(posPow);
					
					auxilary = new IntVarCloneable(store, -posPow.max(), posPow.max()); // aux = +/- posPow
					auxilaryVariables.add(auxilary);
					
					IntVarCloneable negPow = new IntVarCloneable(store, -posPow.max(), 0); // negPow = - posPow
					auxilaryVariables.add(negPow);
					
					decompositionConstraints.add(new AbsXeqYCloneable(v1, abs1));
					
					decompositionConstraints.add(new XmodYeqZCloneable(v2, v3, reminder));
					
					decompositionConstraints.add(new XmulCeqZCloneable(posPow, -1, negPow));
					
					if(v2.min() < 0){
						
						IntVarCloneable abs2 = new IntVarCloneable(store, 0, Math.max(Math.abs(v2.min()), Math.abs(v2.max()))); // abs2 = |v2|
						auxilaryVariables.add(abs2);
						
						IntVarCloneable result2 = new IntVarCloneable(store, -posPow.max(), posPow.max()); // result2 = posPow or result2  = negPow = - posPow
						auxilaryVariables.add(result2);
						
						decompositionConstraints.add(new AbsXeqYCloneable(v2, abs2));
						
						decompositionConstraints.add(new XexpYeqZCloneable(abs1, abs2, posPow));
						
						decompositionConstraints.add(new IfThenElseCloneable(
										new XltCCloneable(v1, 0), 
										new IfThenElseCloneable(
												new XeqCCloneable(reminder, 0),
												new XeqYCloneable(result2, posPow), 
												new XeqYCloneable(result2, negPow)),  
										new XeqYCloneable(result2, posPow)));
						
						decompositionConstraints.add(new IfThenElseCloneable(
								new XltCCloneable(v2, 0), 
								new IfThenElseCloneable(
										new XeqCCloneable(abs1, 1), 
										new XeqYCloneable(auxilary, result2), 
										new XeqCCloneable(auxilary, -1)), 
								new XeqYCloneable(auxilary, result2)));
						
					}else{
						
						decompositionConstraints.add(new XexpYeqZCloneable(abs1, v2, posPow));
						
						decompositionConstraints.add(new IfThenElseCloneable(
										new XltCCloneable(v1, 0), 
										new IfThenElseCloneable(
												new XeqCCloneable(reminder, 0), 
												new XeqYCloneable(auxilary, posPow), 
												new XeqYCloneable(auxilary, negPow)), 
										new XeqYCloneable(auxilary, posPow)));
					}
				}else{
					
					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					int maxAbs2 = Math.max(Math.abs(v2.min()), Math.abs(v2.max())); // abs2 = |v2|
					auxilary = new IntVarCloneable(store, 0, (int) Math.pow(v1.max(), maxAbs2));
					auxilaryVariables.add(auxilary);
					
					if(v2.min() < 0){
						
						IntVarCloneable abs2 = new IntVarCloneable(store, 0, maxAbs2);
						auxilaryVariables.add(abs2);
						
						IntVarCloneable result = new IntVarCloneable(store, 0, (int) Math.pow(v1.max(), abs2.max())); // result = v1 ^abs2
						auxilaryVariables.add(result);
						
						decompositionConstraints.add(new AbsXeqYCloneable(v2, abs2));
						
						decompositionConstraints.add(new XexpYeqZCloneable(v1, abs2, result));
						
						decompositionConstraints.add(new IfThenElseCloneable(
								new XltCCloneable(v2, 0), 
								new IfThenElseCloneable(
										new XeqCCloneable(v1, 1), 
										new XeqCCloneable(auxilary, 1), 
										new XeqCCloneable(auxilary, -1)),
								new XeqYCloneable(auxilary, result)));
						
					// the domains only contain positive values
					}else{

						decompositionConstraints.add(new XexpYeqZCloneable(v1, v2, auxilary));
					}
				}
				
				return auxilary;

			}
			
			if (token.equals("min")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.max() <= v2.min())
						return v1;
					else if (v2.min() <= v1.min()) 
						return v2;
					IntVarCloneable auxilary = new IntVarCloneable(store, Math.min(v1.min(), v2.min()), Math.min(v1.max(), v2.max()));
					auxilaryVariables.add(auxilary);
					
					IntVarCloneable[] listVars = {v1, v2};
					
					decompositionConstraints.add(new MinCloneable(listVars, auxilary));
					
					return auxilary;
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.max() <= c2) 
						return v1;
					else if (c2 <= v1.min()) 
						return c2;
					IntVarCloneable auxilary = new IntVarCloneable(store, Math.min(v1.min(), c2), Math.min(v1.max(), c2));
					auxilaryVariables.add(auxilary);
					
					IntVarCloneable[] listVars = {v1, new IntVarCloneable(store, c2, c2)};
					
					decompositionConstraints.add(new MinCloneable(listVars, auxilary));
							
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					Integer c1 = (Integer) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (c1 <= v2.min()) 
						return c1;
					else if (v2.min() <= c1) 
						return v2;
					IntVarCloneable auxilary = new IntVarCloneable(store, Math.min(c1, v2.min()), Math.min(c1, v2.max()));
					auxilaryVariables.add(auxilary);
					
					IntVarCloneable[] listVars = {v2, new IntVarCloneable(store, c1, c1)};
					
					decompositionConstraints.add(new MinCloneable(listVars, auxilary));
					
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Integer.valueOf(Math.min((Integer)o1, (Integer)o2));

				System.err.println("Failed to parse min(" + o1 + ", " + o2 + ")");
				return null;
			}
			
			if (token.equals("max")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.min() >= v2.max())
						return v1;
					else if (v2.min() >= v1.max()) 
						return v2;
					IntVarCloneable auxilary = new IntVarCloneable(store, Math.max(v1.min(), v2.min()), Math.max(v1.max(), v2.max()));
					auxilaryVariables.add(auxilary);
					
					IntVarCloneable[] listVars = {v1, v2};
					
					decompositionConstraints.add(new MaxCloneable(listVars, auxilary));
					
					return auxilary;
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.min() >= c2) 
						return v1;
					else if (c2 >= v1.max()) 
						return c2;
					IntVarCloneable auxilary = new IntVarCloneable(store, Math.max(v1.min(), c2), Math.max(v1.max(), c2));
					auxilaryVariables.add(auxilary);
					
					IntVarCloneable[] listVars = {v1, new IntVarCloneable(store, c2, c2)};
					
					decompositionConstraints.add(new MaxCloneable(listVars, auxilary));
							
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					Integer c1 = (Integer) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (c1 >= v2.max()) 
						return c1;
					else if (v2.min() >= c1) 
						return v2;
					IntVarCloneable auxilary = new IntVarCloneable(store, Math.max(c1, v2.min()), Math.max(c1, v2.max()));
					auxilaryVariables.add(auxilary);
					
					IntVarCloneable[] listVars = {v2, new IntVarCloneable(store, c1, c1)};
					
					decompositionConstraints.add(new MaxCloneable(listVars, auxilary));
					
					return auxilary;
				} else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Integer.valueOf(Math.max((Integer)o1, (Integer)o2));

				System.err.println("Failed to parse max(" + o1 + ", " + o2 + ")");
				return null;
			}
			
			if (token.equals("if")) {
				
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o3 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				
				if ("true".equals(o1)) 
					return o2;
				else if ("false".equals(o1)) 
					return o3;

				if (o1 instanceof PrimitiveConstraint && o2 instanceof IntVarCloneable && o3 instanceof IntVarCloneable) {
					IntVarCloneable v2 = (IntVarCloneable) o2;
					IntVarCloneable v3 = (IntVarCloneable) o3;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					IntervalDomain auxDom = new IntervalDomain(v2.min(), v2.max());
					auxDom.addDom(v3.domain);
					IntVarCloneable auxilary = new IntVarCloneable(store, auxDom);
					auxilaryVariables.add(auxilary);
					PrimitiveConstraint thenCons = new XeqYCloneable(auxilary, v2);
					PrimitiveConstraint elseCons = new XeqYCloneable(auxilary, v3);
					
					decompositionConstraints.add(new IfThenElseCloneable((PrimitiveConstraint) o1, 
							thenCons,
							elseCons));
					
					return auxilary;
				} else if (o1 instanceof PrimitiveConstraint && o2 instanceof IntVarCloneable && o3 instanceof Integer) {
					IntVarCloneable v2 = (IntVarCloneable) o2;
					Integer c3 = (Integer) o3;
					IntervalDomain auxDom = new IntervalDomain (c3, c3);
					auxDom.addDom(v2.domain);
					IntVarCloneable auxilary = new IntVarCloneable(store, auxDom);
					auxilaryVariables.add(auxilary);
					
					PrimitiveConstraint thenCons = new XeqYCloneable(auxilary, v2);
					PrimitiveConstraint elseCons = new XeqCCloneable(auxilary, c3);
					
					decompositionConstraints.add(new IfThenElseCloneable((PrimitiveConstraint) o1, 
							thenCons,
							elseCons));
					
					return auxilary;
				} else if (o1 instanceof PrimitiveConstraint && o2 instanceof Integer && o3 instanceof IntVarCloneable) {
					Integer c2 = (Integer) o2;
					IntVarCloneable v3 = (IntVarCloneable) o3;
					IntervalDomain auxDom = new IntervalDomain(c2, c2);
					auxDom.addDom(v3.domain);
					IntVarCloneable auxilary = new IntVarCloneable(store, auxDom);
					auxilaryVariables.add(auxilary);
					
					PrimitiveConstraint thenCons = new XeqCCloneable(auxilary, c2);
					PrimitiveConstraint elseCons = new XeqYCloneable(auxilary, v3);
					
					decompositionConstraints.add(new IfThenElseCloneable((PrimitiveConstraint) o1, 
							thenCons,
							elseCons));
					
					return auxilary;
				} else if (o1 instanceof PrimitiveConstraint && o2 instanceof Integer && o3 instanceof Integer) {
					Integer c2 = (Integer) o2;
					Integer c3 = (Integer) o3;
					IntervalDomain auxDom = new IntervalDomain (c2, c2);
					auxDom.addDom(new IntervalDomain (c3, c3));
					IntVarCloneable auxilary = new IntVarCloneable(store, auxDom);
					auxilaryVariables.add(auxilary);
					
					PrimitiveConstraint thenCons = new XeqCCloneable(auxilary, c2);
					PrimitiveConstraint elseCons = new XeqCCloneable(auxilary, c3);
					
					decompositionConstraints.add(new IfThenElseCloneable((PrimitiveConstraint) o1, 
							thenCons,
							elseCons));
					
					return auxilary;
				}
				
				System.err.println("Failed to parse:\n\t if \n\t " + o1 + "\n\t then \n\t " + o2 + " \n\t else \n\t " + o3);
				return null;
			}

			if (token.equals("eq")) {
				
				String nextToken = tokenizer.nextToken();

				if (nextToken.equals("abs")) { // | ... | = ...
					
					String nextNextToken = tokenizer.nextToken();

					if (nextNextToken.equals("sub")) { // |o1 - o2| = o3

						Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
						Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
						Object o3 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

						if (o1 instanceof Integer) {
							String val = String.valueOf(o1);
							if (variableMaping.get(val) == null)
								variableMaping.put(val, new IntVarCloneable(store, (Integer) o1, (Integer) o1));
							o1 = variableMaping.get(val);
						}

						if (o2 instanceof Integer) {
							String val = String.valueOf(o2);
							if (variableMaping.get(val) == null)
								variableMaping.put(val, new IntVarCloneable(store, (Integer) o2, (Integer) o2));
							o2 = variableMaping.get(val);
						}

						if (o3 instanceof Integer) {
							String val = String.valueOf(o3);
							if (variableMaping.get(val) == null)
								variableMaping.put(val, new IntVarCloneable(store, (Integer) o3, (Integer) o3));
							o3 = variableMaping.get(val);
						}

						return new DistanceCloneable((IntVarCloneable) o1, (IntVarCloneable) o2,
								(IntVarCloneable) o3);

					} else { // |o1| = o2

						Object o1 = parse(nextNextToken, tokenizer, store, variableMaping);
						
						Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

						if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
						
							return new AbsXeqYCloneable((IntVarCloneable) o1, (IntVarCloneable) o2);
						
						} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
							
							IntVarCloneable auxilary = new IntVarCloneable(store, (Integer) o2, (Integer) o2);
							auxilaryVariables.add(auxilary);
							
							return new AbsXeqYCloneable((IntVarCloneable) o1, auxilary);
							
						} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
							
							return new XeqCCloneable((IntVarCloneable) o2, Math.abs((Integer) o1));
							
						}
						
						System.err.println("Failed to parse eq(abs(" + o1 + "), " + o2 + ")");
						return null;
					}

				}

				if (nextToken.equals("add")) { // o1 + o2 = o3
					
					Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
					Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
					Object o3 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

					if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable && o3 instanceof IntVarCloneable) { // X + Y = Z
						return (new XplusYeqZCloneable((IntVarCloneable) o1, (IntVarCloneable) o2, (IntVarCloneable) o3));
					}

					if (o1 instanceof IntVarCloneable && o2 instanceof Integer && o3 instanceof IntVarCloneable) { // X + C = Z
						return (new XplusCeqZCloneable((IntVarCloneable) o1, (Integer) o2, (IntVarCloneable) o3));
					}

					if (o1 instanceof Integer && o2 instanceof IntVarCloneable && o3 instanceof IntVarCloneable) { // C + X = Z
						return (new XplusCeqZCloneable((IntVarCloneable) o2, (Integer) o1, (IntVarCloneable) o3));
					}

					if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable && o3 instanceof Integer) { // X + Y = C
						return (new XplusYeqCCloneable((IntVarCloneable) o1, (IntVarCloneable) o2, (Integer) o3));
					}

					if (o1 instanceof IntVarCloneable && o2 instanceof Integer && o3 instanceof Integer) { // X + C = C
						return (new XeqCCloneable((IntVarCloneable) o1, Integer.valueOf((Integer) o3 - (Integer) o2)));
					}

					if (o1 instanceof Integer && o2 instanceof IntVarCloneable && o3 instanceof Integer) { // C + X = C
						return (new XeqCCloneable((IntVarCloneable) o2, Integer.valueOf((Integer) o3 - (Integer) o1)));
					}

					if (o1 instanceof Integer && o2 instanceof Integer && o3 instanceof IntVarCloneable) { // C + C = X
						return (new XeqCCloneable((IntVarCloneable) o3, Integer.valueOf((Integer) o1 + (Integer) o2)));
					}

					System.err.println("Failed to parse eq(add(" + o1 + ", " + o2 + "), " + o3 + ")");
					return null;
				}

				if (nextToken.equals("mul")) {

					Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
					Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
					Object o3 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

					if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable && o3 instanceof IntVarCloneable) { // X * Y = Z
						return (new XmulYeqZCloneable((IntVarCloneable) o1, (IntVarCloneable) o2, (IntVarCloneable) o3));
					}

					if (o1 instanceof IntVarCloneable && o2 instanceof Integer && o3 instanceof IntVarCloneable) { // X * C = Z
						return (new XmulCeqZCloneable((IntVarCloneable) o1, (Integer) o2, (IntVarCloneable) o3));
					}

					if (o1 instanceof Integer && o2 instanceof IntVarCloneable && o3 instanceof IntVarCloneable) { // C * X = Z
						return (new XmulCeqZCloneable((IntVarCloneable) o2, (Integer) o1, (IntVarCloneable) o3));
					}

					if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable && o3 instanceof Integer) { // X * Y = C
						return (new XmulYeqCCloneable((IntVarCloneable) o1, (IntVarCloneable) o2, (Integer) o3));
					}

					if (o1 instanceof Integer && o2 instanceof Integer && o3 instanceof IntVarCloneable) { // C * C = X
						return (new XeqCCloneable((IntVarCloneable) o3, Integer.valueOf((Integer) o1 * (Integer) o2)));
					}

					System.err.println("Failed to parse eq(mul(" + o1 + ", " + o2 + "), " + o3 + ")");
					return null;
				}
				
				// o1 = o2

				// System.out.println("nextToken " + nextToken);

				Object o1 = parse(nextToken, tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				// System.out.println(o1);
				// System.out.println(o2);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1, v2 = (IntVarCloneable) o2;
					if (!v1.domain.isIntersecting(v2.domain))
						return "false";
					return new XeqYCloneable(v1, v2);
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;
					if (! v1.domain.contains(c2)) 
						return "false";
					return new XeqCCloneable(v1, c2);
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					IntVarCloneable v2 = (IntVarCloneable) o2;
					Integer c1 = (Integer) o1;
					if (! v2.domain.contains(c1)) 
						return "false";
					return new XeqCCloneable(v2, c1);
				} else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Boolean.toString(o1.equals(o2));
				
				System.err.println("Failed to parse eq(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("ne")) {

				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable)
					return new XneqYCloneable((IntVarCloneable) o1, (IntVarCloneable) o2);
				else if (o1 instanceof IntVarCloneable && o2 instanceof Integer)
					return new XneqCCloneable((IntVarCloneable) o1, (Integer) o2);
				else if (o1 instanceof Integer && o2 instanceof IntVarCloneable)
					return new XneqCCloneable((IntVarCloneable) o2, (Integer) o1);
				else if (o1 instanceof Integer && o2 instanceof Integer) 
					return Boolean.toString(! o1.equals(o2));

				System.err.println("Failed to parse ne(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("ge")) {
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1, v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.min() >= v2.max()) 
						return "true";
					else if (v1.max() < v2.min()) 
						return "false";
					return new XgteqYCloneable(v1, v2);
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.min() >= c2) 
						return "true";
					else if (v1.max() < c2) 
						return "false";
					return new XgteqCCloneable(v1, c2);
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					Integer c1 = (Integer) o1;
					IntVarCloneable v2 = (IntVarCloneable) o2;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (c1 >= v2.max()) 
						return "true";
					else if (c1 < v2.min()) 
						return "false";
					return new XlteqCCloneable(v2, c1);
				}

				System.err.println("Failed to parse ge(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("gt")) {
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1, v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.min() > v2.max()) 
						return "true";
					else if (v1.max() <= v2.min()) 
						return "false";
					return new XgtYCloneable(v1, v2);
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.min() > c2) 
						return "true";
					else if (v1.max() <= c2) 
						return "false";
					return new XgtCCloneable(v1, c2);
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					IntVarCloneable v2 = (IntVarCloneable) o2;
					Integer c1 = (Integer) o1;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (c1 > v2.max()) 
						return "true";
					else if (c1 <= v2.min()) 
						return "false";
					return new XltCCloneable(v2, c1);
				}

				System.err.println("Failed to parse gt(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("le")) {

				String nextToken = tokenizer.nextToken();

				if (nextToken.equals("add")) { // o1 + o2 <= o3

					Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
					Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
					Object o3 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

					if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable && o3 instanceof IntVarCloneable) {
						return (new XplusYlteqZCloneable((IntVarCloneable) o1, (IntVarCloneable) o2, (IntVarCloneable) o3));
					}

					if (o1 instanceof IntVarCloneable && o2 instanceof Integer && o3 instanceof IntVarCloneable) {
						return (new XplusClteqZCloneable((IntVarCloneable) o1, (Integer) o2, (IntVarCloneable) o3));
					}

					if (o1 instanceof Integer && o2 instanceof IntVarCloneable && o3 instanceof IntVarCloneable) {
						return (new XplusClteqZCloneable((IntVarCloneable) o2, (Integer) o1, (IntVarCloneable) o3));
					}

					if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable && o3 instanceof Integer) {

						String val = String.valueOf(o3);
						if (variableMaping.get(val) == null)
							variableMaping.put(val, new IntVarCloneable(store, (Integer) o3, (Integer) o3));
						o3 = variableMaping.get(val);

						return (new XplusYlteqZCloneable((IntVarCloneable) o1, (IntVarCloneable) o2, (IntVarCloneable) o3));
					}

					System.err.println("Failed to parse le(add(" + o1 + ", " + o2 + "), " + o3 + ")");
					return null;
				}
				
				// o1 <= o2

				Object o1 = parse(nextToken, tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1, v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.max() <= v2.min()) 
						return "true";
					else if (v1.min() > v2.max()) 
						return "false";
					return new XlteqYCloneable(v1, v2);
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.max() <= c2) 
						return "true";
					else if (v1.min() > c2) 
						return "false";
					return new XlteqCCloneable(v1, c2);
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					IntVarCloneable v2 = (IntVarCloneable) o2;
					Integer c1= (Integer) o1;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (c1 <= v2.min()) 
						return "true";
					else if (c1 > v2.max()) 
						return "false";
					return new XgteqCCloneable(v2, c1);
				}

				System.err.println("Failed to parse le(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("lt")) {
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);

				if (o1 instanceof IntVarCloneable && o2 instanceof IntVarCloneable) {
					IntVarCloneable v1 = (IntVarCloneable) o1, v2 = (IntVarCloneable) o2;

					if (v1.dom().isEmpty() || v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.max() < v2.min()) 
						return "true";
					else if (v1.min() >= v2.max()) 
						return "false";
					return new XltYCloneable(v1, v2);
				} else if (o1 instanceof IntVarCloneable && o2 instanceof Integer) {
					IntVarCloneable v1 = (IntVarCloneable) o1;
					Integer c2 = (Integer) o2;

					if (v1.dom().isEmpty()) 
						throw Store.failException;
					
					if (v1.max() < c2) 
						return "true";
					else if (v1.min() >= c2) 
						return "false";
					return new XltCCloneable(v1, c2);
				} else if (o1 instanceof Integer && o2 instanceof IntVarCloneable) {
					IntVarCloneable v2 = (IntVarCloneable) o2;
					Integer c1= (Integer) o1;

					if (v2.dom().isEmpty()) 
						throw Store.failException;
					
					if (c1 < v2.min()) 
						return "true";
					else if (c1 >= v2.max()) 
						return "false";
					return new XgtCCloneable(v2, c1);
				}

				System.err.println("Failed to parse lt(" + o1 + ", " + o2 + ")");
				return null;
			}

			if (token.equals("not")) {
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				
				if ("true".equals(o1)) 
					return "false";
				else if ("false".equals(o1)) 
					return "true";

				return new NotCloneable((PrimitiveConstraint) o1);
			}

			if (token.equals("and")) {
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				
				if ("true".equals(o1)) {
					if ("true".equals(o2)) 
						return "true";
					else if ("false".equals(o2)) 
						return "false";
					return o2;
				} else if ("false".equals(o1)) 
					return "false";
				else if ("true".equals(o2)) 
					return o1;
				else if ("false".equals(o2)) 
					return "false";

				return new AndCloneable((PrimitiveConstraint) o1, (PrimitiveConstraint) o2);
			}

			if (token.equals("or")) {
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				
				if ("true".equals(o1)) 
					return "true";
				else if ("true".equals(o2)) 
					return "true";
				else if ("false".equals(o1)) {
					if ("false".equals(o2)) 
						return "false";
					return o2;
				} else if ("false".equals(o2)) 
					return o1;

				return new OrCloneable((PrimitiveConstraint) o1, (PrimitiveConstraint) o2);
			}

			if (token.equals("xor")) {
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				
				if ("true".equals(o1)) {
					if ("false".equals(o2)) 
						return "true";
					else if ("true".equals(o2)) 
						return "false";
					return new NotCloneable((PrimitiveConstraint) o2);
				} else if ("false".equals(o1)) {
					if ("true".equals(o2)) 
						return "true";
					else if ("false".equals(o2)) 
						return "false";
					return o2;
				} else if ("true".equals(o2)) 
					return new NotCloneable((PrimitiveConstraint) o1);
				else if ("false".equals(o2)) 
					return o1;

				return new EqCloneable(new NotCloneable((PrimitiveConstraint) (o1)), (PrimitiveConstraint) (o2));
			}
			
			if (token.equals("iff")) {
				Object o1 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				Object o2 = parse(tokenizer.nextToken(), tokenizer, store, variableMaping);
				
				if ("true".equals(o1)) {
					if ("true".equals(o2)) 
						return "true";
					else if ("false".equals(o2)) 
						return "false";
					return o2;
				} else if ("false".equals(o1)) {
					if ("false".equals(o2)) 
						return "true";
					else if ("true".equals(o2)) 
						return "false";
					return new NotCloneable((PrimitiveConstraint) o2);
				} else if ("true".equals(o2)) 
					return o1;
				else if ("false".equals(o2)) 
					return new NotCloneable((PrimitiveConstraint) o1);
				
				return new EqCloneable((PrimitiveConstraint) (o1), (PrimitiveConstraint) (o2));
			}

			System.err.println("Unknown token: `" + token + "'");
			return null;

		}

	}


	/** @see org.jacop.constraints.DecomposedConstraint#imposeDecomposition(org.jacop.core.Store) */
	@Override
	public void imposeDecomposition(Store store) 
			throws FailException {
		
		if (decompositionConstraints == null)
			decompose(store);
		
		for (Constraint c : decompositionConstraints)
			store.impose(c);
		
		store.auxilaryVariables.addAll(auxilaryVariables);
	}

	/** @see org.jacop.constraints.DecomposedConstraint#auxiliaryVariables() */
	@Override
	public ArrayList<Var> auxiliaryVariables() {
		return auxilaryVariables;
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public List<IntVarCloneable> arguments() {

		String[] paramNames = this.constraintParameters.split("\\s+");
		ArrayList<IntVarCloneable> out = new ArrayList<IntVarCloneable>(paramNames.length);

		for (String paramName : paramNames) {
			IntVarCloneable var = (IntVarCloneable) this.store.findVariable(paramName);
			if (var != null) // the parameter may also be a constant
				out.add(var);
		}

		if (this.utilVar != null) /// @bug The utilVar is only initialized after the parsing
			out.add(utilVar);

		return out;
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public Predicate cloneInto(StoreCloneable targetStore) throws CloneNotSupportedException {
		return new Predicate (this.constraintParameters, this.predicateParameters, this.description, targetStore);
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

}
