/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2015  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

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
<http://frodo2.sourceforge.net/>
*/

package frodo2.algorithms.duct.samplingMethods;

import frodo2.algorithms.duct.SearchNode;
import frodo2.algorithms.duct.SearchNodeChild;
import frodo2.algorithms.duct.samplingMethods.SamplingProcedure;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableReal;

/**
 * @author Brammert Ottens, Oct 24, 2011
 * @param <V> type used for domain values
 * 
 */
public class SamplingB_Child <V extends Addable<V>> extends SamplingProcedure<V> {
	
	/** 
	 * @see frodo2.algorithms.duct.samplingMethods.SamplingProcedure#processSample(frodo2.algorithms.duct.SearchNode, AddableReal, boolean)
	 */
	@Override
	public void processSample(SearchNode<V> dist, AddableReal infeasibleUtility, final boolean maximize) {
		dist.maxBound = maximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		double maxBound = maximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		double maxValue = maximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		int maxBoundIndex = -1;
		int maxValueIndex = -1;
		for(int i = 0; i < dist.numberOfValues; i++) {
			if(dist.localCosts[i] != infeasibleUtility) {
				double best = dist.bestUtil[i];
				double childB = ((SearchNodeChild<V>)dist).childB[i];
				if(maximize ? maxValue <  best : maxValue > best) {
					maxValue = best;
					maxValueIndex = i;
				}

				if(maximize) {
					best += dist.bounds[i];
					best = best < childB ? best : childB; 
				} else {
					best -= dist.bounds[i];
					best = best < childB ? childB : best;
				}
				
				if(maximize ? maxBound <  best : maxBound > best) {
					if(maximize ? dist.maxBound <  best : dist.maxBound > best)
						dist.maxBound = best;
					if(dist.bestUtil[i] != childB) {
						maxBoundIndex = i;
						maxBound = best;
					}
				}
			}
		}
		
		if(dist.feasible && maxBoundIndex == -1) {
			maxBound = maxValue;
			maxBoundIndex = maxValueIndex;
			((SearchNodeChild<V>)dist).complete = true;
		}
		
		dist.maxValue = maxValue;
		dist.maxBoundIndex = maxBoundIndex;
		dist.maxValueIndex = maxValueIndex;
		
	}

}
