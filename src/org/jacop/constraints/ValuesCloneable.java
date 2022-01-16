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

/** A cloneable version of the Values constraint
 * @author Thomas Leaute
 */
public class ValuesCloneable extends Values implements ConstraintCloneableInterface<ValuesCloneable> {

    /**
     * It specifies a list of variables which are counted.
     */
    final private IntVarCloneable[] list;

    /**
     * It specifies the idNumber of different values among variables on a given list.
     */
    final private IntVarCloneable count;

	/**
	 * It constructs Values constraint.
	 * 
	 * @param list list of variables for which different values are being counted.
	 * @param count specifies the number of different values in the list. 
	 */
	public ValuesCloneable(IntVarCloneable[] list, IntVarCloneable count) {
		super(list, count);
		this.list = list;
		this.count = count;
	}

	/**
	 * It constructs Values constraint.
	 * 
	 * @param list list of variables for which different values are being counted.
	 * @param count specifies the number of different values in the list. 
	 */
	public ValuesCloneable(ArrayList<? extends IntVarCloneable> list, IntVarCloneable count) {
		super(list, count);
		this.list = list.toArray(new IntVarCloneable [list.size()]);
		this.count = count;
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public ValuesCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the IntVar array
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		IntVarCloneable count2 = targetStore.findOrCloneInto((IntVarCloneable) this.count);
		if (count2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new ValuesCloneable (list2, count2);
	}

}
