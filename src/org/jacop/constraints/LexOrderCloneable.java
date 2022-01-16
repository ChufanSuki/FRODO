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

/** A cloneable version of the LexOrder constraint
 * @author Thomas Leaute
 */
public class LexOrderCloneable extends LexOrder implements ConstraintCloneableInterface<LexOrderCloneable> {

    /**
     * It creates a lexicographical order for vectors x and y, 
     *
     * vectors x and y does not need to be of the same size.
     *
     * @param x first vector constrained by LexOrder constraint. 
     * @param y second vector constrained by LexOrder constraint. 
     */
	public LexOrderCloneable(IntVarCloneable[] x, IntVarCloneable[] y) {
		super(x, y);
	}

    /**
     * It creates a lexicographical order for vectors x and y, 
     *
     * vectors x and y does not need to be of the same size.
     *
     * @param x 	first vector constrained by LexOrder constraint. 
     * @param y 	second vector constrained by LexOrder constraint. 
     * @param lt 	defines if we require strict order, Lex_{\<} (lt = true) or Lex_{\<=} (lt = false)
    */
	public LexOrderCloneable(IntVarCloneable[] x, IntVarCloneable[] y, boolean lt) {
		super(x, y, lt);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public LexOrderCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the X
		IntVarCloneable[] x2 = new IntVarCloneable [this.x.length];
		for (int i = this.x.length - 1; i >= 0; i--) 
			if ((x2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.x[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		// Clone the Y
		IntVarCloneable[] y2 = new IntVarCloneable [this.y.length];
		for (int i = this.y.length - 1; i >= 0; i--) 
			if ((y2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.y[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
		
		return new LexOrderCloneable (x2, y2, this.lexLT);
	}

}
