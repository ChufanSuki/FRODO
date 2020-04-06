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

import java.util.Collection;
import java.util.HashMap;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.core.FailException;
import org.jacop.core.Store;
import org.jacop.core.StoreCloneable;

/** A cloneable version of the Geost constraint
 * @author Thomas Leaute
 */
public class GeostCloneable extends Geost implements ConstraintCloneableInterface<GeostCloneable> {

	/**
	 * It creates a geost constraint from provided objects, external constraints, 
	 * as well as shapes. The construct parameters are not cloned so do not 
	 * reuse them in creation in other constraints if changes are necessary.
	 * Make sure that the largest object id is as small as possible to avoid 
	 * unnecessary memory cost. 
	 * 
	 * @param objects objects in the scope of the geost constraint.
	 * @param constraints the collection of external constraints enforced by geost.
	 * @param shapes the list of different shapes used by the objects in scope of the geost. 
	 * 
	 */
	public GeostCloneable(Collection<GeostObject> objects, Collection<ExternalConstraint> constraints, Collection<Shape> shapes) {
		super(objects, constraints, shapes);
	}

	/**
	 * It creates a geost constraint from provided objects, external constraints, 
	 * as well as shapes. The construct parameters are not cloned so do not 
	 * reuse them in creation in other constraints if changes are necessary.
	 * Make sure that the largest object id is as small as possible to avoid 
	 * unnecessary memory cost. 
	 * 
	 * @param objects objects in the scope of the geost constraint.
	 * @param constraints the collection of external constraints enforced by geost.
	 * @param shapes the list of different shapes used by the objects in scope of the geost. 
	 * 
	 */
	public GeostCloneable(GeostObjectCloneable[] objects, ExternalConstraintCloneable[] constraints, ShapeCloneable[] shapes) {
		super(objects, constraints, shapes);
	}

	/** @see org.jacop.constraints.ConstraintCloneableInterface#cloneInto(org.jacop.core.StoreCloneable) */
	@Override
	public GeostCloneable cloneInto(StoreCloneable targetStore) 
	throws FailException {
		
		// Clone the GeostObject array
		GeostObjectCloneable[] objects2 = new GeostObjectCloneable [this.objects.length];
		HashMap<Integer, GeostObjectCloneable> objMap = new HashMap<Integer, GeostObjectCloneable> (this.objects.length); // the keys are the objects' IDs
		GeostObjectCloneable obj;
		for (int i = this.objects.length - 1; i >= 0; i--) {
			obj = objects2[i] = ((GeostObjectCloneable) this.objects[i]).cloneInto(targetStore);
			objMap.put(obj.no, obj);
		}
		
		// "Clone" the constraints
		ExternalConstraintCloneable[] cons2 = new ExternalConstraintCloneable [this.externalConstraints.length];
		for (int i = cons2.length - 1; i >= 0; i--) 
			cons2[i] = ((ExternalConstraintCloneable) this.externalConstraints[i]).cloneAndReplace(objMap);
		
		// Clone the shapes
		ShapeCloneable[] shapes2 = new ShapeCloneable [this.shapeRegister.length];
		for (int i = shapes2.length - 1; i >= 0; i--) 
			shapes2[i] = ((ShapeCloneable) this.shapeRegister[i]).clone();
		
		return new GeostCloneable (objects2, cons2, shapes2);
	}

	/** @see org.jacop.constraints.geost.Geost#toString() */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder (super.toString() + ": ");
		
		for (GeostObject object : this.objects) 
			builder.append(object).append(", ");
		
		return builder.toString();
	}

	/** @see org.jacop.constraints.geost.Geost#impose(org.jacop.core.Store) */
	@Override
	public void impose(Store store) {
		super.impose(store);
		
		super.lastLevelLastVar.update(-1); // Works around a bug in JaCoP https://github.com/radsz/jacop/issues/40
	}

}
