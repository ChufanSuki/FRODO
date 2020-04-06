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
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the AndBoolSimple constraint
 * @author Thomas Leaute
 */
public class AndBoolSimpleCloneable extends AndBoolSimple implements ConstraintCloneableInterface<AndBoolSimpleCloneable> {

	/** Constructor
	 * @param a 		the A variable
	 * @param b 		the B variable
	 * @param result 	the result variable
	 */
	public AndBoolSimpleCloneable(IntVarCloneable a, IntVarCloneable b, IntVarCloneable result) {
		super(a, b, result);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public AndBoolSimpleCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		IntVarCloneable a2 = targetStore.findOrCloneInto((IntVarCloneable) this.a); 
		if (a2.dom().isEmpty()) 
			throw Store.failException;
		
		IntVarCloneable b2 = targetStore.findOrCloneInto((IntVarCloneable) this.b); 
		if (b2.dom().isEmpty()) 
			throw Store.failException;
		
		IntVarCloneable result2 = targetStore.findOrCloneInto((IntVarCloneable) this.result); 
		if (result2.dom().isEmpty()) 
			throw Store.failException;
		
		return new AndBoolSimpleCloneable (a2, b2, result2);
	}

}
