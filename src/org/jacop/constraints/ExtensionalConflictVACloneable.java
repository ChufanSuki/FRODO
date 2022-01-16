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

/** A cloneable version of the ExtensionalConflictVA constraint
 * @author Thomas Leaute
 */
public class ExtensionalConflictVACloneable extends ExtensionalConflictVA implements ConstraintCloneableInterface<ExtensionalConflictVACloneable> {

	/** The original tuples passed to the constructor */
	private final int[][] tuplesInit;
	
	/** Constructor
	 * @param vars 		the variables
	 * @param tuples 	the tuples
	 */
	public ExtensionalConflictVACloneable(IntVarCloneable[] vars, int[][] tuples) {
		super(vars, tuples);
		
		final int nbrVars = vars.length;
		this.tuplesInit = new int [tuples.length][nbrVars];
		for (int i = tuples.length - 1; i >= 0; i--) 
			System.arraycopy(tuples[i], 0, this.tuplesInit[i], 0, nbrVars);
	}

	/** Constructor
	 * @param vars 		the variables
	 * @param tuples 	the tuples
	 */
	public ExtensionalConflictVACloneable(ArrayList<? extends IntVarCloneable> vars, int[][] tuples) {
		this(vars.toArray(new IntVarCloneable [vars.size()]), tuples);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public ExtensionalConflictVACloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the IntVar array
		final int nbrVars = this.list.length;
		IntVarCloneable[] list2 = new IntVarCloneable [nbrVars];
		for (int i = nbrVars - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		// Clone the int double array
		int[][] tuples2 = new int [this.tuplesInit.length][nbrVars];
		for (int i = this.tuplesInit.length - 1; i >= 0; i--) 
			System.arraycopy(this.tuplesInit[i], 0, tuples2[i], 0, nbrVars);
		
		return new ExtensionalConflictVACloneable (list2, tuples2);
	}

	/** @see org.jacop.constraints.ExtensionalConflictVA#findPosition(int, int[]) */
	@Override
	protected int findPosition(int value, int[] values) {
		
		// Work around bug in JaCoP: https://github.com/radsz/jacop/issues/31
		if (values.length == 0) 
			return -1; 
		
		return super.findPosition(value, values);
	}

}
