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

package org.jacop.constraints.netflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.constraints.DecomposedConstraintCloneableInterface;
import org.jacop.core.FailException;
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;
import org.jacop.core.Var;

/** A cloneable version of the Arithmetic constraint
 * @author Thomas Leaute
 */
public class ArithmeticCloneable extends Arithmetic implements ConstraintCloneableInterface<ArithmeticCloneable>, DecomposedConstraintCloneableInterface {

	/** Used to increment the id of constraints */
	private static int idNbr = 0;
	
	/** This constraint's ID/name */
	private final String id;

    /** A list of equations, each identified by its coefficients (copy of super.eqns, which is private) */
    private List<int[]> eqns = new ArrayList<int[]>();;

    /** The list of variables (copy of super.vars, which is private) */
    private List<IntVar> vars = new ArrayList<IntVar>();

    /** Each variable's index in the list of variables (copy of super.map, which is private) */
    private Map<IntVar, Integer> map;

    /** Default constructor */
	public ArithmeticCloneable() {
		this.id = this.getClass().getSimpleName() + idNbr++;
        vars.add(NULL_VAR);
        this.map = Var.createEmptyPositioning();
        map.put(NULL_VAR, 0);
	}

    /** Looks up the index of a variable
     * @param var 	the variable
     * @return 		the index of the variable in the list of variables
     */
    private int lookup(IntVarCloneable var) {
        Integer id = map.get(var);
        if (id == null) {
            map.put(var, id = vars.size());
            vars.add(var);
        }
        return id;
    }

    /** Add an equation
     * @param vars 		the variables
     * @param coeffs 	the coefficients
     */
    public void addEquation(IntVarCloneable[] vars, int[] coeffs) {
        super.addEquation(vars, coeffs);

        this.addEquation(vars, coeffs, 0);
    }

    /** Add an equation
     * @param vars 		the variables
     * @param coeffs 	the coefficients
     * @param constant 	the right-hand-side constant term
     * @note It is necessary to re-implement this method because super.eqns is private. 
     */
    public void addEquation(IntVarCloneable[] vars, int[] coeffs, int constant) {
    	super.addEquation(vars, coeffs, constant);
    	
        if (vars.length == 0 || vars.length != coeffs.length)
            throw new IllegalArgumentException();

        int max = 1;
        for (int i = 0; i < vars.length; i++) {
            int id = lookup(vars[i]);
            if (max <= id)
                max = id + 1;
        }

        int[] eqn = new int[max];
        for (int i = 0; i < vars.length; i++) {
            int id = lookup(vars[i]);
            eqn[id] = coeffs[i];
        }
        eqn[0] = constant;
        eqns.add(eqn);
    }

    /** Add an x + y = z equation
     * @param x 	the X variable
     * @param y 	the Y variable
     * @param z 	the Z variable
     */
    public void addXplusYeqZ(IntVarCloneable x, IntVarCloneable y, IntVarCloneable z) {
    	super.addXplusYeqZ(x, y, z);
    	
        IntVarCloneable[] vars = {x, y, z};
        int[] coeffs = {1, 1, -1};
        this.addEquation(vars, coeffs);
    }

    /** Add an x - y = z equation
     * @param x 	the X variable
     * @param y 	the Y variable
     * @param z 	the Z variable
     */
   public void addXsubYeqZ(IntVarCloneable x, IntVarCloneable y, IntVarCloneable z) {
    	super.addXsubYeqZ(x, y, z);
    	
        this.addXplusYeqZ(z, y, x);
    }

    /** Adds a sum equation 
     * @param vars 	the variables
     * @param sum 	the total sum
     */
    public void addSum(IntVarCloneable[] vars, IntVarCloneable sum) {
    	super.addSum(vars, sum);
    	
        int n = vars.length;
        IntVarCloneable[] _vars = Arrays.copyOf(vars, n + 1);
        int[] coeffs = new int[n + 1];

        Arrays.fill(coeffs, 1);
        _vars[n] = sum;
        coeffs[n] = -1;

        this.addEquation(_vars, coeffs);
    }

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public ArithmeticCloneable cloneInto(StoreCloneable targetStore) 
			throws CloneNotSupportedException, FailException {
		
		ArithmeticCloneable out = new ArithmeticCloneable ();
		
		// Clone the coefficients
		for (int[] eq : this.eqns) 
			out.eqns.add(eq.clone());
		
		// Clone the variables
		final int nbrVars = this.vars.size();
		IntVarCloneable var2;
		for (int i = 1; i < nbrVars; i++) { // starting at 1 because 0 is NULL_VAR
			out.map.put(var2 = targetStore.findOrCloneInto((IntVarCloneable) this.vars.get(i)), i);
			if (var2.dom().isEmpty()) 
				throw Store.failException;
			out.vars.add(var2);
		}
		
		return out;
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#arguments() */
	@Override
	public List<? extends Var> arguments() {
		
		// Remove the NULL_VAR
		ArrayList<IntVar> out = new ArrayList<IntVar> (this.vars);
		out.remove(NULL_VAR);
		
		return out;
	}

	/** @see org.jacop.constraints.DecomposedConstraintCloneableInterface#id() */
	@Override
	public String id() {
		return this.id;
	}

}
