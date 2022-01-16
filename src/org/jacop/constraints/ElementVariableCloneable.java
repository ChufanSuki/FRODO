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

/** A cloneable version of the ElementVariable constraint
 * @author Thomas Leaute
 */
public class ElementVariableCloneable extends ElementVariable implements ConstraintCloneableInterface<ElementVariableCloneable> {

	/**
	 * It constructs an element constraint. 
	 * 
	 * @param index variable index
	 * @param list list of variables from which an index-th element is taken
	 * @param value a value of the index-th element from list
	 */
	public ElementVariableCloneable(IntVarCloneable index, ArrayList<? extends IntVarCloneable> list, IntVarCloneable value) {
		super(index, list, value);
	}

	/**
	 * It constructs an element constraint. 
	 * 
	 * @param index variable index
	 * @param list list of variables from which an index-th element is taken
	 * @param value a value of the index-th element from list
	 */
	public ElementVariableCloneable(IntVarCloneable index, IntVarCloneable[] list, IntVarCloneable value) {
		super(index, list, value);
	}

	/** Constructor
	 * @param index 		the index variable
	 * @param list 			the list of allowed values, indexed by the index variable
	 * @param value 		the variable whose value is constrained to be = list[index - indexOffset]
	 * @param indexOffset 	the index offset
	 */
	public ElementVariableCloneable(IntVarCloneable index, IntVarCloneable[] list, IntVarCloneable value, int indexOffset) {
		super(index, list, value, indexOffset);
	}

	/**
	 * It constructs an element constraint. 
	 * 
	 * @param index variable index
	 * @param list list of variables from which an index-th element is taken
	 * @param value a value of the index-th element from list
	 * @param indexOffset shift applied to index variable. 
	 */
	public ElementVariableCloneable(IntVarCloneable index, ArrayList<? extends IntVarCloneable> list, IntVarCloneable value, int indexOffset) {
		super(index, list, value, indexOffset);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public ElementVariableCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the IntVar array
		IntVarCloneable[] list2 = new IntVarCloneable [this.list.length];
		for (int i = this.list.length - 1; i >= 0; i--) 
			if ((list2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.list[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		IntVarCloneable index2 = targetStore.findOrCloneInto((IntVarCloneable) this.index);
		if (index2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		IntVarCloneable value2 = targetStore.findOrCloneInto((IntVarCloneable) this.value);
		if (value2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new ElementVariableCloneable (index2, list2, value2, this.indexOffset);
	}

}
