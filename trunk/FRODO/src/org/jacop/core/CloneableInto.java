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

/** An interface for JaCoP variables that are cloneable into another store
 * @author Thomas Leaute
 * @param <V> the type of variable this class can be cloned into
 */
public interface CloneableInto <V> {
	
	/** Clones this variable into the input store 
	 * @param store		the target store
	 * @return the cloned variable
	 */
	public V cloneInto (StoreCloneable store);

}
