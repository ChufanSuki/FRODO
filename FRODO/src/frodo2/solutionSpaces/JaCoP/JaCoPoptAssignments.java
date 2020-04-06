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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jacop.core.IntVarCloneable;
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
import frodo2.solutionSpaces.hypercube.BasicHypercube;
import frodo2.solutionSpaces.hypercube.ScalarBasicHypercube;
import frodo2.solutionSpaces.hypercube.ScalarSpaceIter;

/** Conditional optimal assignment(s) to one or more variable(s)
 * @author Thomas Leaute
 */
public class JaCoPoptAssignments implements BasicUtilitySolutionSpace< AddableInteger, ArrayList<AddableInteger> > {

	/** Used for serialization */
	private static final long serialVersionUID = 2713289252460722989L; 
	
	/** The space from which variables were projected out */
	private final JaCoPutilSpace<?> space;
	
	/** The variables in the separator */
	private final IntVarCloneable[] vars;
	
	/** The projected variables for which we want to search the values given an assignment */
	private final IntVarCloneable[] projectedVars;
	
	/** The variable names, including the projected out and sliced out variables, but excluding the utility variable */
	HashMap<String, AddableInteger[]> allDoms;

	/** Constructor 
	 * @param space 	The space from which variables were projected out
	 * @param vars 		The variables in the separator
	 * @param varsOut 	The variables that were projected
	 */
	JaCoPoptAssignments (JaCoPutilSpace<?> space, IntVarCloneable[] vars, IntVarCloneable[] varsOut) {
		this.space = space;
		this.vars = vars;
		this.projectedVars = varsOut;
		this.allDoms = space.allDoms;
	}
	
	/** @see java.lang.Object#toString() */
	@Override
	public String toString () {
		return "JaCoPoptAssignments" +
				"\n\t vars: " + Arrays.toString(this.vars) +
				"\n\t proj: " + Arrays.toString(this.projectedVars) +
				"\n\t space: " + this.space;
	}

	/** @see BasicUtilitySolutionSpace#augment(Addable[], java.io.Serializable) */
	public void augment(AddableInteger[] variablesValues,
			ArrayList<AddableInteger> utilityValue) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see BasicUtilitySolutionSpace#changeVariablesOrder(java.lang.String[]) */
	public BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> changeVariablesOrder(
			String[] variablesOrder) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see Object#clone() */
	public BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> clone() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#compose(java.lang.String[], BasicUtilitySolutionSpace) */
	public BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> compose(
			String[] vars,
			BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> substitution) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#equivalent(BasicUtilitySolutionSpace) */
	public boolean equivalent(
			BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> space) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see BasicUtilitySolutionSpace#getClassOfU() */
	public Class<ArrayList<AddableInteger>> getClassOfU() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#getDefaultUtility() */
	public ArrayList<AddableInteger> getDefaultUtility() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#getUtility(Addable[]) */
	public ArrayList<AddableInteger> getUtility(AddableInteger[] variablesValues) {
		
		// The input does not specify a value for each variable
		if(vars.length != 0 && variablesValues.length < vars.length){ ///@todo the condition vars.length != 0 is necessary as we do not use explicit ScalarHypercube
			return null;
		}
		
		ArrayList<AddableInteger> out = new ArrayList<AddableInteger>();
		
		// If the constraints haven't been imposed yet, do it now
		if (this.space.isConsistent == null) 
			this.space.imposeConstraints();
		
		// The problem is infeasible, we just choose any assignment to each projected variable
		StoreCloneable store = this.space.store;
		if(!this.space.isConsistent){
			
			AddableInteger[] dom;
			for(int i = 0; i < projectedVars.length; i++){
				// We take the first value of the domain
				dom = space.getDomain(projectedVars[i].id());
				if(dom != null){
					out.add(this.space.getDomain(projectedVars[i].id())[0]);
				}else{
					out.add(this.space.getProjVarDomain(projectedVars[i].id())[0]);
				}
			}
			
			return out;
			
		}

		int lvlReminder = store.level;
		
		// Change the store level to be able to ground variables in a reversible manner
		store.setLevel(lvlReminder+1);
		
		for(int i = 0; i < vars.length; i++){
			IntVarCloneable var = (IntVarCloneable) store.findVariable(this.vars[i].id);
			assert var != null: "Variable " + this.vars[i].id + " not found in the store!";
			
			// We ground the variables in the separator
			try{
				var.domain.in(store.level, var, variablesValues[i].intValue(), variablesValues[i].intValue());
				
			}catch (org.jacop.core.FailException e){
				
				for(int k = store.level; k > lvlReminder; k--){
					store.removeLevel(k);
				}
				
				store.setLevel(lvlReminder);
			
				// The problem is infeasible, we just choose any assignment to each projected variable
				
				AddableInteger[] dom;
				for(int j = 0; j < projectedVars.length; j++){
					// We take the first value of the domain
					dom = space.getDomain(projectedVars[j].id());
					if(dom != null){
						out.add(this.space.getDomain(projectedVars[j].id())[0]);
					}else{
						out.add(this.space.getProjVarDomain(projectedVars[j].id())[0]);
					}
				}
				
				return out;	
			}
		}
		
		// Search over the projected variables 
		IntVarCloneable[] searchedVars = new IntVarCloneable[projectedVars.length + space.getProjectedVars().length];
		int n = 0;
		for(IntVarCloneable var : projectedVars){
			// Find the JaCoP variable
			searchedVars[n] = (IntVarCloneable) store.findVariable(var.id());
			assert searchedVars[n] != null: "Variable " + var.id() + " not found in the store!";
			n++;
		}
		
		for(IntVarCloneable var : space.getProjectedVars()) {
			// Find the JaCoP variable
			searchedVars[n] = (IntVarCloneable) store.findVariable(var.id());
			assert searchedVars[n] != null: "Variable " + var.id() + " not found in the store!";
			n++;
		}

		IntVarCloneable utilVar = (IntVarCloneable) store.findVariable("util_total");
		assert utilVar != null: "Variable " + "util_total" + " not found in the store!";

				
		// Optimization search
		Search<IntVarCloneable> search = new DepthFirstSearch<IntVarCloneable> ();
		search.getSolutionListener().recordSolutions(true);
			
		// Debug information
		search.setPrintInfo(false);
			
		boolean result = search.labeling(store, new SimpleSelect<IntVarCloneable> (searchedVars, 
				new SmallestDomain<IntVarCloneable>(), new MostConstrainedDynamic<IntVarCloneable>(), new IndomainMin<IntVarCloneable>()), utilVar);	
			
		if(!result){
			// The problem is infeasible, we can choose any assignment to each projected variable
			
			AddableInteger[] dom;
			for(int i = 0; i < projectedVars.length; i++){
				// We take the first value of the domain
				dom = space.getDomain(projectedVars[i].id());
				if(dom != null){
					out.add(this.space.getDomain(projectedVars[i].id())[0]);
				}else{
					out.add(this.space.getProjVarDomain(projectedVars[i].id())[0]);
				}
			}
			
			// Revert the store level
			for(int k = store.level; k > lvlReminder; k--){
				store.removeLevel(k);
			}
			store.setLevel(lvlReminder);

			return out;
			
		}
		
		for (int j=0; j < (projectedVars.length); j++){
			assert search.getSolution()[j].singleton(): "In a solution, all the variables must be grounded";
			out.add(new AddableInteger(search.getSolution()[j].valueEnumeration().nextElement()));
		}
		
		
		// Store backtrack
		for(int k = store.level; k > lvlReminder; k--){
			store.removeLevel(k);
		}
		
		store.setLevel(lvlReminder);

		return out;
		
	}

	/** @see BasicUtilitySolutionSpace#getUtility(java.lang.String[], Addable[]) */
	public ArrayList<AddableInteger> getUtility(String[] variablesNames,
			AddableInteger[] variablesValues) {
		
		AddableInteger[] assignment = null;
		
		if(vars.length == 0){
			return getUtility(assignment);
		}
		
		assert variablesNames.length >= this.vars.length;
		assert variablesNames.length == variablesValues.length;
		
		//Note: "variables_names" and "variables_values" may contain variables that are not present in this hypercube but must 
		//provide a value for each variable of this space otherwise a null is returned.

		assignment = new AddableInteger[vars.length];
		final int variables_size = variablesNames.length;
		final int variables_size2 = vars.length;

		// loop over all the variables present in the array "variablesNames"
		String var;
		ext: for(int i = 0; i < variables_size2; i++){
			var = this.vars[i].id();
			for(int j = 0; j < variables_size; j++){
				if( var.equals(variablesNames[j])) {
					assignment[i] = variablesValues[j];
					continue ext;
				}
			}

			// No value found for variable i
			return null;
		}

		return getUtility(assignment);
		
	}

	/** @see BasicUtilitySolutionSpace#getUtility(java.util.Map) */
	public ArrayList<AddableInteger> getUtility(Map<String, AddableInteger> assignments) {
		/// @todo Auto-generated method stub
		assert false : "Not implemented";
		return null;
	}
	
	/** @see BasicUtilitySolutionSpace#getUtility(long) */
	public ArrayList<AddableInteger> getUtility(long index) {

		// obtain the correct values array that corresponds to the index
		AddableInteger[] values = new AddableInteger[vars.length];
		AddableInteger[] domain;
		long location = this.getNumberOfSolutions();
		int indice;
		for(int i = 0; i < vars.length; i++){

			domain = allDoms.get(vars[i].id());
			location = location/domain.length;

			assert index/location < Integer.MAX_VALUE : "Integer overflow";
			indice = (int) (index/location);
			index = index % location;

			values[i] = domain[indice];
		}
		return getUtility(values);
	}

	/** @see BasicUtilitySolutionSpace#isIncludedIn(BasicUtilitySolutionSpace) */
	public boolean isIncludedIn(
			BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> space) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see BasicUtilitySolutionSpace#iterator() */
	public BasicUtilitySolutionSpace.Iterator<AddableInteger, ArrayList<AddableInteger>> iterator() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#iterator(java.lang.String[], Addable[][]) */
	public BasicUtilitySolutionSpace.Iterator<AddableInteger, ArrayList<AddableInteger>> iterator(
			String[] variables, AddableInteger[][] domains) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}


	/** @see BasicUtilitySolutionSpace#iterator(java.lang.String[], Addable[][], Addable[]) */
	public Iterator<AddableInteger, ArrayList<AddableInteger>> iterator(
			String[] variables, AddableInteger[][] domains,
			AddableInteger[] assignment) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#sparseIter() */
	@Override
	public SparseIterator<AddableInteger, ArrayList<AddableInteger>> sparseIter() {
		/// @todo Auto-generated method stub
		assert false : "Not implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#sparseIter(java.lang.String[]) */
	@Override
	public SparseIterator<AddableInteger, ArrayList<AddableInteger>> sparseIter(
			String[] order) {
		/// @todo Auto-generated method stub
		assert false : "Not implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#sparseIter(java.lang.String[], Addable[][]) */
	@Override
	public SparseIterator<AddableInteger, ArrayList<AddableInteger>> sparseIter(
			String[] variables, AddableInteger[][] domains) {
		/// @todo Auto-generated method stub
		assert false : "Not implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#sparseIter(java.lang.String[], Addable[][], Addable[]) */
	@Override
	public SparseIterator<AddableInteger, ArrayList<AddableInteger>> sparseIter(
			String[] variables, AddableInteger[][] domains,
			AddableInteger[] assignment) {
		/// @todo Auto-generated method stub
		assert false : "Not implemented";
		return null;
	}
		/** @see BasicUtilitySolutionSpace#prettyPrint(java.io.Serializable) */
	public String prettyPrint(ArrayList<AddableInteger> ignoredUtil) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#resolve() */
	@Override
	public BasicUtilitySolutionSpace< AddableInteger, ArrayList<AddableInteger> > resolve() {
		return this.resolve(true);
	}
	
	/** @see BasicUtilitySolutionSpace#resolve(boolean) */
	@Override
	public BasicUtilitySolutionSpace< AddableInteger, ArrayList<AddableInteger> > resolve(boolean unused) {
		
		if (this.vars.length == 0) 
			return new ScalarBasicHypercube< AddableInteger, ArrayList<AddableInteger> > (this.getUtility(0), null);
		
		// Compute the utilities for all combinations of assignments to the variables
		assert this.getNumberOfSolutions() < Integer.MAX_VALUE : "Cannot resolve a space that contains more than 2^32 solutions";
		@SuppressWarnings("unchecked")
		ArrayList<AddableInteger>[] utilities = new ArrayList [(int) this.getNumberOfSolutions()];
		int i = 0;
		for (ScalarSpaceIter<AddableInteger, AddableInteger> iter = 
				new ScalarSpaceIter<AddableInteger, AddableInteger> (null, this.getVariables(), this.getDomains(), null, null); iter.hasNext(); i++) 
			utilities[i] = this.getUtility(iter.nextSolution());
		
		return new BasicHypercube< AddableInteger, ArrayList<AddableInteger> > (this.getVariables(), this.getDomains(), utilities, null);
	}

	/** @see BasicUtilitySolutionSpace#setDefaultUtility(java.io.Serializable) */
	public void setDefaultUtility(ArrayList<AddableInteger> utility) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see BasicUtilitySolutionSpace#setInfeasibleUtility(java.io.Serializable) */
	public void setInfeasibleUtility(ArrayList<AddableInteger> utility) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
	}
	
	/** @see BasicUtilitySolutionSpace#setUtility(Addable[], java.io.Serializable) */
	public boolean setUtility(AddableInteger[] variablesValues,
			ArrayList<AddableInteger> utility) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
	}

	/** @see BasicUtilitySolutionSpace#setUtility(long, java.io.Serializable) */
	public void setUtility(long index, ArrayList<AddableInteger> utility) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see BasicUtilitySolutionSpace#slice(java.lang.String[], Addable[][]) */
	public BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> slice(
			String[] variablesNames, AddableInteger[][] subDomains) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#slice(java.lang.String[], Addable[]) */
	public BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> slice(
			String[] variablesNames, AddableInteger[] values) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#slice(java.lang.String, Addable[]) */
	public BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> slice(
			String var, AddableInteger[] subDomain) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#slice(java.lang.String, Addable) */
	public BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> slice(
			String var, AddableInteger val) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see BasicUtilitySolutionSpace#slice(Addable[]) */
	public BasicUtilitySolutionSpace<AddableInteger, ArrayList<AddableInteger>> slice(
			AddableInteger[] variablesValues) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see SolutionSpace#augment(Addable[]) */
	public void augment(AddableInteger[] variablesValues) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see SolutionSpace#getDomain(java.lang.String) */
	public AddableInteger[] getDomain(String variable) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see SolutionSpace#getDomain(int) */
	public AddableInteger[] getDomain(int index) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see SolutionSpace#getDomain(java.lang.String, int) */
	public AddableInteger[] getDomain(String variable, int index) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see SolutionSpace#getDomains() */
	public AddableInteger[][] getDomains() {
		
		AddableInteger[][] doms = new AddableInteger [this.vars.length][];
		
		for (int i = this.vars.length - 1; i >= 0; i--) 
			doms[i] = this.allDoms.get(this.vars[i].id());
		
		return doms;
	}

	/** @see SolutionSpace#getIndex(java.lang.String) */
	public int getIndex(String variable) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return 0;
	}

	/** @see SolutionSpace#getName() */
	public String getName() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
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
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see SolutionSpace#getVariables() */
	public String[] getVariables() {
		
		String[] out = new String [this.vars.length];
		for (int i = this.vars.length - 1; i >= 0; i--) 
			out[i] = this.vars[i].id();
		
		return out;
	}

	/** @see BasicUtilitySolutionSpace#iterator(java.lang.String[]) */
	public BasicUtilitySolutionSpace.Iterator< AddableInteger, ArrayList<AddableInteger> > iterator(
			String[] order) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
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
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return false;
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
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
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

	/** @see frodo2.solutionSpaces.SolutionSpace#getOwner() */
	public String getOwner() {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		return null;
	}

	/** @see frodo2.solutionSpaces.SolutionSpace#setOwner(java.lang.String) */
	public void setOwner(String owner) {
		/// @todo Auto-generated method stub
		assert false : "Not yet implemented";
		
	}

	/** @see SolutionSpace#setProblem(ProblemInterface) */
	@Override
	public void setProblem(ProblemInterface<AddableInteger, ?> problem) { }

	/** @see SolutionSpace#countsCCs() */
	@Override
	public boolean countsCCs() {
		return false;
	}

}
