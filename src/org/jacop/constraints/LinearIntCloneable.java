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

package org.jacop.constraints;

import java.util.ArrayList;

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable LinearInt constraint
 * @author Thomas Leaute
 */
public class LinearIntCloneable extends LinearInt implements ConstraintCloneableInterface<LinearIntCloneable> {

	/** Constructor
	 * @param list 		the list of variable being summed
	 * @param weights 	the summation weights 
	 * @param rel 		the relation name
	 * @param sum 		the total sum variable
	 */
	public LinearIntCloneable(IntVarCloneable[] list, int[] weights, String rel, IntVarCloneable sum) {
		super(list, weights, rel, sum);
	}

	/** Constructor
	 * @param list 		the list of variable being summed
	 * @param weights 	the summation weights 
	 * @param rel 		the relation name
	 * @param sum 		the total sum constant
	 */
	public LinearIntCloneable(IntVarCloneable [] list, int[] weights, String rel, int sum) {
		super(list, weights, rel, sum);
	}

	/** Constructor 
	 * @param vars 		the variables
	 * @param weights 	the weights
     * @param rel 		the relation, one of "==", "<", ">", "<=", ">=", "!="
	 * @param sum 		the constant with which the weighted sum should be compared
	 */
	public LinearIntCloneable(ArrayList<? extends IntVarCloneable> vars, ArrayList<Integer> weights, String rel, int sum) {
		super(vars, weights, rel, sum);
	}
	
    /**
     * Constructor used by the cloneInto() method
     * @param o 				a LinearIntCloneable constraint to be cloned
     * @param targetStore 		the store to be cloned into
     * @throws FailException 	thrown if one of the variables has an empty domain
     */
    protected LinearIntCloneable (LinearIntCloneable o, StoreCloneable targetStore) 
    throws FailException {
    	super();
    	
		// Clone the X
		IntVarCloneable[] x2 = new IntVarCloneable [o.x.length];
		for (int i = o.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) o.x[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		// Clone the a to an int[]
		int[] a2 = new int [o.a.length];
		for (int i = o.a.length - 1; i >= 0; i--) 
			a2[i] = (int) o.a[i];
		
    	this.commonInitialization(targetStore, x2, a2, "==", (int) this.b);
        this.relationType = o.relationType;
    }

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public LinearIntCloneable cloneInto(StoreCloneable targetStore) throws FailException {
		return new LinearIntCloneable (this, targetStore);
	}

}
