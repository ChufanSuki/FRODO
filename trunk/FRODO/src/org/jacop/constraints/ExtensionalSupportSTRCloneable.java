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

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the ExtensionalSupportSTR constraint
 * @author Thomas Leaute
 */
public class ExtensionalSupportSTRCloneable extends ExtensionalSupportSTR implements ConstraintCloneableInterface<ExtensionalSupportSTRCloneable> {

	/** Constructor
	 * @param variables 	the list of variables
	 * @param tuples 		the list of allowed tuples
	 */
	public ExtensionalSupportSTRCloneable(IntVarCloneable[] variables, int[][] tuples) {
		super(variables, tuples);
	}

	/**
	 * It constructs an extensional constraint.
	 * @param list the variables in the scope of the constraint.
	 * @param tuples the tuples which are supports.
	 * @param reinsertBefore it specifies if the tuples which were removed and are reinstatiated are inserted at the beginning.
	 * @param residuesBefore it specifies if the residue tuples are moved to the beginning. 
	 */
	public ExtensionalSupportSTRCloneable(IntVarCloneable[] list, int[][] tuples, boolean reinsertBefore, boolean residuesBefore) {
		super(list, tuples, reinsertBefore, residuesBefore);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public ExtensionalSupportSTRCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the variables if necessary
		final int nbrVars = this.list.length;
		IntVarCloneable[] list2 = new IntVarCloneable [nbrVars];
		for (int i = nbrVars - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		// Clone the tuples
		int[][] tuples2 = new int [this.tuples.length][nbrVars];
		for (int i = this.tuples.length - 1; i >= 0; i--) {
			if (this.firstConsistencyCheck) 
				System.arraycopy(this.tuples[i], 0, tuples2[i], 0, nbrVars);
			
			else { // after the first consistency check, tuples by value have been replaced by tuples by index
				int[] tuplesByIndex = this.tuples[i];
				int[] tuples2byValue = tuples2[i];
				for (int j = tuplesByIndex.length - 1; j >= 0; j--) 
					tuples2byValue[j] = this.list[j].dom().getElementAt(tuplesByIndex[j]);
			}
		}
		
		return new ExtensionalSupportSTRCloneable (list2, tuples2);
	}
	
}
