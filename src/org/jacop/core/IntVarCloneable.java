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

import org.jacop.core.IntDomain;

/** A JaCoP IntVar that is cloneable into a store
 * @author Thomas Leaute
 */
public class IntVarCloneable extends org.jacop.core.IntVar implements CloneableInto<IntVarCloneable> {

	/** Empty constructor */
	public IntVarCloneable() { }

	/** Constructor
	 * @param store 	the store
	 */
	public IntVarCloneable(StoreCloneable store) {
		super(store);
	}

	/** Constructor
	 * @param store 	the store
	 * @param dom 		the variable domain
	 */
	public IntVarCloneable(StoreCloneable store, IntDomain dom) {
		super(store, dom);
	}

	/** Constructor 
	 * @param store 	the store
	 * @param name 		the name/id of the variable
	 */
	public IntVarCloneable(StoreCloneable store, String name) {
		super(store, name);
	}

	/** Constructor
	 * @param store 	the store
	 * @param name 		the name/id of the variable
	 * @param dom 		the variable domain
	 */
	public IntVarCloneable(StoreCloneable store, String name, IntDomain dom) {
		super(store, name, dom);
	}

	/** Constructor
	 * @param store the store
	 * @param min 	minimum value
	 * @param max 	maximum value
	 */
	public IntVarCloneable(StoreCloneable store, int min, int max) {
		super(store, min, max);
	}

	/** Constructor
	 * @param store the store
	 * @param name 	name/id of the variable
	 * @param min 	minimum value
	 * @param max 	maximum value
	 */
	public IntVarCloneable(StoreCloneable store, String name, int min, int max) {
		super(store, name, min, max);
	}

	/** 
	 * @see CloneableInto#cloneInto(StoreCloneable) 
	 */
	@Override
	public IntVarCloneable cloneInto(StoreCloneable store2) {
		return new IntVarCloneable (store2, this.id, this.domain.cloneLight());
	}

	/** True if the two variables have the same ID
	 * @see java.lang.Object#equals(java.lang.Object) 
	 */
	@Override
	public boolean equals (Object o) {
		
		if (this == o) 
			return true;
		
		if (o == null) 
			return false;
		
		IntVarCloneable v = null;
		try {
			v = (IntVarCloneable) o;
		} catch (ClassCastException e) {
			return false;
		}
		
		return this.id.equals(v.id);
	}
	
	/** Returns the hash code of this variable's ID
	 * @see java.lang.Object#hashCode() 
	 */
	@Override
	public int hashCode () {
		return this.id.hashCode();
	}
	
}
