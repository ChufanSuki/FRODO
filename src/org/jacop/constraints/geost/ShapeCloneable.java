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

import java.util.ArrayList;
import java.util.Collection;

/** A cloneable version of Shape
 * @author Thomas Leaute
 */
public class ShapeCloneable extends Shape implements Cloneable {

	/**
	 * It constructs a shape with a given id based on a specified collection of Dboxes.
	 *  
	 * @param no the unique identifier of the created shape.
	 * @param boxes the collection of boxes constituting the shape.
	 */
	public ShapeCloneable(int no, Collection<DBox> boxes) {
		super(no, boxes);
	}

	/**
	 * It constructs a shape from only one DBox. 
	 * 
	 * @param id shape unique identifier.
	 * @param box the single dbox specifying the shape.
	 */
	public ShapeCloneable(int id, DBox box) {
		super(id, box);
	}

	/**
	 * It constructs a shape with a given id based on a single dbox
	 * specified by the origin and length arrays.
	 * 
	 * @param id the unique identifier of the constructed shape.
	 * @param origin it specifies the origin of the dbox specifying the shape.
	 * @param length it specifies the length of the dbox specifying the shape.
	 */
	public ShapeCloneable(int id, int[] origin, int[] length) {
		super(id, origin, length);
	}

	/** @see java.lang.Object#clone() */
	@Override
	public ShapeCloneable clone () {
		return new ShapeCloneable (this.no, new ArrayList<DBox> (this.boxes)); // no need to clone each box; it is unmutable
	}
	
}
