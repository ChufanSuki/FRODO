/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2012  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

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

/** Classes implementing the Param-DPOP algorithm, able to solve DCOP involving free parameters */
package frodo2.algorithms.dpop.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;

import frodo2.algorithms.AgentFactory;
import frodo2.algorithms.StatsReporter;
import frodo2.algorithms.XCSPparser;
import frodo2.algorithms.dpop.DPOPsolver;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.AddableReal;
import frodo2.solutionSpaces.BasicUtilitySolutionSpace;
import frodo2.solutionSpaces.DCOPProblemInterface;
import frodo2.solutionSpaces.UtilitySolutionSpace;

/** A DCOP solver using Param-DPOP
 * @author Thomas Leaute
 * @param <V> type used for variable values
 * @param <U> type used for utility values
 */
public class ParamDPOPsolver< V extends Addable<V>, U extends Addable<U> > extends DPOPsolver<V, U> {

	/** An optimal solution to the problem
	 * @param <V> type used for variable values
	 * @param <U> type used for utility values
	 */
	public static class ParamSolution< V extends Addable<V>, U extends Addable<U> > {
		
		/** Utility of the solution, as a function of the parameters */
		private UtilitySolutionSpace<V, U> utility;
		
		/** For arrays of variables, their assignments in the solution found to the problem, as a function of the parameters */
		private Map< String[], BasicUtilitySolutionSpace< V, ArrayList<V> > > assignments;
		
		/** The number of ncccs used */
		private long ncccCount;
		
		/** The time needed to solve the problem*/
		private long timeNeeded;
		
		/** The tree width of the tree on which the algorithm has run */
		private int treeWidth = -1;

		/** Constructor 
		 * @param utility 		the optimal utility, as a function of the parameters
		 * @param assignments 	the optimal assignments, as a function of the parameters
		 * @param ncccCount 	the ncccs used
		 * @param timeNeeded 	the time needed to solve the problem
		 * @param treeWidth 	the width of the tree on which the algorithm has run
		 */
		public ParamSolution (UtilitySolutionSpace<V, U> utility, Map< String[], BasicUtilitySolutionSpace< V, ArrayList<V> > > assignments, long ncccCount, long timeNeeded, int treeWidth) {
			this.utility = utility;
			this.assignments = assignments;
			this.ncccCount = ncccCount;
			this.timeNeeded = timeNeeded;
			this.treeWidth = treeWidth;
		}

		/** @return the utility of the solution, as a function of the parameters */
		public UtilitySolutionSpace<V, U> getUtility() {
			return utility;
		}

		/** @return the optimal assignments found, as a function of the parameters */
		public Map< String[], BasicUtilitySolutionSpace< V, ArrayList<V> > > getAssignments() {
			return assignments;
		}
		
		/** @return the ncccCount */
		public long getNcccCount() {
			return ncccCount;
		}
		
		/**
		 * @author Brammert Ottens, 22 jun 2009
		 * @return the time needed to solve the problem
		 */
		public long getTimeNeeded() {
			return this.timeNeeded;
		}
		
		/** @return the tree width of the tree on which the algorithm ran */
		public int getTreeWidth() {
			return this.treeWidth;
		}

		/** @see java.lang.Object#toString() */
		@Override
		public String toString () {
			return "Optimal utility: " + this.utility + "\nAssignments:\t " + this.assignments;
		}
		
	}

	/** The UTIL propagation module */
	private ParamUTIL<V, U> utilModule;
	
	/** The VALUE propagation module */
	private ParamVALUE<V> valueModule;

	/** Default constructor */
	public ParamDPOPsolver () {
		this(false);
	}
	
	/** Constructor 
	 * @param useTCP 		whether to use TCP pipes
	 * @warning Using TCP pipes automatically disables simulated time. 
	 */
	@SuppressWarnings("unchecked")
	public ParamDPOPsolver (boolean useTCP) {
		this((Class<V>) AddableInteger.class, (Class<U>) AddableInteger.class, useTCP);
	}
	
	/** Constructor 
	 * @param domClass 		the class to use for variable values
	 * @param utilClass 	the class to use for utilities
	 */
	public ParamDPOPsolver (Class<V> domClass, Class<U> utilClass) {
		super ("/frodo2/algorithms/dpop/param/Param-DPOP.xml", domClass, utilClass);
	}
	
	/** Constructor 
	 * @param domClass 		the class to use for variable values
	 * @param utilClass 	the class to use for utilities
	 * @param useTCP 		whether to use TCP pipes
	 * @warning Using TCP pipes automatically disables simulated time. 
	 */
	public ParamDPOPsolver (Class<V> domClass, Class<U> utilClass, boolean useTCP) {
		super ("/frodo2/algorithms/dpop/param/Param-DPOP.xml", domClass, utilClass, useTCP);
	}
	
	/** Solves the input problem
	 * @param problem 	the problem
	 * @return 			an optimal solution
	 */
	public ParamSolution<V, U> solveParam (Document problem) {
		this.agentDesc.getRootElement().getChild("parser").setAttribute("displayGraph", "false");
		return this.solveParam(new XCSPparser<V, U> (problem, this.agentDesc.getRootElement().getChild("parser")));
	}
	
	/** Solves the input problem
	 * @param problem 	the problem
	 * @return 			an optimal solution
	 */
	public ParamSolution<V, U> solveParam (DCOPProblemInterface<V, U> problem) {
		
		this.problem = problem;
		
		// Instantiate the modules that listen for the solution
		ArrayList<StatsReporter> solGatherers = this.getSolGatherers();
		
		// Solve the problem
		this.agentDesc.getRootElement().getChild("parser").setAttribute("displayGraph", "false");
		AgentFactory<V> factory = new AgentFactory<V> (problem, agentDesc, solGatherers, null);
		factory.end();
		
		return new ParamSolution<V, U> (utilModule.getOptParamUtil(), valueModule.getParamSolution(), factory.getNcccs(), factory.getTime(), utilModule.getMaxMsgDim());
	}
	
	/** @see DPOPsolver#getSolGatherers() */
	@Override
	public ArrayList<StatsReporter> getSolGatherers() {

		ArrayList<StatsReporter> solGatherers = new ArrayList<StatsReporter> (2);
		
		utilModule = new ParamUTIL<V, U> (null, problem);
		utilModule.setSilent(true);
		solGatherers.add(utilModule);
		
		valueModule = new ParamVALUE<V> (null, problem);
		valueModule.setSilent(true);
		solGatherers.add(valueModule);
		
		return solGatherers;
	}
	
	/** Solves the problem and returns the expectation over the random variables of the optimal utility
	 * @param problem 		the problem
	 * @return 				the expected optimal utility
	 * @warning This method will only work if the class used for utilities is AddableReal. 
	 */
	@SuppressWarnings("unchecked")
	public UtilitySolutionSpace<V, AddableReal> getExpectedOptUtil (Document problem) {
				
		// Solve the problem
		UtilitySolutionSpace<V, AddableReal> util = (UtilitySolutionSpace<V, AddableReal>) this.solveParam(problem).getUtility();
		
		// Compute the expectation of the optimal parametric utility 
		this.agentDesc.getRootElement().getChild("parser").setAttribute("displayGraph", "false");
		XCSPparser<V, AddableReal> parser = new XCSPparser<V, AddableReal> (problem, this.agentDesc.getRootElement().getChild("parser"));
		parser.setUtilClass(AddableReal.class);
		HashMap< String, UtilitySolutionSpace<V, AddableReal> > distributions = 
			new HashMap< String, UtilitySolutionSpace<V, AddableReal> > ();
		for (UtilitySolutionSpace<V, AddableReal> probSpace : parser.getProbabilitySpaces()) 
			distributions.put(probSpace.getVariable(0), probSpace);
		if (! distributions.isEmpty()) 
			util = util.expectation(distributions);
		
		return util;
	}
	
	/** @see DPOPsolver#clear() */
	@Override
	public void clear () {
		super.clear();
		this.utilModule = null;
		this.valueModule = null;
	}
	
}
