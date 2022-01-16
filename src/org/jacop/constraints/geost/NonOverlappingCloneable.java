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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.jacop.core.FailException;

/** A cloneable version of NonOverlapping
 * @author Thomas Leaute
 */
public class NonOverlappingCloneable extends NonOverlapping implements ExternalConstraintCloneable {

	/**
	 * It creates an external constraint to make sure that specified set of objects does not overlap
	 * in k-dimensional space on the given number of selected dimensions within this k-dimensional space.
	 * 
	 * @param objects the set of objects which can not overlap
	 * @param selectedDimensions the dimensions among which there must be at least one for which the objects do not overlap. 
	 */
	public NonOverlappingCloneable(GeostObjectCloneable[] objects, int[] selectedDimensions) {
		super(objects, selectedDimensions);
	}

	/**
	 * It creates an external constraint to make sure that specified set of objects does not overlap
	 * in k-dimensional space on the given number of selected dimensions within this k-dimensional space.
	 * 
	 * @param objects the set of objects which can not overlap
	 * @param selectedDimensions the dimensions among which there must be at least one for which the objects do not overlap. 
	 */
	public NonOverlappingCloneable(Collection<GeostObject> objects, int[] selectedDimensions) {
		super(objects, selectedDimensions);
	}

	/** @see org.jacop.constraints.geost.ExternalConstraintCloneable#cloneAndReplace(java.util.Map) */
	@Override
	public NonOverlappingCloneable cloneAndReplace(Map<Integer, GeostObjectCloneable> objMap) 
	throws FailException {
		
		// Look up the object list
		GeostObjectCloneable[] objects2 = new GeostObjectCloneable [this.objects.length];
		for (int i = objects2.length - 1; i >= 0; i--) 
			objects2[i] = objMap.get(this.objects[i].no);
		
		return new NonOverlappingCloneable (objects2, this.selectedDimensions);
	}

    /** @see org.jacop.constraints.geost.NonOverlapping#genInternalConstraints(org.jacop.constraints.geost.Geost) */
    public Collection<ObstacleObjectFrame> genInternalConstraints(Geost geost) {

        if (objectConstraintMap == null) {

            //find largest object ID
            int largestID = 0;
            for (GeostObject o : objects)
                largestID = Math.max(largestID, o.no);

            objectConstraintMap = new ObstacleObjectFrame[largestID + 1];
            Arrays.fill(objectConstraintMap, null);

            constraints = new HashSet<ObstacleObjectFrame>();

            for (GeostObject o : objects) {

                ObstacleObjectFrame c;

                if (geost.alwaysUseFrames || !o.shapeID.singleton())
                    c = new ObstacleObjectFramePatch(geost, o, selectedDimensions);
                else
                    c = new ObstacleObjectPatch(geost, o, selectedDimensions);

                objectConstraintMap[o.no] = c;
                constraints.add(c);
            }
        }

        return constraints;
    }

}
