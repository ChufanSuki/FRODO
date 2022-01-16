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

/** A cloneable version of the BoolClause constraint
 * @author Thomas Leaute
 */
public class BoolClauseCloneable extends BoolClause implements ConstraintCloneableInterface<BoolClauseCloneable> {

    /**
     * It constructs BoolClause. 
     * 
     * @param x list of positive arguments x's.
     * @param y list of negative arguments y's. 
     */
	public BoolClauseCloneable(IntVarCloneable[] x, IntVarCloneable[] y) {
		super(x, y);
	}

    /**
     * It constructs BoolClause. 
     * 
     * @param x list of positive arguments x's.
     * @param y list of negative arguments y's. 
     */
	public BoolClauseCloneable(ArrayList<IntVarCloneable> x, ArrayList<IntVarCloneable> y) {
		super(x.toArray(new IntVarCloneable [x.size()]), y.toArray(new IntVarCloneable [y.size()]));
	}

	/** @see ConstraintCloneableInterface#cloneInto(StoreCloneable) */
	@Override
	public BoolClauseCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the X
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw Store.failException;
		
		// Clone the Y
		IntVarCloneable[] y2 = new IntVarCloneable [this.y.length];
		for (int i = this.y.length - 1; i >= 0; i--) 
			if ((y2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.y[i])).dom().isEmpty()) 
				throw Store.failException;
		
		return new BoolClauseCloneable (x2, y2);
	}
	
}
