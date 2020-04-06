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

package frodo2.solutionSpaces.JaCoP;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jacop.constraints.ConstraintCloneableInterface;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.DecomposedConstraint;
import org.jacop.constraints.SumInt;
import org.jacop.constraints.XmulCeqZCloneable;
import org.jacop.constraints.XplusYeqC;
import org.jacop.core.CloneableInto;
import org.jacop.core.FailException;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.IntervalDomain;
import org.jacop.core.StoreCloneable;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.MostConstrainedDynamic;
import org.jacop.search.Search;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;

import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.BasicUtilitySolutionSpace;
import frodo2.solutionSpaces.ProblemInterface;
import frodo2.solutionSpaces.SolutionSpace;
import frodo2.solutionSpaces.UtilitySolutionSpace;
import frodo2.solutionSpaces.hypercube.BasicHypercube;
import frodo2.solutionSpaces.hypercube.Hypercube;
import frodo2.solutionSpaces.hypercube.ScalarHypercube;
import frodo2.solutionSpaces.hypercube.ScalarSpaceIter;
import frodo2.solutionSpaces.hypercube.Hypercube.NullHypercube;

/** A UtilitySolutionSpace based on JaCoP as a local solver
 * @author Thomas Leaute, Radoslaw Szymanek
 * @param <U> the type used for utility values
 */
public class JaCoPutilSpace < U extends Addable<U> > implements UtilitySolutionSpace<AddableInteger, U>, Externalizable {

	/** For now (and maybe forever) we will assume that all variables are projected in the same way (all minimized or all maximized) */
	private boolean maximize;

	/** The list of JaCoP Constraints */
	private ArrayList<Constraint> constraints;
	
	/** The list of JaCoP DecomposedConstraints */
	private ArrayList< DecomposedConstraint<Constraint> > decompConstraints;

	/** The list of utility variables from the constraints */
	private ArrayList<IntVarCloneable> utilVars;

	/** The total utility variable */
	private IntVarCloneable utilVar;

	/** The JaCoP Store */
	StoreCloneable store;

	/** The consistency of the JaCoP Store */
	Boolean isConsistent;

	/** The ordered variables of the space*/
	private IntVarCloneable[] vars;
	
	/** The ordered variables whose projection has been requested */
	private IntVarCloneable[] projectedVars;
	
	/** The variables that have been grounded by a slice operation */
	private IntVarCloneable[] slicedVars;

	/** The variable names and domains, including the projected out and sliced out variables, but excluding the utility variable */
	HashMap<String, AddableInteger[]> allDoms;

	/** The types of spaces that we know how to handle */
	private static HashSet< Class<?> > knownSpaces;

	static { /// @bug Doesn't it also know Hypercubes?
		knownSpaces = new HashSet< Class<?> > ();
		knownSpaces.add(JaCoPutilSpace.class);
		knownSpaces.add(ScalarHypercube.class);
	}

	/** The name of the UtilSpace **/
	private String name;

	/** The default Utility **/
	public U defaultUtil;

	/** The infeasible utility */
	public U infeasibleUtil;

	/** The owner of this space */
	private String owner;

	/** Constructor				constructs an explicit JaCoPutilSpace
	 * @param name				the name of the JaCoPutilSpace corresponds to the name of its XCSP constraint
	 * @param owner 			the owner
	 * @param constraints 		a list of Constraints
	 * @param decompCons 		a list of DecomposedConstraints
	 * @param utilVars 			a list of utility variables
	 * @param vars 				the variables
	 * @param domains 			the domains of the variables
	 * @param maximize	 		whether we should maximize the utility or minimize the cost
	 * @param infeasibleUtil 	the infeasible utility
	 */
	JaCoPutilSpace (String name, String owner, List<Constraint> constraints, List< DecomposedConstraint<Constraint> > decompCons, 
			List<IntVarCloneable> utilVars, IntVarCloneable[] vars, AddableInteger[][] domains, boolean maximize, U infeasibleUtil) {
		this.name = name;
		this.owner = owner;

		this.infeasibleUtil = infeasibleUtil;
		this.defaultUtil = this.infeasibleUtil.getZero();

		this.maximize = maximize;

		this.allDoms = new HashMap<String, AddableInteger[]>(vars.length);

		// Construct the array of variables
		assert vars.length == domains.length;
		for(int i = vars.length - 1; i >= 0; i--) 
			allDoms.put(vars[i].id(), domains[i]);

		// Create the store (without imposing the constraints) and clone the input JaCoP objects into it
		this.initStore(constraints, decompCons, utilVars, vars, new IntVarCloneable [0], new IntVarCloneable [0]);
		
		if (this.isConsistent != null && ! this.isConsistent) 
			this.defaultUtil = this.infeasibleUtil;
	}

	/** Empty constructor that does nothing */
	public JaCoPutilSpace() { }

	/** Constructor				construct an implicit JaCoPutilSpace
	 * @param name				the name of the JaCoPutilSpace
	 * @param constraints 		a list of JaCoP Constraints
	 * @param decompCons 		a list of JaCoP DecomposedConstraints
	 * @param utilVars 			a list of utility variables
	 * @param allVars			all the variables of the space including the projected out and sliced out variables
	 * @param vars				the variables
	 * @param projectedVars		the variables whose projection has been requested
	 * @param slicedVars 		the variables that have been sliced out
	 * @param maximize	 		whether we should maximize the utility or minimize the cost
	 * @param defaultUtil		The default utility
	 * @param infeasibleUtil 	The infeasible utility
	 */
	public JaCoPutilSpace (String name, List<Constraint> constraints, List< DecomposedConstraint<Constraint> > decompCons, List<IntVarCloneable> utilVars, 
			HashMap<String, AddableInteger[]> allVars, IntVarCloneable[] vars, IntVarCloneable[] projectedVars, IntVarCloneable[] slicedVars, 
			boolean maximize, U defaultUtil, U infeasibleUtil) {

		this.name = name;
		this.maximize = maximize;
		this.infeasibleUtil = infeasibleUtil;
		this.defaultUtil = defaultUtil;
		this.allDoms = new HashMap<String, AddableInteger[]>(allVars);
		
		// Create the store (without imposing the constraints) and clone the input JaCoP objects into it
		this.initStore(constraints, decompCons, utilVars, vars, projectedVars, slicedVars);
	}

	/** Creates the store and clones the inputs into it
	 * @param constraints2 		the Constraints
	 * @param decompCons 		the DecomposedConstraints
	 * @param utilVars2 		the utility variables
	 * @param vars2 			this space's variables
	 * @param projectedVars2 	the variables to be projected out
	 * @param slicedVars2 		the variables to be sliced out
	 */
	@SuppressWarnings("unchecked")
	private void initStore(List<Constraint> constraints2, List< DecomposedConstraint<Constraint> > decompCons, 
			List<IntVarCloneable> utilVars2, IntVarCloneable[] vars2, IntVarCloneable[] projectedVars2, IntVarCloneable[] slicedVars2) {
		
		// Create the JaCoP Store
		this.store = new StoreCloneable();
		
		// Clone the utility variables
		this.utilVars = new ArrayList<IntVarCloneable> (utilVars2.size());
		for (IntVarCloneable utilVar : utilVars2) 
			this.utilVars.add(this.store.findOrCloneInto(utilVar));
		
		// Clone the space's variables
		this.vars = new IntVarCloneable [vars2.length];
		for (int i = vars2.length - 1; i >= 0; i--) 
			this.vars[i] = this.store.findOrCloneInto(vars2[i]);
		
		// Clone the projected variables
		this.projectedVars = new IntVarCloneable [projectedVars2.length];
		for (int i = projectedVars2.length - 1; i >= 0; i--) 
			this.projectedVars[i] = this.store.findOrCloneInto(projectedVars2[i]);
		
		// Clone the sliced vars
		this.slicedVars = new IntVarCloneable [slicedVars2.length];
		for (int i = slicedVars2.length - 1; i >= 0; i--) 
			this.slicedVars[i] = this.store.findOrCloneInto(slicedVars2[i]);
		
		// Slice the variables' domains
		IntVarCloneable var;
		IntervalDomain dom;
		for (Map.Entry< String, AddableInteger[] > entry : this.allDoms.entrySet()) {
			
			// Look up the variable in the store
			if( (var = (IntVarCloneable) this.store.findVariable(entry.getKey())) == null) 
				continue;
			
			// Convert the AddableInteger[] domain to an IntervalDomain
			dom = new IntervalDomain ();
			for (AddableInteger val : entry.getValue()) 
				dom.addDom(new IntervalDomain (val.intValue(), val.intValue()));
			
			// Compute the intersection of the domains
			var.domain.intersectAdapt(dom);
		}

		this.constraints = new ArrayList<Constraint> (constraints2.size());
		this.decompConstraints = new ArrayList< DecomposedConstraint<Constraint> > (decompCons.size());

		// Clone the Constraints
		for (Constraint cons : constraints2) {
			try {
				Constraint clone = ((ConstraintCloneableInterface<Constraint>)cons).cloneInto(this.store);
				if (clone != null) 
					this.constraints.add(clone);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			} catch (FailException e) {
				this.isConsistent = false;
				return;
			}
		}

		// Clone the DecomposedConstraints
		for (DecomposedConstraint<Constraint> cons : decompCons) {
			try {
				DecomposedConstraint<Constraint> clone = ((ConstraintCloneableInterface< DecomposedConstraint<Constraint> >)cons).cloneInto(this.store);
				if (clone != null) 
					this.decompConstraints.add(clone);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			} catch (FailException e) {
				this.isConsistent = false;
				return;
			}
		}
	}

	/** 
	 * @return a ScalarHypercube if there is no variable left, or the current object 
	 * @throws ObjectStreamException should never be thrown
	 */
	private Object readResolve() throws ObjectStreamException {
		
		if (this.allDoms.size() == 0)
			return new ScalarHypercube<AddableInteger, U>(this.defaultUtil, this.infeasibleUtil, new AddableInteger [0].getClass());
		
		return this;
	}
	
	/** @see java.io.Externalizable#writeExternal(java.io.ObjectOutput) */
	///@todo change the whole method for optimizing purpose: do a master-slave JaCoP search to get only the feasible tuples,
	// then pass only those with their location and a default utility that is the infeasible utility 
	public void writeExternal(ObjectOutput out) throws IOException {
		// This function writes almost the same amount of data as the writeExternal of the corresponding hypercube would do

		// Write the name of the space and relation
		String newName = "explicit" + new Object().hashCode();
		out.writeObject(newName);

		// Write the type of optimization
		out.writeBoolean(maximize);

		// Write the variables
		assert this.vars.length < Short.MAX_VALUE : "Too many variables to fit in a short";
		out.writeShort(this.vars.length);
		for (int i = 0; i < this.vars.length; i++) 
			out.writeObject(this.vars[i].id());

		/// @todo Write less data by taking advantage of the fact that we know we are using AddableIntegers?

		// Write the domains
		assert this.vars.length < Short.MAX_VALUE : "Too many domains to fit in a short";
		out.writeShort(this.vars.length); // number of domains
		AddableInteger[] dom = this.allDoms.get(vars[0].id());
		assert dom.length < Short.MAX_VALUE : "Too many values to fit in a short";
		out.writeShort(dom.length); // size of first domain
		out.writeObject(dom[0]); // first value of first domain
		final boolean externalize = dom[0].externalize();
		for (int i = 1; i < dom.length; i++) { // remaining values in first domain
			if (externalize) 
				dom[i].writeExternal(out);
			else 
				out.writeObject(dom[i]);
		}
		for (int i = 1; i < this.vars.length; i++) { // remaining domains
			dom = this.allDoms.get(vars[i].id());
			assert dom.length < Short.MAX_VALUE : "Too many values to fit in a short";
			out.writeShort(dom.length); // size of domain
			for (int j = 0; j < dom.length; j++) { // each value in the domain
				if (externalize) 
					dom[j].writeExternal(out);
				else 
					out.writeObject(dom[j]);
			}
		}

		out.writeObject(this.infeasibleUtil);

		assert this.getNumberOfSolutions() < Integer.MAX_VALUE : "Cannot extensionalize a space containing more than " + Integer.MAX_VALUE + " solutions";
		for(Iterator<AddableInteger, U> iter = this.iterator(); iter.hasNext(); ) 
			out.writeObject(iter.nextUtility()); // each utility
	}

	/** @see java.io.Externalizable#readExternal(java.io.ObjectInput) */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
	ClassNotFoundException {
		// Construct the data structures that are special to JaCoPutilSpace

		this.store = new StoreCloneable();
		this.allDoms = new HashMap<String, AddableInteger[]>();
		this.projectedVars = new IntVarCloneable[0];
		this.slicedVars = new IntVarCloneable[0];

		this.name = (String) in.readObject();

		// Read the type of optimization
		this.maximize = in.readBoolean();

		// Read the variables
		this.vars = new IntVarCloneable[in.readShort()];
		for (int i = 0; i < this.vars.length; i++) 
			this.vars[i] = new IntVarCloneable (this.store, (String) in.readObject());

		// Read the domains
		final int nbrDoms = in.readShort(); // number of domains
		int domSize = in.readShort(); // size of first domain
		AddableInteger val = (AddableInteger) in.readObject(); // first value of first domain
		final boolean externalize = val.externalize();
		AddableInteger[] dom = (AddableInteger[]) Array.newInstance(val.getClass(), domSize);
		IntervalDomain jacopDom = new IntervalDomain (domSize);
		dom[0] = val;
		jacopDom.addDom(new IntervalDomain (val.intValue(), val.intValue()));
		for (int i = 1; i < domSize; i++) { // read the remaining values in the first domain
			if (externalize) {
				val = val.getZero();
				val.readExternal(in);
				val = dom[i] = (AddableInteger) val.readResolve();
			} else 
				val = dom[i] = (AddableInteger) in.readObject();
			jacopDom.addDom(new IntervalDomain (val.intValue(), val.intValue()));
		}
		this.allDoms.put(vars[0].id(), dom);
		this.vars[0].setDomain(jacopDom);
		
		for (int i = 1; i < nbrDoms; i++) { // read the remaining domains
			domSize = in.readShort(); // domain size
			dom = (AddableInteger[]) Array.newInstance(val.getClass(), domSize);
			jacopDom = new IntervalDomain (domSize);
			for (int j = 0; j < domSize; j++) { // each value in the domain
				if (externalize) {
					val = val.getZero();
					val.readExternal(in);
					val = dom[j] = (AddableInteger) val.readResolve();
				} else 
					val = dom[j] = (AddableInteger) in.readObject();
				jacopDom.addDom(new IntervalDomain (val.intValue(), val.intValue()));
			}
			this.allDoms.put(vars[i].id(), dom);
			this.vars[i].setDomain(jacopDom);
		}
		this.infeasibleUtil = (U) in.readObject();
		this.defaultUtil = this.infeasibleUtil.getZero();
		
		// Iterate through all of this space's solutions and read each solution's utility
		ScalarSpaceIter<AddableInteger, U> iter = 
				new ScalarSpaceIter<AddableInteger, U> (null, this.getVariables(), this.getDomains(), this.infeasibleUtil, null);
		
		// allVars contains this space's variables, plus its utility variable
		final int nbrVars = this.getNumberOfVariables();
		final int nbrAllVars = nbrVars + 1;
		IntVarCloneable[] allVars = Arrays.copyOf(this.vars, nbrAllVars);
		IntVarCloneable utilVar = allVars[this.getNumberOfVariables()] = new IntVarCloneable (this.store, "util_" + this.name);
		(this.utilVars = new ArrayList<IntVarCloneable> (1)).add(utilVar);
		HashSet<Integer> utilDom = new HashSet<Integer> ();
		
		// For each feasible solution, the variable assignments (including the utility variable)
		ArrayList< int[] > solutions = new ArrayList< int[] > ();
		
		U util;
		int intUtil, i;
		int[] sol;
		AddableInteger[] assignment;
		while (iter.hasNext()) {
			assignment = iter.nextSolution();
			
			if (! this.infeasibleUtil.equals(util = (U) in.readObject())) { /// @todo externalize if possible
				
				// Record the utility value as part of utilVar's domain
				utilDom.add(intUtil = util.intValue());
				
				solutions.add(sol = new int [nbrAllVars]);
				for (i = 0; i < nbrVars; i++) 
					sol[i] = assignment[i].intValue();
				sol[nbrVars] = intUtil;
			}
		}
		
		/// @bug Treat separately the case when no feasible solution exists
		
		// Set utilVar's domain
		IntervalDomain utilVarDom = new IntervalDomain (utilDom.size());
		for (Integer utilVal : utilDom) 
			utilVarDom.addDom(new IntervalDomain (intUtil = utilVal.intValue(), intUtil));
		utilVar.setDomain(utilVarDom);
		
		(this.constraints = new ArrayList<Constraint> (1)).add(
//				new ExtensionalSupportSTR (allVars, solutions.toArray(new int[solutions.size()][this.getNumberOfVariables()+1])));
				new ExtensionalSupportHypercube (store, allVars, solutions.toArray(new int [solutions.size()][nbrAllVars])));
		this.decompConstraints = new ArrayList< DecomposedConstraint<Constraint> > (0);
	}

	/** @see java.lang.Object#toString() */
	public String toString () {
		StringBuilder builder = new StringBuilder ("JaCoPutilSpace \n");
		builder.append("\t variables:\t" + Arrays.toString(this.vars) + "\n");
		if (this.projectedVars.length > 0) 
			builder.append("\t projectedVars:\t" + Arrays.toString(this.projectedVars) + "\n");
		if (this.slicedVars.length > 0) 
			builder.append("\t slicedVars:\t" + Arrays.toString(this.slicedVars) + "\n");
		if (! this.utilVars.isEmpty()) 
			builder.append("\t utilVars:\t").append(this.utilVars).append("\n");
		
		if (! this.constraints.isEmpty()) {
			builder.append("\t constraints:\t");
			for(Constraint cons: this.constraints){
				builder.append(cons + " ");
			}
			builder.append("\n");
		}
		
		if (! this.decompConstraints.isEmpty()) {
			builder.append("\t decomposed constraints:\t");
			for(DecomposedConstraint<Constraint> cons: this.decompConstraints){
				builder.append(cons + " ");
			}
			builder.append("\n");
		}
		
		builder.append("\t maximize:\t" + this.maximize + "\n");
		builder.append("\t baseUtil:\t" + this.defaultUtil + "\n");
		builder.append("\t infeasibleUtil:\t" + this.infeasibleUtil);
		return builder.toString();
	}

	/** @see UtilitySolutionSpace#changeVariablesOrder(java.lang.String[]) */
	public UtilitySolutionSpace<AddableInteger, U> changeVariablesOrder(
			final String[] variablesOrder) {
		
		/// @todo Perform some input checking

		JaCoPutilSpace<U> out = new JaCoPutilSpace<U> (this.name + "_reordered", this.constraints, this.decompConstraints, this.utilVars, 
				this.allDoms, this.vars, this.projectedVars, this.slicedVars, this.maximize, this.defaultUtil, this.infeasibleUtil);
		
		final int nbrVars = variablesOrder.length;
		for (int i = 0; i < nbrVars; i++) 
			out.vars[i] = (IntVarCloneable) this.store.findVariable(variablesOrder[i]);

		return out;

	}

	/** @see Object#clone() */
	@SuppressWarnings("unchecked")
	public UtilitySolutionSpace<AddableInteger, U> clone() {
		
		JaCoPutilSpace<U> out = new JaCoPutilSpace<U> ();
		
		out.allDoms = new HashMap<String, AddableInteger[]> (this.allDoms);
		out.defaultUtil = this.defaultUtil;
		out.infeasibleUtil = this.infeasibleUtil;
		out.maximize = this.maximize;
		out.name = this.name;
		out.owner = this.owner;
		out.utilVars = new ArrayList<IntVarCloneable> (this.utilVars);
		
		// Create a new store and clone the variables into it
		out.store = new StoreCloneable ();
		
		// Clone the variables in this space (utility variables will be re-created by the constraints)
		final int nbrVars = this.vars.length;
		out.vars = new IntVarCloneable [nbrVars];
		for (int i = 0; i < nbrVars; i++) 
			out.vars[i] = ((CloneableInto<IntVarCloneable>)this.vars[i]).cloneInto(out.store);
		
		// Also clone the projected variables
		final int nbrProjVars = this.projectedVars.length;
		out.projectedVars = new IntVarCloneable [nbrProjVars];
		for (int i = 0; i < nbrProjVars; i++) 
			out.projectedVars[i] = ((CloneableInto<IntVarCloneable>)this.projectedVars[i]).cloneInto(out.store);
		
		// Also clone the sliced variables
		final int nbrSlicedVars = this.slicedVars.length;
		out.slicedVars = new IntVarCloneable [nbrSlicedVars];
		for (int i = 0; i < nbrSlicedVars; i++) 
			out.slicedVars[i] = ((CloneableInto<IntVarCloneable>)this.slicedVars[i]).cloneInto(out.store);
		
		// Clone the Constraints
		out.constraints = new ArrayList<Constraint> (this.constraints.size());
		for (Constraint cons : this.constraints) {
			try {
				Constraint clone = ((ConstraintCloneableInterface<Constraint>)cons).cloneInto(out.store);
				if (clone != null) 
					out.constraints.add(clone);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			} catch (FailException e) {
				return NullHypercube.NULL;
			}
		}
		
		// Clone the DecomposedConstraints
		out.decompConstraints = new ArrayList< DecomposedConstraint<Constraint> > (this.decompConstraints.size());
		for (DecomposedConstraint<Constraint> cons : this.decompConstraints) {
			try {
				DecomposedConstraint<Constraint> clone = ((ConstraintCloneableInterface< DecomposedConstraint<Constraint> >)cons).cloneInto(out.store);
				if (clone != null) 
					out.decompConstraints.add(clone);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			} catch (FailException e) {
				return NullHypercube.NULL;
			}
		}
		
		out.imposeConstraints();

		return out;
	}

	/** @see UtilitySolutionSpace#compose(java.lang.String[], BasicUtilitySolutionSpace) */
	public UtilitySolutionSpace<AddableInteger, U> compose(
			String[] vars,
			BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> substitution) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#consensus(java.lang.String, java.util.Map, boolean) */
	public UtilitySolutionSpace.ProjOutput<AddableInteger, U> consensus(
			String varOut,
			Map<String, UtilitySolutionSpace<AddableInteger, U>> distributions,
			boolean maximum) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#consensusAllSols(java.lang.String, java.util.Map, boolean) */
	public UtilitySolutionSpace.ProjOutput<AddableInteger, U> consensusAllSols(
			String varOut,
			Map<String, UtilitySolutionSpace<AddableInteger, U>> distributions,
			boolean maximum) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see frodo2.solutionSpaces.UtilitySolutionSpace#consensusExpect(java.lang.String, java.util.Map, boolean) */
	@Override
	public frodo2.solutionSpaces.UtilitySolutionSpace.ProjOutput<AddableInteger, U> consensusExpect(
			String varOut,
			Map<String, UtilitySolutionSpace<AddableInteger, U>> distributions,
			boolean maximum) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see frodo2.solutionSpaces.UtilitySolutionSpace#consensusAllSolsExpect(java.lang.String, java.util.Map, boolean) */
	@Override
	public frodo2.solutionSpaces.UtilitySolutionSpace.ProjOutput<AddableInteger, U> consensusAllSolsExpect(
			String varOut,
			Map<String, UtilitySolutionSpace<AddableInteger, U>> distributions,
			boolean maximum) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see frodo2.solutionSpaces.UtilitySolutionSpace#expectation(java.util.Map) */
	public UtilitySolutionSpace<AddableInteger, U> expectation(Map< String, UtilitySolutionSpace<AddableInteger, U> > distributions) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#isIncludedIn(UtilitySolutionSpace) */
	public boolean isIncludedIn(
			UtilitySolutionSpace<AddableInteger, U> space) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return false;
	}

	/** @see UtilitySolutionSpace#iteratorBestFirst(boolean) */
	public UtilitySolutionSpace.IteratorBestFirst<AddableInteger, U> iteratorBestFirst(
			boolean maximize) {
		
		// Clone myself
		UtilitySolutionSpace<AddableInteger, U> clone = this.clone();
		
		if (NullHypercube.NULL.equals(clone)) 
			return new ScalarHypercube<AddableInteger, U> (this.infeasibleUtil, this.infeasibleUtil, AddableInteger[].class).iteratorBestFirst(maximize);

		return new JaCoPutilSpaceIterBestFirst<U> ((JaCoPutilSpace<U>) clone, maximize);
	}

	/** @see UtilitySolutionSpace#join(UtilitySolutionSpace, java.lang.String[]) */
	public UtilitySolutionSpace<AddableInteger, U> join(
			UtilitySolutionSpace<AddableInteger, U> space,
			String[] totalVariables) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#join(UtilitySolutionSpace) */
	@SuppressWarnings("unchecked")
	public UtilitySolutionSpace<AddableInteger, U> join(
			UtilitySolutionSpace<AddableInteger, U> space) {
		return this.join(new UtilitySolutionSpace[] { space });
	}

	/** @see UtilitySolutionSpace#join(UtilitySolutionSpace[]) */
	public UtilitySolutionSpace<AddableInteger, U> join(
			UtilitySolutionSpace<AddableInteger, U>[] spaces) {


		// Build the lists of constraints, utility variables, and projected variables
		ArrayList<Constraint> newConst = new ArrayList<Constraint> ();
		ArrayList< DecomposedConstraint<Constraint> > newDecomp = new ArrayList< DecomposedConstraint<Constraint> > ();
		ArrayList<IntVarCloneable> newUtilVars = new ArrayList<IntVarCloneable> ();
		HashMap<String, AddableInteger[]> newAllVars = new HashMap<String, AddableInteger[]> (this.allDoms);
		HashSet<IntVarCloneable> newVars = new HashSet<IntVarCloneable>();
		HashSet<IntVarCloneable> newProjectedVars = new HashSet<IntVarCloneable>();
		HashSet<IntVarCloneable> newSlicedVars = new HashSet<IntVarCloneable>();

		// Go through all spaces
		ArrayList< UtilitySolutionSpace<AddableInteger, U> > allSpaces = new ArrayList< UtilitySolutionSpace<AddableInteger, U> > (spaces.length + 1);
		allSpaces.add(this);
		allSpaces.addAll(Arrays.asList(spaces));
		U newDefaultUtil = this.defaultUtil.getZero();
		for (UtilitySolutionSpace<AddableInteger, U> space : allSpaces) {

			if(space instanceof ScalarHypercube<?,?>) 
				newDefaultUtil = newDefaultUtil.add(((ScalarHypercube<AddableInteger,U>)space).getUtility(0));
			
			else if (space instanceof Hypercube<?, ?>) {
				return this.toHypercube().join(spaces);
				
				// The following is less efficient in practice
//				JaCoPutilSpace<U> spaceCast = new JaCoPutilSpace<U> ((Hypercube<AddableInteger, U>) space, this.maximize, this.infeasibleUtil);
//				newConst.addAll(spaceCast.constraints);
//				newUtilVars.addAll(spaceCast.utilVars);
//				newAllVars.putAll(spaceCast.allDoms);
//				newVars.addAll(Arrays.asList(spaceCast.vars));
			
			} else { // must be a JaCoPutilSpace

				// First cast the space to a JaCoPutilSpace
				assert space instanceof JaCoPutilSpace<?> : "Unsupported space type: " + space.getClass().getName();
				JaCoPutilSpace<U> spaceCast = null;
				try {
					spaceCast = (JaCoPutilSpace<U>) space;
				} catch (ClassCastException e) { }
				
				/// @bug If the current space is supposed to project out any variable contained in another space, 
				/// it must do so before the join is performed, by calling resolve(). This should normally never happen, though. 
				assert Collections.disjoint(Arrays.asList(this.projectedVars), Arrays.asList(spaceCast.vars));
				
				newDefaultUtil = newDefaultUtil.add(spaceCast.defaultUtil);
				newConst.addAll(spaceCast.constraints);
				newDecomp.addAll(spaceCast.decompConstraints);
				newUtilVars.addAll(spaceCast.utilVars);
				newAllVars.putAll(spaceCast.allDoms);
				newVars.addAll(Arrays.asList(spaceCast.vars));
				newProjectedVars.addAll(Arrays.asList(spaceCast.projectedVars));
				newSlicedVars.addAll(Arrays.asList(spaceCast.slicedVars));
			}
		}
		
		assert Collections.disjoint(newProjectedVars, newVars);

		// Joining only ScalarHypercube results in a ScalarHypercube
		if(newAllVars.size() == 0) 
			return new ScalarHypercube<AddableInteger, U>(newDefaultUtil, this.infeasibleUtil, new AddableInteger [0].getClass());
		 else 
			return new JaCoPutilSpace<U> ("joined" + new Object().hashCode(), newConst, newDecomp, newUtilVars, newAllVars,
					newVars.toArray(new IntVarCloneable[newVars.size()]), newProjectedVars.toArray(new IntVarCloneable[newProjectedVars.size()]),
					newSlicedVars.toArray(new IntVarCloneable[newSlicedVars.size()]), this.maximize, newDefaultUtil, this.infeasibleUtil);
	}

	/** @see UtilitySolutionSpace#joinMinNCCCs(UtilitySolutionSpace) */
	public UtilitySolutionSpace<AddableInteger, U> joinMinNCCCs(
			UtilitySolutionSpace<AddableInteger, U> space) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#joinMinNCCCs(UtilitySolutionSpace[]) */
	public UtilitySolutionSpace<AddableInteger, U> joinMinNCCCs(
			UtilitySolutionSpace<AddableInteger, U>[] spaces) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#multiply(UtilitySolutionSpace, java.lang.String[]) */
	public UtilitySolutionSpace<AddableInteger, U> multiply(
			UtilitySolutionSpace<AddableInteger, U> space,
			String[] totalVariables) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#multiply(UtilitySolutionSpace) */
	public UtilitySolutionSpace<AddableInteger, U> multiply(
			UtilitySolutionSpace<AddableInteger, U> space) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#multiply(UtilitySolutionSpace[]) */
	public UtilitySolutionSpace<AddableInteger, U> multiply(
			UtilitySolutionSpace<AddableInteger, U>[] spaces) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#project(java.lang.String[], boolean) */
	public UtilitySolutionSpace.ProjOutput<AddableInteger, U> project(String[] vars, boolean maximum) {

		HashMap<String, AddableInteger[]> newAllVars = new HashMap<String, AddableInteger[]> (this.allDoms);
		IntVarCloneable[] newVars = new IntVarCloneable[this.vars.length - vars.length];
		IntVarCloneable[] newProjectedVars = Arrays.copyOf(this.projectedVars, this.projectedVars.length + vars.length);
		IntVarCloneable[] newSlicedVars = this.slicedVars;

		// move the projected vars from vars to projectedVars
		int i = 0;
		HashSet<String> inVars = new HashSet<String> (Arrays.asList(vars));
		for(IntVarCloneable v: this.vars){
			if(inVars.contains(v.id()))
				continue;
			newVars[i] = v;
			i++;
		}
		final int nbrVarsOut = vars.length;
		IntVarCloneable[] varsOut = new IntVarCloneable [nbrVarsOut];
		for (i = 0; i < nbrVarsOut; i++) 
			varsOut[i] = (IntVarCloneable) this.store.findVariable(vars[i]);
		System.arraycopy(varsOut, 0, newProjectedVars, this.projectedVars.length, vars.length);
		
		// For now (and maybe forever) we will assume that all variables are projected in the same way (all minimized or all maximized)
		assert (this.maximize == maximum) : "All variables must be projected the same way!";

		JaCoPutilSpace<U> outSpace = new JaCoPutilSpace<U> (this.getName() + "projected", this.constraints, this.decompConstraints, 
				this.utilVars, newAllVars, newVars, newProjectedVars, newSlicedVars, this.maximize, this.defaultUtil, this.infeasibleUtil);
		JaCoPoptAssignments assignments = new JaCoPoptAssignments (this, outSpace.vars, varsOut);

		ProjOutput<AddableInteger, U> out = new ProjOutput<AddableInteger, U> (outSpace, vars, assignments);
		
		return out;
	}

	/** @see UtilitySolutionSpace#project(int, boolean) */
	public UtilitySolutionSpace.ProjOutput<AddableInteger, U> project(
			int numberToProject, boolean maximum) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#project(java.lang.String, boolean) */
	public ProjOutput<AddableInteger, U> project(String variableName, boolean maximum) {

		HashMap<String, AddableInteger[]> newAllVars = new HashMap<String, AddableInteger[]> (this.allDoms);
		IntVarCloneable[] newVars = new IntVarCloneable[this.vars.length-1];
		IntVarCloneable[] newProjectedVars = Arrays.copyOf(this.projectedVars, this.projectedVars.length + 1);
		IntVarCloneable[] newSlicedVars = this.slicedVars;

		// move the projected var from vars to projectedVars
		int i = 0;
		for(IntVarCloneable v: this.vars){
			if(v.id().equals(variableName))
				continue;
			newVars[i] = v;
			i++;
		}
		IntVarCloneable varOut = (IntVarCloneable) this.store.findVariable(variableName);
		newProjectedVars[this.projectedVars.length] = varOut;
		
		// For now (and maybe forever) we will assume that all variables are projected in the same way (all minimized or all maximized)
		assert (this.maximize == maximum) : "All variables must be projected the same way!";

		JaCoPutilSpace<U> outSpace = new JaCoPutilSpace<U> (this.getName() + "projected", this.constraints, this.decompConstraints, 
				this.utilVars, newAllVars, newVars, newProjectedVars, newSlicedVars, this.maximize, this.defaultUtil, this.infeasibleUtil);
		JaCoPoptAssignments assignments = new JaCoPoptAssignments (this, outSpace.vars, new IntVarCloneable[] {varOut});

		ProjOutput<AddableInteger, U> out = new ProjOutput<AddableInteger, U> (outSpace, 
				new String[] {variableName}, 
				assignments);
		
		return out;
	}

	/** @see UtilitySolutionSpace#projectAll(boolean) */
	public UtilitySolutionSpace.ProjOutput<AddableInteger, U> projectAll(boolean maximum) {

		HashMap<String, AddableInteger[]> newAllVars = new HashMap<String, AddableInteger[]> (this.allDoms);
		IntVarCloneable[] newVars = new IntVarCloneable[0];
		IntVarCloneable[] newProjectedVars = Arrays.copyOf(this.projectedVars, this.projectedVars.length + this.vars.length);
		IntVarCloneable[] newSlicedVars = this.slicedVars;

		// move all variables from vars to projectedVars
		System.arraycopy(this.vars, 0, newProjectedVars, this.projectedVars.length, this.vars.length);

		// For now (and maybe forever) we will assume that all variables are projected in the same way (all minimized or all maximized)
		assert (this.maximize == maximum) : "All variables must be projected the same way!";

		JaCoPutilSpace<U> outSpace = new JaCoPutilSpace<U> (this.getName() + "projected", this.constraints, this.decompConstraints, 
				this.utilVars, newAllVars, newVars, newProjectedVars, newSlicedVars, this.maximize, this.defaultUtil, this.infeasibleUtil);
		JaCoPoptAssignments assignments = new JaCoPoptAssignments (this, outSpace.vars, this.vars);

		ProjOutput<AddableInteger, U> out = new ProjOutput<AddableInteger, U> (outSpace, 
				this.getVariables(), 
				assignments);

		return out;
	}

	/** @see UtilitySolutionSpace#projectAll(boolean, java.lang.String[]) */
	public UtilitySolutionSpace.ProjOutput<AddableInteger, U> projectAll(
			boolean maximum, String[] order) {

		HashMap<String, AddableInteger[]> newAllVars = new HashMap<String, AddableInteger[]> (this.allDoms);
		IntVarCloneable[] newVars = new IntVarCloneable[0];
		IntVarCloneable[] newSlicedVars = this.slicedVars;

		// Order the projected vars according to the input order
		IntVarCloneable[] newProjectedVars = new IntVarCloneable [this.projectedVars.length + this.vars.length];
		int i = 0;
		for (String inputVarName : order) {
			for (IntVarCloneable var : this.vars) { // search for the variable by its name
				if (var.id.equals(inputVarName)) {
					newProjectedVars[i++] = var;
					break;
				}
				
				assert false: "Input order includes unknown variable " + inputVarName;
			}
		}
		
		assert i == this.vars.length : "Input order does not include all variables";

		// move all variables from vars to projectedVars
		System.arraycopy(this.vars, 0, newProjectedVars, this.projectedVars.length, this.vars.length);

		// For now (and maybe forever) we will assume that all variables are projected in the same way (all minimized or all maximized)
		assert (this.maximize == maximum) : "All variables must be projected the same way!";
		

		JaCoPutilSpace<U> outSpace = new JaCoPutilSpace<U> (this.getName() + "projected", this.constraints, this.decompConstraints, 
				this.utilVars, newAllVars, newVars, newProjectedVars, newSlicedVars, this.maximize, this.defaultUtil, this.infeasibleUtil);
		JaCoPoptAssignments assignments = new JaCoPoptAssignments (this, newVars, newProjectedVars);

		ProjOutput<AddableInteger, U> out = new ProjOutput<AddableInteger, U> (outSpace, 
				order, 
				assignments);

		return out;
	}

	/** @see UtilitySolutionSpace#resolve() */
	@Override
	public UtilitySolutionSpace<AddableInteger, U> resolve() {	
		return this.resolve(true);
	}

	/** @see UtilitySolutionSpace#resolve(boolean) */
	@Override
	public UtilitySolutionSpace<AddableInteger, U> resolve(final boolean sparse) {	
	
		if(vars.length == 0){ // No variable, we construct a scalar hypercube
			U cost = this.getUtility(0);
			return new ScalarHypercube<AddableInteger, U>(cost, this.infeasibleUtil, AddableInteger[].class);
		}
		
		// Construct the output extensional space
		ArrayList<Constraint> outConstraints = new ArrayList<Constraint> (1);
		ArrayList<IntVarCloneable> outUtilVars = new ArrayList<IntVarCloneable> (1);
		JaCoPutilSpace<U> out = new JaCoPutilSpace<U> (null, outConstraints, new ArrayList< DecomposedConstraint<Constraint> > (0), outUtilVars, 
				this.allDoms, this.vars, new IntVarCloneable [0], new IntVarCloneable [0], this.maximize, this.defaultUtil, this.infeasibleUtil);
		
		// Initialize the domain of the output utility variable
		IntervalDomain utilDom = new IntervalDomain ();
		
		// Iterate over the solutions in this space 
		SparseIterator<AddableInteger, U> iter = sparse ? this.sparseIter() : this.iterator();
		AddableInteger[] sol;
		ArrayList< int[] > tuples = new ArrayList< int[] > ();
		int[] tuple;
		final int nbrVars = this.vars.length;
		int intVal;
		final U inf = this.infeasibleUtil;
		for (U util = iter.nextUtility(); util != null; util = iter.nextUtility()) {
			
			// Record the tuple for this solution
			if (sparse || ! inf.equals(util)) { // checks if the solution is feasible

				// Retrieve the tuple 
				sol = iter.getCurrentSolution();
				tuples.add(tuple = new int [nbrVars + 1]); // + 1 for the utility variable
				for (int i = nbrVars - 1; i >= 0; i--) 
					tuple[i] = sol[i].intValue();

				// Record the value of the utility variable
				utilDom.addDom(new IntervalDomain (tuple[nbrVars] = (intVal = util.intValue()), intVal));
			}
		}

		// Construct the utility variable
		IntVarCloneable utilVar = new IntVarCloneable (out.store, "util_" + out.store.id, utilDom);
		out.utilVars.add(utilVar);
		
		// Construct the extensional constraint
		IntVarCloneable[] allVars = new IntVarCloneable [nbrVars + 1]; // + 1 for the utility variable
		System.arraycopy(out.vars, 0, allVars, 0, nbrVars);
		allVars[nbrVars] = utilVar;
//		out.constraints.add(new ExtensionalSupportSTR (allVars, tuples.toArray(new int [tuples.size()][nbrVars + 1])));
		out.constraints.add(new ExtensionalSupportHypercube (out.store, allVars, tuples.toArray(new int [tuples.size()][nbrVars + 1])));
		
		return out;
	}

	/** Imposes the constraints and calls the consistency function 
	 * @return whether the resulting space is consistent 
	 */
	public boolean imposeConstraints () {
		
		try {
			// Impose the Constraints
			for (Constraint cons : this.constraints) 
				this.store.impose(cons);

			// Impose the DecomposedConstraints
			for (DecomposedConstraint<Constraint> cons : this.decompConstraints) 
				this.store.imposeDecomposition(cons);
			
		} catch (FailException e) {
			return this.isConsistent = false;
		}
		
		/// @todo The construction of the "util_total" variable can be improved for performance
		
		// Construct the domain for the sum variable
		/// @todo Properly compute the exact domain, not just a super-interval?
		int min = 0, max = 0;
		for (IntVarCloneable var : utilVars) {
			// It is possible that a relation cannot be satisfied. Hence the variable corresponding to its utility will have an empty domain
			if(!var.dom().isEmpty()){
				min += ((IntDomain)var.dom()).min();
				max += ((IntDomain)var.dom()).max();
			}
		}
				
		// Clone the util variables
		ArrayList<IntVarCloneable> clonedUtilVars = new ArrayList<IntVarCloneable> (this.utilVars.size());
		for (IntVarCloneable utilVar : this.utilVars) 
			if (! utilVar.dom().isEmpty()) // skip util variables with empty domains
				clonedUtilVars.add(this.store.findOrCloneInto(utilVar));

		IntervalDomain sumDom = new IntervalDomain (min, max);

		// If it is a minimization problem, the total cost is simply the sum of the utility variables.
		if(this.maximize == false){
			// Construct the sum variable
			this.utilVar = new IntVarCloneable (store, "util_total", sumDom);
			if (! clonedUtilVars.isEmpty()) 
				store.impose(new SumInt (clonedUtilVars, "==", utilVar)); /// @todo Use more efficient constraints depending on clonedUnitVars.size()
			
			// If it is a maximization problem, we need to create the total utility variable (that is the negation of the total cost variable)
			// to be able to convert the problem into a minimization one as JaCoP can only handle those.
		}else{
			// Construct the sum variable
			IntVarCloneable costVar = new IntVarCloneable (store, "cost_total", sumDom);
			if (! clonedUtilVars.isEmpty()) 
				store.impose(new SumInt (clonedUtilVars, "==", costVar));

			// @todo compute the exact opposite domain of sumDom
			IntervalDomain oppDom = new IntervalDomain(IntDomain.MinInt,IntDomain.MaxInt);
			if(min == 0 && max == 0){
				oppDom = new IntervalDomain (0, 0);
			}
			this.utilVar = new IntVarCloneable(store, "util_total", oppDom);
			store.impose(new XplusYeqC(costVar, this.utilVar, 0));
		}

		return this.isConsistent = this.store.consistency();
	}

	/** @see UtilitySolutionSpace#sample(int) */
	public Map<AddableInteger, Double> sample(int nbrSamples) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#slice(java.lang.String[], Addable[][]) */
	public UtilitySolutionSpace<AddableInteger, U> slice(
			String[] variablesNames, AddableInteger[][] subDomains) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#slice(java.lang.String[], Addable[]) */
	public UtilitySolutionSpace<AddableInteger, U> slice(
			String[] variablesNames, AddableInteger[] values) {
		
		HashMap<String, AddableInteger[]> newAllVars = new HashMap<String, AddableInteger[]>(this.allDoms);
		HashSet<IntVarCloneable> newVars = new HashSet<IntVarCloneable>(Arrays.asList(this.vars));
		ArrayList<IntVarCloneable> newSlicedVars = new ArrayList<IntVarCloneable>(Arrays.asList(this.slicedVars));
		
		AddableInteger[] domain;
		IntVarCloneable jacopVar;
		for(int i = 0 ; i < variablesNames.length; i++){
			if ((jacopVar = (IntVarCloneable) this.store.findVariable(variablesNames[i])) != null 
					&& newVars.remove(jacopVar)) {
				newSlicedVars.add(jacopVar);
				domain = new AddableInteger[1];
				domain[0] = values[i];
				newAllVars.put(variablesNames[i], domain);
			}
		}
		
		JaCoPutilSpace<U> out = new JaCoPutilSpace<U> ("sliced" + new Object().hashCode(), this.constraints, this.decompConstraints, 
				this.utilVars, newAllVars, newVars.toArray(new IntVarCloneable[newVars.size()]),Arrays.copyOf(this.projectedVars, this.projectedVars.length),
				newSlicedVars.toArray(new IntVarCloneable[newSlicedVars.size()]), this.maximize, this.defaultUtil, this.infeasibleUtil);
		
		return out;
	}

	/** @see UtilitySolutionSpace#slice(java.lang.String, Addable[]) */
	public UtilitySolutionSpace<AddableInteger, U> slice(
			String var, AddableInteger[] subDomain) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#slice(java.lang.String, Addable) */
	public UtilitySolutionSpace<AddableInteger, U> slice(
			String var, AddableInteger val) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#slice(Addable[]) */
	public UtilitySolutionSpace<AddableInteger, U> slice(
			AddableInteger[] variablesValues) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#split(Addable, boolean) */
	public UtilitySolutionSpace<AddableInteger, U> split(
			U threshold, boolean maximum) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see BasicUtilitySolutionSpace#augment(Addable[], java.io.Serializable) */
	public void augment(AddableInteger[] variablesValues,
			U utilityValue) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";

	}
	
	/** @see java.lang.Object#hashCode() */
	@Override
	public int hashCode () {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return 0;
	}
	
	/** @see java.lang.Object#equals(java.lang.Object) */
	@Override
	public boolean equals (Object o) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see BasicUtilitySolutionSpace#equivalent(BasicUtilitySolutionSpace) */
	public boolean equivalent(BasicUtilitySolutionSpace<AddableInteger, U> space) {
		
		if (space == null) 
			return false;
		
		Iterator<AddableInteger, U> myIter = this.iterator(this.getVariables(), this.getDomains());
		BasicUtilitySolutionSpace.Iterator<AddableInteger, U> otherIter = space.iterator(this.getVariables(), this.getDomains());
		
		if (myIter.getNbrSolutions() != otherIter.getNbrSolutions()) 
			return false;
		
		while (myIter.hasNext()) 
			if (! myIter.nextUtility().equals(otherIter.nextUtility())) 
				return false;
		
		return true;
	}

	/** @see BasicUtilitySolutionSpace#getClassOfU() */
	public Class<U> getClassOfU() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see BasicUtilitySolutionSpace#getDefaultUtility() */
	public U getDefaultUtility() {
		return this.infeasibleUtil.getZero();
	}

	/** @see BasicUtilitySolutionSpace#getUtility(Addable[]) */
	@SuppressWarnings("unchecked")
	public U getUtility(AddableInteger[] variablesValues) {
		
		// The input does not specify a value for each variable
		if(variablesValues.length < vars.length){
			return this.defaultUtil;
		}
		
		if(this.defaultUtil.equals(this.infeasibleUtil)){
			return this.infeasibleUtil;
		}

		// If this is a scalar space
		if(this.allDoms.size() == 0 // no variables
				|| (constraints.isEmpty() && this.decompConstraints.isEmpty()) ) { // no constraints
			return this.defaultUtil;
		}

		// If the constraints haven't been imposed yet, impose them now
		if (this.isConsistent == null) 
			this.imposeConstraints();
		
		if(!isConsistent){
			return this.infeasibleUtil;
		}

		int lvlReminder = store.level;

		// Change the store level to be able to ground the variable in a reversible manner
		store.setLevel(lvlReminder+1);
		for(int i = 0; i < variablesValues.length; i++){

			assert variablesValues[i] != null : "null value passed for variable " + this.vars[i];

			// We ground the variables in the separator
			try{
				IntVarCloneable var = (IntVarCloneable) store.findVariable(vars[i].id());
				
				// No need to ground the variable if it is already grounded to the desired value
				if (var.singleton() && var.value() == variablesValues[i].intValue()) 
					continue;
				
				var.domain.in(lvlReminder+1, var, variablesValues[i].intValue(), variablesValues[i].intValue());
			}catch (org.jacop.core.FailException e){

				for(int k = store.level; k > lvlReminder; k--){
					store.removeLevel(k);
				}

				store.setLevel(lvlReminder);

				return this.infeasibleUtil;				
			}
		}

		U costValue = this.infeasibleUtil;

		if(store.consistency()){
			
			if(this.projectedVars.length == 0){

				// Optimization search
				Search<IntVarCloneable> search = new DepthFirstSearch<IntVarCloneable> ();
				search.getSolutionListener().recordSolutions(true);
				search.setAssignSolution(false);

				// Debug information
				search.setPrintInfo(false);

				boolean result = search.labeling(store, 
						new SimpleSelect<IntVarCloneable> (new IntVarCloneable[] { this.utilVar }, 
								new SmallestDomain<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()));

				if(!result){
					// The solution given in argument is inconsistent!
					costValue = this.infeasibleUtil;
				}else{

					int cost = search.getSolution()[0].valueEnumeration().nextElement();

					// If it is a maximization problem
					if(this.maximize == true){
						cost *= -1;
					}

					try {
						costValue = (U) this.defaultUtil.getClass().getConstructor(int.class).newInstance(cost);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}


			}else{
				// Find the variables that were already projected (the ones whose solution values we want) in the store
				IntVarCloneable projectedVars[] = new IntVarCloneable[this.projectedVars.length];
				int n = 0;
				for(IntVarCloneable var: this.projectedVars){

					// Find the JaCoP variable
					projectedVars[n] = (IntVarCloneable) store.findVariable(var.id());
					assert projectedVars[n] != null: "Variable " + var.id() + " not found in the store!";
					n++;

				}
				
				// We add the utilVar to the list of searched variables, this is not mandatory but this will prevent JaCoP from crashing if there is no projected variable!
				//searchedVars[this.projectedVars.size()] = utilVar;

				// Optimization search
				Search<IntVarCloneable> search = new DepthFirstSearch<IntVarCloneable> ();
				search.getSolutionListener().recordSolutions(false);
				search.setAssignSolution(false);

				// Debug information
				search.setPrintInfo(false);
				
				boolean result = search.labeling(store, new SimpleSelect<IntVarCloneable> (projectedVars, 
						new SmallestDomain<IntVarCloneable>(), new MostConstrainedDynamic<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()), utilVar);	

				if(!result){
					// The solution given in argument is inconsistent!
					costValue = this.infeasibleUtil;
				}else{

					int cost = search.getCostValue();

					// If it is a maximization problem
					if(this.maximize == true){
						cost *= -1;
					}

					try {
						costValue = (U) this.defaultUtil.getClass().getConstructor(int.class).newInstance(cost);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}

		// Store backtrack
		for(int k = store.level; k > lvlReminder; k--){
			store.removeLevel(k);
		}

		store.setLevel(lvlReminder);
		
		return costValue.add(this.defaultUtil);

	}

	/** @see BasicUtilitySolutionSpace#getUtility(java.lang.String[], Addable[]) */
	public U getUtility(String[] variablesNames,
			AddableInteger[] variablesValues) {

		assert variablesNames.length >= this.vars.length;
		assert variablesNames.length == variablesValues.length;

		//Note: "variables_names" and "variables_values" may contain variables that are not present in this hypercube but must 
		//provide a value for each variable of this space otherwise a null is returned.

		AddableInteger[] assignment = new AddableInteger[vars.length];
		final int variables_size = variablesNames.length;
		final int variables_size2 = vars.length;

		// loop over all the variables present in the array "variablesNames"
		IntVarCloneable var;
		ext: for(int i = 0; i < variables_size2; i++){
			var = this.vars[i];
			for(int j = 0; j < variables_size; j++){
				if( var.id().equals(variablesNames[j])) {
					assignment[i] = variablesValues[j];
					continue ext;
				}
			}

			// No value found for variable i
			return this.infeasibleUtil.getZero();
		}

		return getUtility(assignment);
	}

	/** @see BasicUtilitySolutionSpace#getUtility(java.util.Map) */
	public U getUtility(Map<String, AddableInteger> assignments) {

		// obtain the correct values array that corresponds to the index
		AddableInteger[] values = new AddableInteger[vars.length];
		for(int i = 0; i < vars.length; i++){
			values[i] = assignments.get(vars[i].id());
			if(values[i] == null) {
				return this.infeasibleUtil.getZero();
			}
		}

		return getUtility(values);
	}

	/** @see BasicUtilitySolutionSpace#getUtility(long) */
	public U getUtility(long index) {

		// obtain the correct values array that corresponds to the index
		AddableInteger[] values = new AddableInteger[vars.length];
		AddableInteger[] domain;
		long location = this.getNumberOfSolutions();
		int indice;
		for(int i = 0; i < vars.length; i++){

			domain = allDoms.get(vars[i].id());
			location = location/domain.length;

			indice = (int) (index/location);
			index = index % location;

			values[i] = domain[indice];
		}
		
		return getUtility(values);
	}

	/** @see BasicUtilitySolutionSpace#isIncludedIn(BasicUtilitySolutionSpace) */
	public boolean isIncludedIn(
			BasicUtilitySolutionSpace<AddableInteger, U> space) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return false;
	}

	/** @see UtilitySolutionSpace#iterator() */
	@Override
	public UtilitySolutionSpace.Iterator<AddableInteger, U> iterator() {
		return (UtilitySolutionSpace.Iterator<AddableInteger, U>) this.iterator(false);
	}

	/** @see UtilitySolutionSpace#sparseIter() */
	@Override
	public UtilitySolutionSpace.SparseIterator<AddableInteger, U> sparseIter() {
		return this.iterator(true);
	}

	/** Returns an iterator
	 * @param sparse 	whether the iterator should skip infeasible solutions
	 * @return an iterator
	 */
	private UtilitySolutionSpace.SparseIterator<AddableInteger, U> iterator(final boolean sparse) {

		AddableInteger[][] variables_domain = new AddableInteger[vars.length][];

		for(int i = 0; i < vars.length; i++){
			variables_domain[i] = allDoms.get(vars[i].id());
		}
		
		if (this.defaultUtil.equals(this.infeasibleUtil)) 
			return new ScalarHypercube<AddableInteger, U> (this.infeasibleUtil, this.infeasibleUtil, AddableInteger[].class)
					.iterator(this.getVariables(), variables_domain);
		
		// Clone myself
		UtilitySolutionSpace<AddableInteger, U> clone = this.clone();
		
		if (NullHypercube.NULL.equals(clone)) 
			return new ScalarHypercube<AddableInteger, U> (this.infeasibleUtil, this.infeasibleUtil, AddableInteger[].class).iterator();
		else if (sparse) 
			return new JaCoPutilSpaceIter2<U> ((JaCoPutilSpace<U>) clone, this.getVariables(), variables_domain);
		else 
			return new JaCoPutilSpaceIter<U> ((JaCoPutilSpace<U>) clone, this.getVariables(), variables_domain);
	}

	/** @see UtilitySolutionSpace#iterator(java.lang.String[], Addable[][], Addable[]) */
	@Override
	public UtilitySolutionSpace.Iterator<AddableInteger, U> iterator(String[] variables, AddableInteger[][] domains, AddableInteger[] assignment) {
		return (UtilitySolutionSpace.Iterator<AddableInteger, U>) this.iterator(variables, domains, assignment, false);
	}
	
	/** @see UtilitySolutionSpace#sparseIter(java.lang.String[], Addable[][], Addable[]) */
	@Override
	public UtilitySolutionSpace.SparseIterator<AddableInteger, U> sparseIter(String[] variables, AddableInteger[][] domains, AddableInteger[] assignment) {
		return this.iterator(variables, domains, assignment, true);
	}
	
	/** Returns an iterator
	 * @param variables 	The variables to iterate over
	 * @param domains		The domains of the variables over which to iterate
	 * @param assignment 	An array that will be used as the output of nextSolution()
	 * @param sparse 		Whether to return an iterator that skips infeasible solutions
	 * @return an iterator which allows to iterate over the given variables and their utilities 
	 */
	private UtilitySolutionSpace.SparseIterator<AddableInteger, U> 
	iterator(String[] variables, AddableInteger[][] domains, AddableInteger[] assignment, final boolean sparse) {
		
		if (this.defaultUtil.equals(this.infeasibleUtil)) 
			return new ScalarHypercube<AddableInteger, U> (this.infeasibleUtil, this.infeasibleUtil, AddableInteger[].class)
					.iterator(variables, domains, assignment);

		// We want to allow the input list of variables not to contain all this space's variables
		int nbrInputVars = variables.length;
		ArrayList<String> vars = new ArrayList<String> (nbrInputVars);
		ArrayList<AddableInteger[]> doms = new ArrayList<AddableInteger[]> (nbrInputVars);

		// Go through the list of input variables
		for (int i = 0; i < nbrInputVars; i++) {

			// Record the variable
			String var = variables[i];
			vars.add(var);

			// Record the domain, as the intersection of the input domain with the space's domain, if any
			AddableInteger[] myDom = this.getDomain(var);
			if (myDom == null) // unknown variable
				doms.add(domains[i]);
			else 
				doms.add(BasicHypercube.intersection(myDom, domains[i]));

		}

		// Add the variables that are in this space and not in the input list
		int myNbrVars = this.vars.length;
		for (int i = 0; i < myNbrVars; i++) {
			String var = this.vars[i].id();
			if (! vars.contains(var)) {
				vars.add(var);
				doms.add(this.allDoms.get(var));
			}
		}

		int nbrVarsIter = vars.size();
		
		// Clone myself
		UtilitySolutionSpace<AddableInteger, U> clone = this.clone();
		
		if (NullHypercube.NULL.equals(clone)) 
			return new ScalarHypercube<AddableInteger, U> (this.infeasibleUtil, this.infeasibleUtil, AddableInteger[].class).iterator();
		else if (sparse) 
			return new JaCoPutilSpaceIter2<U> ((JaCoPutilSpace<U>) clone, vars.toArray(new String [nbrVarsIter]), 
					doms.toArray((AddableInteger[][]) Array.newInstance(domains.getClass().getComponentType(), nbrVarsIter)));
		else 
			return new JaCoPutilSpaceIter<U> ((JaCoPutilSpace<U>) clone, vars.toArray(new String [nbrVarsIter]), 
					doms.toArray((AddableInteger[][]) Array.newInstance(domains.getClass().getComponentType(), nbrVarsIter)));
	}

	/** @see BasicUtilitySolutionSpace#prettyPrint(java.io.Serializable) */
	public String prettyPrint(U ignoredUtil) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see BasicUtilitySolutionSpace#setDefaultUtility(java.io.Serializable) */
	public void setDefaultUtility(U utility) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";

	}

	/** @see BasicUtilitySolutionSpace#setInfeasibleUtility(java.io.Serializable) */
	public void setInfeasibleUtility(U utility) {
		this.infeasibleUtil = utility;
	}

	/** @see BasicUtilitySolutionSpace#setUtility(Addable[], java.io.Serializable) */
	public boolean setUtility(AddableInteger[] variablesValues, U utility) {
		/// @todo Not yet implemented
		assert false : "Not yet implemented";
		return false;
	}

	/** @see BasicUtilitySolutionSpace#setUtility(long, java.io.Serializable) */
	public void setUtility(long index, U utility) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";

	}

	/** @see SolutionSpace#augment(Addable[]) */
	public void augment(AddableInteger[] variablesValues) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";

	}

	/** @see SolutionSpace#getDomain(java.lang.String) */
	public AddableInteger[] getDomain(String variable) {
		
		// Only return a domain if the input variable is actually part of this space (and hasn't been projected out or sliced)
		for(IntVarCloneable v: this.vars) 
			if(v.id().equals(variable))
				return allDoms.get(variable);

		return null;
	}

	/** Returns an array of all possible values that the projected variable provided as a parameter 
	 * can take in this JaCoPutilSpace
	 * @param variable 		the name of the projected variable
	 * @return  			the projected variable's domain
	 */
	AddableInteger[] getProjVarDomain(String variable) {
		
		for(IntVarCloneable v: this.projectedVars) 
			if(v.id().equals(variable))
				return allDoms.get(variable);
		
		return null;
	}

	/** @see SolutionSpace#getDomain(int) */
	public AddableInteger[] getDomain(int index) {
		return allDoms.get(vars[index].id());
	}

	/** @see SolutionSpace#getDomain(java.lang.String, int) */
	public AddableInteger[] getDomain(String variable, int index) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see SolutionSpace#getDomains() */
	public AddableInteger[][] getDomains() {
		AddableInteger[][] domains = new AddableInteger[vars.length][];
		for(int i = 0; i < vars.length; i++){
			domains[i] = allDoms.get(vars[i].id());
		}
		return domains;
	}

	/** @see SolutionSpace#getIndex(java.lang.String) */
	public int getIndex(String variable) {
		for (int i = this.vars.length - 1; i >= 0; i--) 
			if (this.vars[i].id().equals(variable)) 
				return i;
		return -1;
	}

	/** @see SolutionSpace#getName() */
	public String getName() {
		return this.name;
	}

	/** @see SolutionSpace#getOwner() */
	public String getOwner() {
		return owner;
	}

	/** @see SolutionSpace#setOwner(java.lang.String) */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/** @see SolutionSpace#getNumberOfSolutions() */
	public long getNumberOfSolutions() {
		long nbrUtils = 1;

		for(IntVarCloneable var: this.vars){
			if (Math.log(nbrUtils) + Math.log(allDoms.get(var.id()).length) >= Math.log(Long.MAX_VALUE)) 
				throw new OutOfMemoryError ("Long overflow: too many solutions in an explicit space");
			nbrUtils *= allDoms.get(var.id()).length;
		}
		return nbrUtils;
	}

	/** @see SolutionSpace#getNumberOfVariables() */
	public int getNumberOfVariables() {
		return this.vars.length;
	}

	/** @see SolutionSpace#getVariable(int) */
	public String getVariable(int index) {
		return vars[index].id();
	}

	/** @see SolutionSpace#getVariables() */
	public String[] getVariables() {
		
		/// @todo Consider storing this in the space to avoid recomputing it every time
		String[] outVars = new String [this.vars.length];
		for (int i = this.vars.length - 1; i >= 0; i--) 
			outVars[i] = this.vars[i].id();
		
		return outVars;
	}

	/** @see UtilitySolutionSpace#iterator(java.lang.String[]) */
	@Override
	public UtilitySolutionSpace.Iterator<AddableInteger, U> iterator(String[] order) {
		return (UtilitySolutionSpace.Iterator<AddableInteger, U>) this.iterator(order, false);
	}
	
	/** @see UtilitySolutionSpace#sparseIter(java.lang.String[]) */
	@Override
	public UtilitySolutionSpace.SparseIterator<AddableInteger, U> sparseIter(String[] order) {
		return this.iterator(order, true);
	}
	
	/** Returns an iterator with a specific variable order
	 * @param order 	the order of iteration of the variables
	 * @param sparse 	whether to return an iterator that skips infeasible solutions
	 * @return 			an iterator which can be used to iterate through solutions 
	 * @warning The input array of variables must contain exactly all of the space's variables. 
	 */
	private UtilitySolutionSpace.SparseIterator<AddableInteger, U> iterator(String[] order, final boolean sparse) {

		AddableInteger[][] variables_domain = new AddableInteger[vars.length][];
		for(int i = 0; i < vars.length; i++){
			variables_domain[i] = allDoms.get(order[i]);
		}

		if (this.defaultUtil.equals(this.infeasibleUtil)) 
			return new ScalarHypercube<AddableInteger, U> (this.infeasibleUtil, this.infeasibleUtil, AddableInteger[].class)
					.iterator(order, variables_domain);
		else if (sparse) 
			return this.clone().sparseIter(order, variables_domain);
		else 
			return this.clone().iterator(order, variables_domain);
	}

	/** @see SolutionSpace#join(SolutionSpace, java.lang.String[]) */
	public SolutionSpace<AddableInteger> join(
			SolutionSpace<AddableInteger> space, String[] totalVariables) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see SolutionSpace#join(SolutionSpace) */
	public SolutionSpace<AddableInteger> join(
			SolutionSpace<AddableInteger> space) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see SolutionSpace#join(SolutionSpace[], java.lang.String[]) */
	public SolutionSpace<AddableInteger> join(
			SolutionSpace<AddableInteger>[] spaces, String[] totalVariablesOrder) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see SolutionSpace#join(SolutionSpace[]) */
	public SolutionSpace<AddableInteger> join(
			SolutionSpace<AddableInteger>[] spaces) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see SolutionSpace#knows(java.lang.Class) */
	public boolean knows(Class<?> spaceClass) {
		return knownSpaces.contains(spaceClass);
	}

	/** @see SolutionSpace#renameVariable(java.lang.String, java.lang.String) */
	public void renameVariable(String oldName, String newName) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";

	}

	/** @see SolutionSpace#setDomain(java.lang.String, Addable[]) */
	public void setDomain(String var, AddableInteger[] dom) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";

	}

	/** @see SolutionSpace#setName(java.lang.String) */
	public void setName(String name) {
		this.name = name;
	}

	/** @see UtilitySolutionSpace#blindProject(java.lang.String, boolean) */
	public UtilitySolutionSpace<AddableInteger, U> blindProject(String varOut, boolean maximize) {
		
		HashMap<String, AddableInteger[]> newAllVars = new HashMap<String, AddableInteger[]> (this.allDoms);
		HashSet<IntVarCloneable> newVars = new HashSet<IntVarCloneable>(Arrays.asList(this.vars));
		ArrayList<IntVarCloneable> newProjectedVars = new ArrayList<IntVarCloneable>(Arrays.asList(this.projectedVars));
		IntVarCloneable newSlicedVars[] = this.slicedVars;

		// move the projected vars from vars to projectedVars
		IntVarCloneable jacopVarOut = (IntVarCloneable) this.store.findVariable(varOut);
		if (jacopVarOut != null && newVars.remove(jacopVarOut)) 
			newProjectedVars.add(jacopVarOut);

		// For now (and maybe forever) we will assume that all variables are projected in the same way (all minimized or all maximized)
		assert (this.maximize == maximize) : "All variables must be projected the same way!";

		return new JaCoPutilSpace<U> (this.getName() + "projected", this.constraints, this.decompConstraints, this.utilVars, newAllVars,
				newVars.toArray(new IntVarCloneable[newVars.size()]), newProjectedVars.toArray(new IntVarCloneable[newProjectedVars.size()]),
				newSlicedVars, this.maximize, this.defaultUtil, this.infeasibleUtil);
	}

	/** @see UtilitySolutionSpace#blindProject(java.lang.String[], boolean) */
	public UtilitySolutionSpace<AddableInteger, U> blindProject(String[] varsOut, boolean maximize) {
		
		HashMap<String, AddableInteger[]> newAllVars = new HashMap<String, AddableInteger[]> (this.allDoms);
		HashSet<IntVarCloneable> newVars = new HashSet<IntVarCloneable>(Arrays.asList(this.vars));
		ArrayList<IntVarCloneable> newProjectedVars = new ArrayList<IntVarCloneable>(Arrays.asList(this.projectedVars));
		IntVarCloneable newSlicedVars[] = this.slicedVars;

		// move the projected vars from vars to projectedVars
		IntVarCloneable jacopVarOut;
		for(int i = varsOut.length - 1; i >= 0; i--) 
			if ((jacopVarOut = (IntVarCloneable) this.store.findVariable(varsOut[i])) != null && newVars.remove(jacopVarOut)) 
				newProjectedVars.add(jacopVarOut);

		// For now (and maybe forever) we will assume that all variables are projected in the same way (all minimized or all maximized)
		assert (this.maximize == maximize) : "All variables must be projected the same way!";

		return new JaCoPutilSpace<U> (this.getName() + "projected", this.constraints, this.decompConstraints, this.utilVars, newAllVars,
				newVars.toArray(new IntVarCloneable[newVars.size()]), newProjectedVars.toArray(new IntVarCloneable[newProjectedVars.size()]),
				newSlicedVars, this.maximize, this.defaultUtil, this.infeasibleUtil);
	}

	/** @see UtilitySolutionSpace#blindProjectAll(boolean) */
	public U blindProjectAll(boolean maximize) {
		return this.blindProject(this.getVariables(), maximize).getUtility(0);
	}

	/** @see UtilitySolutionSpace#iteratorBestFirst(boolean, java.lang.String[], Addable[]) */
	public IteratorBestFirst<AddableInteger, U> iteratorBestFirst(
			boolean maximize, String[] fixedVariables,
			AddableInteger[] fixedValues) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#max(java.lang.String) */
	public UtilitySolutionSpace<AddableInteger, U> max(
			String variable) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#min(java.lang.String) */
	public UtilitySolutionSpace<AddableInteger, U> min(
			String variable) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see SolutionSpace#getRelationName() */
	public String getRelationName() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see SolutionSpace#renameAllVars(java.lang.String[]) */
	public SolutionSpace<AddableInteger> renameAllVars(String[] newVarNames) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see SolutionSpace#setRelationName(java.lang.String) */
	public void setRelationName(String name) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";

	}

	/** @see UtilitySolutionSpace#projExpectMonotone(java.lang.String, java.util.Map, boolean) */
	public UtilitySolutionSpace.ProjOutput<AddableInteger, U> projExpectMonotone(String varOut, Map< String, UtilitySolutionSpace<AddableInteger, U> > distributions, boolean maximum) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	return null;
	}

	/** @see UtilitySolutionSpace#setProblem(ProblemInterface) */
	public void setProblem(ProblemInterface<AddableInteger, ?> problem) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";

	}

	/** @see UtilitySolutionSpace#countsCCs() */
	@Override
	public boolean countsCCs () {
		return false;
	}

	/**
	 * @return the store associated to this solution space
	 */
	public StoreCloneable getStore() {
		return store;
	}

	/**
	 * @return all the variables of this solution space whose projection has been requested
	 */
	IntVarCloneable[] getProjectedVars() {
		return projectedVars;
	}
	
	/** @return the sliced variables */
	IntVarCloneable[] getSlicedVars() {
		return this.slicedVars;
	}

	/** 
	 * @return true if it is a maximization problem, false if it is a minimization problem.
	 */
	public boolean maximize() {
		return maximize;
	}

	/** @return a hypercube corresponding to this utility space */
	public Hypercube<AddableInteger, U> toHypercube(){
		// ScalarHypercube
		if(this.vars.length == 0){
			return new ScalarHypercube<AddableInteger, U>(this.getUtility(0), this.infeasibleUtil, new AddableInteger [0].getClass());
		}else{

			final int nbrVars = vars.length;
			String[] outVars = new String [nbrVars];
			int nbrUtils = 1;
			AddableInteger[][] doms = new AddableInteger[nbrVars][];

			int n = 0;
			AddableInteger[] dom;
			for(IntVarCloneable var: this.vars){
				dom = allDoms.get(outVars[n] = var.id());
				if (Math.log(nbrUtils) + Math.log(dom.length) >= Math.log(Integer.MAX_VALUE)) 
					throw new OutOfMemoryError ("Integer overflow: too many solutions in an explicit space");
				nbrUtils *= dom.length;
				doms[n] = dom;
				n++;
			}

			@SuppressWarnings("unchecked")
			U[] utils = (U[]) Array.newInstance(this.defaultUtil.getClass(), nbrUtils);
			Arrays.fill(utils, this.infeasibleUtil);

			Hypercube<AddableInteger, U> out = new Hypercube<AddableInteger, U> (outVars, doms, utils, this.infeasibleUtil);

			final long nbrSol = this.getNumberOfSolutions();
			assert nbrSol < Integer.MAX_VALUE : "A hypercube can only contain up to " + Integer.MAX_VALUE + " solutions";

			Iterator<AddableInteger, U> iter = this.iterator(outVars, doms);
			for (long i = 0; i < nbrSol; i++){
				out.setUtility(i, iter.nextUtility());
			}

			/// @todo Improve performance by using a sparse iterator? 
//			assert this.getNumberOfSolutions() < Integer.MAX_VALUE : "A hypercube can only contain up to " + Integer.MAX_VALUE + " solutions";
//
//			SparseIterator<AddableInteger, U> iter = this.sparseIter(outVars, doms);
//			U util = null;
//			while ((util = iter.nextUtility()) != null) 
//				out.setUtility(iter.getCurrentSolution(), util);

			return out;
		}
	}

	/** @see UtilitySolutionSpace#iterator(java.lang.String[], Addable[][]) */
	@Override
	public Iterator<AddableInteger, U> iterator(String[] variables, AddableInteger[][] domains) {
		return this.iterator(variables, domains, (AddableInteger[]) Array.newInstance(AddableInteger.class, variables.length));
	}
	
	/** @see UtilitySolutionSpace#sparseIter(java.lang.String[], Addable[][]) */
	@Override
	public SparseIterator<AddableInteger, U> sparseIter(String[] variables, AddableInteger[][] domains) {
		return this.sparseIter(variables, domains, (AddableInteger[]) Array.newInstance(AddableInteger.class, variables.length));
	}
	
	/** @see UtilitySolutionSpace#rescale(Addable, Addable) */
	@Override
	public UtilitySolutionSpace<AddableInteger, U> rescale(U add, U multiply) {
		
		// Clone myself
		UtilitySolutionSpace<AddableInteger, U> clone = this.clone();
		if (NullHypercube.NULL.equals(clone)) 
			return new ScalarHypercube<AddableInteger, U> (this.infeasibleUtil, this.infeasibleUtil, AddableInteger[].class);
		
		JaCoPutilSpace<U> out = (JaCoPutilSpace<U>) clone;
		out.defaultUtil = out.defaultUtil.multiply(multiply).add(add);
		out.infeasibleUtil = out.infeasibleUtil.multiply(multiply).add(add);
		
		// No need to multiply by 1 = 1 * 1
		if (! multiply.equals(multiply.getZero()) 
				&& multiply.multiply(multiply).equals(multiply)) 
			return out;
		
		if (! multiply.abs().equals(multiply)) // multiplying by a negative number
			out.maximize = ! out.maximize;
		
		// Sum up the utility variables, then multiply by multiply
		switch (this.utilVars.size()) {
		
		case 0:
			break;
			
		case 1: // X * multiply = Z
			IntVarCloneable util = out.store.findOrCloneInto(this.utilVars.get(0));
			
			// Rescale the domain
			int min2, max2 = 0;
			if (multiply.abs().equals(multiply)) { // multiplying by a positive number
				min2 = util.min() * multiply.intValue();
				max2 = util.max() * multiply.intValue();
			} else {
				max2 = util.min() * multiply.intValue();
				min2 = util.max() * multiply.intValue();
			}
			
			IntVarCloneable util2 = new IntVarCloneable (out.store, util.id() + "_rescaled", min2, max2);
			out.utilVars.set(0, util2);
			
			// Add the constraint util * multiply = util2
			out.constraints.add(new XmulCeqZCloneable (util, multiply.intValue(), util2));
			break;
			
		case 2: // ((X + Y) * multiply)
			/// @todo Not implemented
			assert false : "TBC";
			break;
			
		case 3: // ((X + Y + Q) * multiply)
			/// @todo Not implemented
			assert false : "TBC";
			break;
			
		default: // (Sum(Xi) * multiply)
			/// @todo Not implemented
			assert false : "TBC";
			break;			
		}
		
		return out;
	}
}
