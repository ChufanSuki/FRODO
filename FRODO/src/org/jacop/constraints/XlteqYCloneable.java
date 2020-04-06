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

/** A cloneable version of the XlteqY constraint
 * @author Thomas Leaute
 */
public class XlteqYCloneable extends XlteqY implements ConstraintCloneableInterface<XlteqYCloneable>  {

	/** Constructor
	 * @param x 	the X variable
	 * @param y 	the Y variable
	 */
	public XlteqYCloneable(IntVarCloneable x, IntVarCloneable y) {
		super(x, y);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public XlteqYCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		IntVarCloneable x2 = targetStore.findOrCloneInto((IntVarCloneable) this.x);
		if (x2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		IntVarCloneable y2 = targetStore.findOrCloneInto((IntVarCloneable) this.y);
		if (y2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new XlteqYCloneable (x2, y2);
	}

}
