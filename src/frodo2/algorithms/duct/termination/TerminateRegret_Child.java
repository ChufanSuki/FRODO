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

package frodo2.algorithms.duct.termination;

import frodo2.algorithms.duct.SearchNode;
import frodo2.algorithms.duct.SearchNodeChild;
import frodo2.algorithms.duct.termination.TerminationCondition;
import frodo2.solutionSpaces.Addable;

/**
 * @author Brammert Ottens, Jan 16, 2012
 * @param <V> type used for domain values
 * 
 */
public class TerminateRegret_Child <V extends Addable<V>> implements TerminationCondition<V> {

	/** @see frodo2.algorithms.duct.termination.TerminationCondition#converged(frodo2.algorithms.duct.SearchNode, double, double, boolean) */
	@Override
	public boolean converged(SearchNode<V> dist, double error, double delta, boolean maximize) {
		// @todo create a check to see if the distribution is feasible and replace this with that check
		if(dist.maxValueIndex == -1)
			return true;
		
		if(dist.nbrFeasibleLocalSolutions == 1)
			return true;
		
		double maxBound = maximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		double[] bestUtils = dist.bestUtil;
		double bound;
		
		for(int i = 0; i < bestUtils.length; i++) {
			double childBound = ((SearchNodeChild<V>)dist).childB[i]; 
			if(bestUtils[i] == childBound)
				bound = bestUtils[i];
			else {
				if(maximize) {
					bound = bestUtils[i] + dist.convergenceBound(i, delta);
					bound = bound < childBound ? bound : childBound;
				} else {
					bound = bestUtils[i] - dist.convergenceBound(i, delta);
					bound = bound > childBound ? bound : childBound;
				}
			}
			if (maximize)
				maxBound = maxBound < bound ? bound : maxBound;
			else
				maxBound = maxBound < bound ? maxBound : bound;
		}
		
		return error >= (maximize ? maxBound - dist.maxValue : dist.maxValue - maxBound);
	}
}
