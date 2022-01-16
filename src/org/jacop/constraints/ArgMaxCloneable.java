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
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the ArgMax constraint 
 * @author Thomas Leaute
 */
public class ArgMaxCloneable extends ArgMax implements ConstraintCloneableInterface<ArgMaxCloneable> {

	/** Constructor
	 * @param vars 			the variables
	 * @param argmaxIndex 	the variable whose value is the index of the argmax variable
	 */
	public ArgMaxCloneable(IntVarCloneable[] vars, IntVarCloneable argmaxIndex) {
		super(vars, argmaxIndex);
	}

	/** Constructor
	 * @param vars 			the variables
	 * @param argmaxIndex 	the variable whose value is the index of the argmax variable
	 */
	public ArgMaxCloneable(ArrayList<? extends IntVarCloneable> vars, IntVarCloneable argmaxIndex) {
		super(vars, argmaxIndex);
	}

	/** Constructor
	 * @param vars 			the variables
	 * @param argmaxIndex 	the variable whose value is the index of the argmax variable
     * @param indexOffset 	the offset for the index that is computed from 1 by default (if needed from 0, use -1 for this parameter)
     * @param tiebreak 		defines if tie breaking should be used (returning the least index if several maximum elements
	 */
	public ArgMaxCloneable(IntVarCloneable[] vars, IntVarCloneable argmaxIndex, int indexOffset, boolean tiebreak) {
		super(vars, argmaxIndex, indexOffset, tiebreak);
	}

	/** Constructor
	 * @param vars 			the variables
	 * @param argmaxIndex 	the variable whose value is the index of the argmax variable
     * @param indexOffset 	the offset for the index that is computed from 1 by default (if needed from 0, use -1 for this parameter)
     * @param tiebreak 		defines if tie breaking should be used (returning the least index if several maximum elements
	 */
	public ArgMaxCloneable(ArrayList<? extends IntVarCloneable> vars, IntVarCloneable argmaxIndex, int indexOffset, boolean tiebreak) {
		super(vars, argmaxIndex, indexOffset, tiebreak);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public ArgMaxCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the list 
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw Store.failException;
		
		IntVarCloneable maxIndex2 = targetStore.findOrCloneInto((IntVarCloneable) this.maxIndex);
		if (maxIndex2.dom().isEmpty()) 
			throw Store.failException;
		
		return new ArgMaxCloneable (list2, maxIndex2);
	}
}
