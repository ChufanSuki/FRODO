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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.constraints.Constraint;
import org.jacop.core.FailException;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;
import org.jacop.core.ValueEnumeration;
import org.jacop.core.Var;

/** A JaCoP extensional constraint encoded by a hypercube for faster lookups
 * @author Thomas Leaute
 */
public class ExtensionalSupportHypercube extends Constraint implements ConstraintCloneableInterface<ExtensionalSupportHypercube> {
	
	/** The ID assigned to the last instantiated object of this class */
	private static int lastID = -1;
	
	/** The JaCoP store */
	private StoreCloneable store;
	
	/** The variables */
	private IntVarCloneable[] vars;
	
	/** this.vars.length - 2 */
	private final int nbrVarsMin2;
	
	/** The multi-dimensional array indicating the utility of each possible tuple */
	private Object hypercube;
	
	/** For each variable (except the utility variable), for each value in its domain, the index of this value in the domain */
	private ArrayList< HashMap<Integer, Integer> > indexes;

	/** Private constructor that does not initialize the hypercube
	 * @param store 		the store
	 * @param vars 			the list of variables, the last being the utility variable
	 * @param indexes 		ror each variable (except the utility variable), for each value in its domain, the index of this value in the domain
	 */
	private ExtensionalSupportHypercube (StoreCloneable store, IntVarCloneable[] vars, ArrayList< HashMap<Integer, Integer> > indexes) {

		super.numberId = ++lastID;
		this.nbrVarsMin2 = vars.length - 2;
		this.store = store;
		
		// Look up the variables from the store
		this.vars = new IntVarCloneable [vars.length];
		for (int i = vars.length - 1; i >= 0; i--) 
			this.vars[i] = store.findOrCloneInto(vars[i]);
		
		// Create the variable value indexes
		if (indexes != null) 
			this.indexes = indexes;
		else {
			final int nbrVars = vars.length;
			this.indexes = new ArrayList< HashMap<Integer, Integer> > (nbrVars - 1); // skipping the last variable, which is the utility variable
			for (int i = 0; i <= this.nbrVarsMin2; i++) {

				HashMap<Integer, Integer> map = new HashMap<Integer, Integer> ();
				this.indexes.add(map);

				int j = 0;
				for (ValueEnumeration iter = vars[i].dom().valueEnumeration(); iter.hasMoreElements(); ) 
					map.put(iter.nextElement(), j++);
			}
		}
	}

	/** Constructor
	 * @param store 	the store
	 * @param vars 		the list of variables, the last being the utility variable
	 * @param tuples 	the list of allowed tuples
	 */
	public ExtensionalSupportHypercube(StoreCloneable store, IntVarCloneable[] vars, int[][] tuples) {
		this(store, vars, (ArrayList< HashMap<Integer, Integer> >) null);
		
		// Create the hypercube
		final int nbrVarsMin1 = vars.length - 1;
		int[] dimensions = new int [nbrVarsMin1]; // skipping the last variable, which is the utility variable
		for (int i = this.nbrVarsMin2; i >= 0; i--) 
			dimensions[i] = vars[i].dom().getSize();
		assert dimensions.length > 0;
		this.hypercube = Array.newInstance(Integer.class, dimensions);
		
		// Populate the hypercube
		tuplesLoop: for (int[] tuple : tuples) {
			
			// Slice the hypercube following all non-utility variables but the last
			Object slice = this.hypercube;
			for (int i = 0; i < nbrVarsMin2; i++) {
				
				if (this.indexes.get(i).get(tuple[i]) == null) // the tuple value is not found in the variable's domain
					continue tuplesLoop;
				
				slice = Array.get(slice, this.indexes.get(i).get(tuple[i]));
			}
			
			// Record the utility of the tuple
			Array.set(slice, this.indexes.get(nbrVarsMin2).get(tuple[nbrVarsMin2]), tuple[nbrVarsMin1]);
		}
		
		assert this.checkIndexesVsHypercube();
	}

	/** @see org.jacop.constraints.Constraint#arguments() */
	@Override
	public HashSet<Var> arguments() {
		return new HashSet<Var> (Arrays.asList(this.vars));
	}
	
	/** Method used as an assert
	 * @return whether the indexes are consistent with the hypercube
	 */
	private boolean checkIndexesVsHypercube () {
		
		Object slice = this.hypercube;
		
		// For each variable
		for (int i = 0; i <= this.nbrVarsMin2; i++) {
			
			// Check the hypercube's dimension against this variable's number of indexes
			if (Array.getLength(slice) < this.indexes.get(i).size()) {
				System.err.println("hypercube.dimension(" + i + ") = " + Array.getLength(slice) + " < " + this.indexes.get(i).size());
				System.err.println("vars = " + Arrays.toString(this.vars));
				System.err.println("hypercube = " + Arrays.deepToString((Object[]) this.hypercube));
				System.err.println("indexes = " + this.indexes);
				return false; 
			}
			
			slice = Array.get(slice, 0);
		}
		
		return true;
	}

	/** @see org.jacop.constraints.Constraint#consistency(org.jacop.core.Store) */
	@Override
	public void consistency(Store arg0) {
		
//		System.out.println(Arrays.toString(this.vars));
		
		// Try to look up the value of the utility variable
		Object slice = this.hypercube;
		IntVarCloneable var = null;
		for (int i = 0; i < this.nbrVarsMin2; i++) { // for each non-utility variable except the last
			
			// Fail to check consistency if the variable's domain is not a singleton
			/// @todo Propagate earlier
			if (! (var = this.vars[i]).dom().singleton()) 
				return;
			
			// Look up the value index of the variable's current value
			Integer index = this.indexes.get(i).get(var.value());
			if (index == null) // the variable has been assigned an unknown value
				throw Store.failException;
			
			slice = Array.get(slice, index);
		}
		
		// Fail to check consistency if the variable's domain is not a singleton
		/// @todo Propagate earlier
		if (! (var = this.vars[this.nbrVarsMin2]).dom().singleton()) 
			return;
		
		// Look up the value index of the variable's current value
		Integer index = this.indexes.get(this.nbrVarsMin2).get(var.value());
		if (index == null) // the variable has been assigned an unknown value
			throw Store.failException;

		// Look up the utility 
		Integer util = (Integer) Array.get(slice, index);
		if (util == null) // infeasible tuple
			throw Store.failException;
		else 
			(var = this.vars[this.nbrVarsMin2 + 1]).dom().in(this.store.level, var, util, util);
	}

	/** @see org.jacop.constraints.Constraint#getConsistencyPruningEvent(org.jacop.core.Var) */
	@Override
	public int getConsistencyPruningEvent(Var arg0) {
		return IntDomain.GROUND;
	}

	/** @see org.jacop.constraints.Constraint#getDefaultConsistencyPruningEvent() */
	@Override
	public int getDefaultConsistencyPruningEvent() {
		return IntDomain.GROUND;
	}

	/** @see org.jacop.constraints.Constraint#impose(org.jacop.core.Store) */
	@Override
	public void impose(Store store) {
		assert store == this.store : "Constraint being imposed into a different store";
		
		for (IntVarCloneable var : this.vars) 
			var.putModelConstraint(this, this.getConsistencyPruningEvent(var));
		store.addChanged(this);
		store.countConstraint();
	}

	/** @see org.jacop.constraints.Constraint#increaseWeight() */
	@Override
	public void increaseWeight() {
		for (IntVarCloneable var : this.vars)
			var.weight++;
	}

	/** @see org.jacop.constraints.Constraint#removeConstraint() */
	@Override
	public void removeConstraint() {
		for (IntVarCloneable var : this.vars)
			var.removeConstraint(this);
	}

	/** @see org.jacop.constraints.Constraint#toString() */
	@Override
	public String toString() {
		
		StringBuffer out = new StringBuffer ();
		
		out.append(this.id()).append("(");
		
		out.append(Arrays.toString(this.vars));
//		out.append(", ").append(Arrays.deepToString((Object[]) this.hypercube));
		
		out.append(")");
		
		return out.toString();
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public ExtensionalSupportHypercube cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		ExtensionalSupportHypercube out = new ExtensionalSupportHypercube (targetStore, this.vars, this.indexes);
		
		// Clone the variables
		for (int i = this.vars.length - 1; i >= 0; i--) 
			if ((out.vars[i] = targetStore.findOrCloneInto((IntVarCloneable) this.vars[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		out.hypercube = this.hypercube; // no need to make a copy: the hypercube will never be changed
		
		assert out.checkIndexesVsHypercube();
		
		return out;
	}

}
