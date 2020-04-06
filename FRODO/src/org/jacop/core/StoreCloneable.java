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

package org.jacop.core;

import org.jacop.core.Var;

/** A JaCoP store that support cloning
 * @author Thomas Leaute
 *
 */
public class StoreCloneable extends org.jacop.core.Store {

	/** Constructor */
	public StoreCloneable() {
		super.setID("store" + Math.random());
	}

	/** Tries to find a variable with the same id as the input variable; else creates a clone of it
	 * @param var 	the input variable, typically from another store
	 * @return a variable in this store with id = var.id
	 */
	@SuppressWarnings("unchecked")
	public < V extends Var & CloneableInto<V> > V findOrCloneInto (V var) {
		
		Var var2 = this.findVariable(var.id());
		
		if (var2 != null) { // a variable with the same id already exists in this store
			try {
				return (V) var2;
			} catch (ClassCastException e) {
				throw new ClassCastException (var2 + " already exists in the target store but is not of type " + var.getClass().getName());
			}
		} else {
			try {
				return (V) var.cloneInto(this);
			} catch (ClassCastException e) {
				throw new ClassCastException (var.getClass().getName() + 
						".cloneInto(Store) returned the unexpected type " + var.cloneInto(this).getClass().getName());
			}
		}
	}
	
}
