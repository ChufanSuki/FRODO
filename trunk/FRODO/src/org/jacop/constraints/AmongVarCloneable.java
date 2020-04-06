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

/** A cloneable version of the AmongVar constraint
 * @author Thomas Leaute
 */
public class AmongVarCloneable extends AmongVar implements ConstraintCloneableInterface<AmongVarCloneable> {

	/** Constructor
	 * @param xList 	the x variables
	 * @param yList 	the y variables
	 * @param nVar		the n variable
	 */
	public AmongVarCloneable(IntVarCloneable[] xList, IntVarCloneable[] yList, IntVarCloneable nVar) {
		super(xList, yList, nVar);
	}

	/**
	 * It constructs an AmongVar constraint. 
	 * @param listOfX the list of variables whose equality to other set of variables we count
	 * @param listOfY the list of variable to which equality is counted.
	 * @param n how many variables from list x are equal to at least one variable from list y.
	 */
	public AmongVarCloneable(ArrayList<IntVarCloneable> listOfX, ArrayList<IntVarCloneable> listOfY, IntVarCloneable n) {
		this(listOfX.toArray(new IntVarCloneable [listOfX.size()]), listOfY.toArray(new IntVarCloneable [listOfY.size()]), n);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public AmongVarCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the X
		IntVarCloneable[] listOfX2 = new IntVarCloneable [this.listOfX.length];
		for (int i = this.listOfX.length - 1; i >= 0; i--) 
			if ((listOfX2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.listOfX[i])).dom().isEmpty()) 
				throw Store.failException;
		
		// Clone the Y
		IntVarCloneable[] listOfY2 = new IntVarCloneable [this.listOfY.length];
		for (int i = this.listOfY.length - 1; i >= 0; i--) 
			if ((listOfY2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.listOfY[i])).dom().isEmpty()) 
				throw Store.failException;
		
		IntVarCloneable n2 = targetStore.findOrCloneInto((IntVarCloneable) this.n); 
		if (n2.dom().isEmpty()) 
			throw Store.failException;
		
		return new AmongVarCloneable (listOfX2, listOfY2, n2);
	}

}
