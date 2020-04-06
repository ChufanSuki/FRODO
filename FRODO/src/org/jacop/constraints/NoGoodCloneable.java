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
import org.jacop.core.IntVar;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the NoGood constraint
 * @author Thomas Leaute
 */
public class NoGoodCloneable extends NoGood implements ConstraintCloneableInterface<NoGoodCloneable> {

    /**
     * It specifies a list of variables in no-good constraint.
     */
    private final IntVar listOfVars[];

    /**
     * It specifies a list of values in no-good constraint.
     */
    private final int listOfValues[];

	/** Constructor
	 * @param listOfVars 	the list of variables
	 * @param listOfValues 	the list of values
	 */
	public NoGoodCloneable(IntVarCloneable[] listOfVars, int[] listOfValues) {
		super(listOfVars, listOfValues);
		this.listOfVars = listOfVars;
		this.listOfValues = listOfValues;
	}

	/** Constructor
	 * @param listOfVars 	the list of variables
	 * @param listOfValues 	the list of values
	 */
	public NoGoodCloneable(ArrayList<? extends IntVarCloneable> listOfVars, ArrayList<Integer> listOfValues) {
		super(listOfVars, listOfValues);
		this.listOfVars = listOfVars.toArray(new IntVarCloneable [listOfVars.size()]);
		this.listOfValues = listOfValues.stream().mapToInt(i -> i).toArray();
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public NoGoodCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the IntVar array
		IntVarCloneable[] listOfVars2 = new IntVarCloneable [this.listOfVars.length];
		for (int i = this.listOfVars.length - 1; i >= 0; i--) 
			if ((listOfVars2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.listOfVars[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		return new NoGoodCloneable (
				listOfVars2, 
				this.listOfValues.clone());
	}

}
