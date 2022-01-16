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

import org.jacop.core.IntVarCloneable;
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the XplusYplusQgtC constraint
 * @author Thomas Leaute
 */
public class XplusYplusQgtCCloneable extends XplusYplusQgtC implements ConstraintCloneableInterface<XplusYplusQgtCCloneable> {

	/** Constructor
	 * @param x 	the X variable
	 * @param y 	the Y variable 
	 * @param q 	the Q variable
	 * @param c 	the C constant
	 */
	public XplusYplusQgtCCloneable(IntVarCloneable x, IntVarCloneable y, IntVarCloneable q, int c) {
		super(x, y, q, c);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public XplusYplusQgtCCloneable cloneInto(StoreCloneable targetStore) {
		
		IntVarCloneable x2 = targetStore.findOrCloneInto((IntVarCloneable) this.x);
		if (x2.dom().isEmpty()) 
			throw Store.failException;
		
		IntVarCloneable y2 = targetStore.findOrCloneInto((IntVarCloneable) this.y);
		if (y2.dom().isEmpty()) 
			throw Store.failException;
		
		IntVarCloneable q2 = targetStore.findOrCloneInto((IntVarCloneable) this.q);
		if (q2.dom().isEmpty()) 
			throw Store.failException;
		
		return new XplusYplusQgtCCloneable (x2, y2, q2, this.c);
	}

}
