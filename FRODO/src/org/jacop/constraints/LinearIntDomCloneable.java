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

/** A cloneable version of the LinearIntDom constraint
 * @author Thomas Leaute
 */
public class LinearIntDomCloneable extends LinearIntDom implements ConstraintCloneableInterface<LinearIntDomCloneable> {

	/** Constructor
	 * @param list 		the list of variable being summed
	 * @param weights 	the summation weights 
	 * @param rel 		the relation name
	 * @param sum 		the total sum variable
	 */
	public LinearIntDomCloneable(IntVarCloneable[] list, int[] weights, String rel, IntVarCloneable sum) {
		super(list, weights, rel, sum);
	}

	/** Constructor
	 * @param list 		the list of variable being summed
	 * @param weights 	the summation weights 
	 * @param rel 		the relation name
	 * @param sum 		the total sum constant
	 */
	public LinearIntDomCloneable(IntVarCloneable[] list, int[] weights, String rel, int sum) {
		super(list, weights, rel, sum);
	}

	/** Constructor
	 * @param list 		the list of variable being summed
	 * @param weights 	the summation weights 
	 * @param rel 		the relation name
	 * @param sum 		the total sum constant
	 */
	public LinearIntDomCloneable(ArrayList<? extends IntVarCloneable> list, ArrayList<Integer> weights, String rel, int sum) {
		super(list, weights, rel, sum);
	}

    /** Constructor used by the cloneInto() method
     * @param o 				a LinearInt constraint to be cloned
     * @param targetStore 		the store to be cloned into
     * @throws FailException 	thrown if one of the variables in the target store has an empty domain
     */
    private LinearIntDomCloneable (LinearIntDomCloneable o, StoreCloneable targetStore) 
    throws FailException {
    	super(new IntVarCloneable [0], new int [0], "==", 0);
    	
		this.store = targetStore;
		this.b = o.b;
		this.pos = o.pos;
		this.l = o.l;
		this.queueIndex = o.queueIndex;
		
		// Clone the X
		this.x = new IntVarCloneable [o.x.length];
		for (int i = o.x.length - 1; i >= 0; i--) 
			if ((this.x[i] = targetStore.findOrCloneInto((IntVarCloneable) o.x[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;

		// Clone the a
		this.a = new long [o.a.length];
		System.arraycopy(o.a, 0, this.a, 0, o.a.length);
		
		// Clone the I
		this.I = new long [o.I.length];
		System.arraycopy(o.I, 0, this.I, 0, o.I.length);
    }

    /** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable)  */
	@Override
	public LinearIntDomCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the X
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		Integer sum = 0; // keeps track of the sum of all grounded variables; null if one isn't grounded
		for (int i = this.x.length - 1; i >= 0; i--) {
			switch ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().getSize()) {
			
			case 0: 
				throw StoreCloneable.failException;
			case 1: 
				sum += x2[i].value();
				break;
			default: 
				sum = null;
			}
		}
		
		if (sum != null) { // all variables are grounded; check for consistency
			switch (this.relationType) {
			
			case eq: 
				if (sum.intValue() != this.b) 
					throw StoreCloneable.failException;
				
			case le: 
				if (sum.intValue() > this.b) 
					throw StoreCloneable.failException;
				
			case lt:
				if (sum.intValue() >= this.b) 
					throw StoreCloneable.failException;
				
			case ne:
				if (sum.intValue() == this.b) 
					throw StoreCloneable.failException;
				
			case gt:
				if (sum.intValue() <= this.b) 
					throw StoreCloneable.failException;
				
			case ge:
				if (sum.intValue() < this.b) 
					throw StoreCloneable.failException;
				
			default: 
				return null; // the constraint is already consistent
			}
		}
		
		// Clone the a to an int[]
		int[] a2 = new int [this.a.length];
		for (int i = this.a.length - 1; i >= 0; i--) 
			a2[i] = (int) this.a[i];
		
		LinearIntDomCloneable out = new LinearIntDomCloneable (x2, a2, "==", (int) this.b);
        out.relationType = this.relationType;
		
		return out;
	}
}
