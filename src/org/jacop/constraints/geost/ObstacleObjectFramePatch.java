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

import org.jacop.core.IntDomain;
import org.jacop.core.Store;

/** Overrides its parent class to fix a bug in JaCoP 
 * @author Thomas Leaute
 * @see "https://github.com/radsz/jacop/issues/41"
 */
public class ObstacleObjectFramePatch extends ObstacleObjectFrame {

    /**
     * It creates an internal constraint to enforce non-overlapping relation with this
     * obstacle object.
     *
     * @param geost              the geost constraint which this constraint is part of.
     * @param obstacle           the obstacle object responsible for this internal constraint.
     * @param selectedDimensions the dimensions on which the constraint is applied
     */
	public ObstacleObjectFramePatch(Geost geost, GeostObject obstacle, int[] selectedDimensions) {
		super(geost, obstacle, selectedDimensions);
	}

    /** @see ObstacleObjectFrame#timeOnlyCheck(Geost.SweepDirection, LexicographicalOrder, GeostObject, int, int[]) */
    protected boolean timeOnlyCheck(Geost.SweepDirection min, LexicographicalOrder order, GeostObject o, int currentShape, int[] c) {

        if (useTime) {
            //if there is no overlap in time, no need to continue
            if (min == Geost.SweepDirection.PRUNEMIN) {
                //largest possible origin when objects begin to overlap (infeasible)
                timeSizeOrigin = obstacle.start.max() - o.duration.min() + 1;
                //smallest possible end is when placed after the obstacle (feasible)
                if (obstacle.start.min() + obstacle.duration.min() > obstacle.end.min()) 
                	throw Store.failException;
                timeSizeMax = obstacle.end.min();
            } else {
                //PRUNEMAX: the outbox has to mark the upper limit of the possible domain (end variable)

				/* in the usual case (not time), we can simply prune the upper bound of the origin domain.
				 * However, in this case, since the "length" changes, we really have to consider the maximal
				 * ending time.
				 */

                timeSizeOrigin = obstacle.start.max() + 1;
                if (obstacle.start.min() + obstacle.duration.min() > obstacle.end.min()) 
                	throw Store.failException;
                timeSizeMax = obstacle.end.min() + o.duration.min();
            }

            if (timeSizeMax - timeSizeOrigin <= 0)
                return false;

            //check if point is between bounds, if not return null
            if (c[obstacle.dimension] < timeSizeOrigin || c[obstacle.dimension] > timeSizeMax)
                //point cannot be contained in outbox, no need to continue
                return false;
            else
                return true;

        } else {
            //time is not included in the dimensions, thus the outbox covers the whole space
            timeSizeOrigin = IntDomain.MinInt;
            timeSizeMax = IntDomain.MaxInt;
            return true;
        }
    }

}
