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

package org.jacop.constraints.geost;

import java.util.Arrays;

import org.jacop.core.FailException;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.StoreCloneable;

/** A cloneable version of GeostObject
 * @author Thomas Leaute
 */
public class GeostObjectCloneable extends GeostObject {
	
	/** ID of the last instantiated object */
	static private int lastID = -1;
	
	/** ID of this object */
	final int id = ++lastID;

	/**
	 * 
	 * It constructs a Geost object with all the attributes needed by the Geost
	 * constraint. 
	 * 
	 * @param no nonnegative unique id of this object. 
	 * @param coords an array of variables representing the origin (start) of the objects.
	 * @param shapeID the variable specifying the shape finite domain variable.
	 * @param start it determines the start time of the geost object in terms of time.
	 * @param duration finite domain variable specifying the duration of the geost object in terms of time.
	 * @param end finite domain variable specifying the end of the geost object in terms of time.
	 */
	public GeostObjectCloneable(int no, IntVarCloneable[] coords, IntVarCloneable shapeID, IntVarCloneable start, IntVarCloneable duration, IntVarCloneable end) {
		super(no, coords, shapeID, start, duration, end);
	}

	/** Creates a "clone" of this object using IntVar found or created in the input target store
	 * @param targetStore 		the store into which this object should be cloned
	 * @return a clone of this object in the input target store
	 * @throws FailException 	thrown if one of the variables has an empty domain
	 */
	public GeostObjectCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the coordinates
		IntVarCloneable[] coords2 = new IntVarCloneable [this.coords.length];
		for (int i = coords2.length - 1; i >= 0; i--) 
			if ((coords2[i] = targetStore.findOrCloneInto((IntVarCloneable) this.coords[i])).dom().isEmpty()) 
				throw StoreCloneable.failException;
	
		IntVarCloneable shapeID2 = targetStore.findOrCloneInto((IntVarCloneable) this.shapeID);
		if (shapeID2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		IntVarCloneable start2 = targetStore.findOrCloneInto((IntVarCloneable) this.start);
		if (start2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		IntVarCloneable duration2 = targetStore.findOrCloneInto((IntVarCloneable) this.duration);
		if (duration2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		IntVarCloneable end2 = targetStore.findOrCloneInto((IntVarCloneable) this.end);
		if (end2.dom().isEmpty()) 
			throw StoreCloneable.failException;
		
		return new GeostObjectCloneable (no, coords2, shapeID2, start2, duration2, end2);
	}

	/** @see org.jacop.constraints.geost.GeostObject#toString() */
	@Override
	public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Object(").append(this.id).append(", ");
        builder.append(shapeID).append(", ");
        builder.append(Arrays.toString(coords)).append(", ");
        builder.append(start).append(", ");
        builder.append(duration).append(", ");
        builder.append(end);
        builder.append(")");

        return builder.toString();

    }

}
