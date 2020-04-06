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

/** A cloneable version of the AbsXeqY constraint
 * @author Thomas Leaute
 */
public class AbsXeqYCloneable extends AbsXeqY implements ConstraintCloneableInterface<AbsXeqYCloneable> {

	/** Constructor
	 * @param x 	the X variable
	 * @param y 	the Y variable
	 */
	public AbsXeqYCloneable(IntVarCloneable x, IntVarCloneable y) {
		super(x, y);
	}

	/** Constructor
	 * @param x 	the X variable
	 * @param y 	the Y variable
	 * @param domConsistency 	whether to enforce domain- (true) or bound- (false) consistency
	 */
	public AbsXeqYCloneable(IntVarCloneable x, IntVarCloneable y, boolean domConsistency) {
		super(x, y, domConsistency);
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public AbsXeqYCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		IntVarCloneable x2 = targetStore.findOrCloneInto((IntVarCloneable) this.x); 
		if (x2.dom().isEmpty()) 
			throw Store.failException;
		
		IntVarCloneable y2 = targetStore.findOrCloneInto((IntVarCloneable) this.y); 
		if (y2.dom().isEmpty()) 
			throw Store.failException;
		
		return new AbsXeqYCloneable (x2, y2, this.domainConsistent);
	}

}
