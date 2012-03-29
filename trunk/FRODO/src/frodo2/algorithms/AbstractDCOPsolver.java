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

package frodo2.algorithms;

import java.lang.reflect.Field;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import frodo2.algorithms.varOrdering.election.VariableElection;
import frodo2.communication.MessageListener;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.DCOPProblemInterface;

/** An abstract convenient class for solving DCOP instances
 * @author Thomas Leaute
 * @param <V> type used for variable values
 * @param <U> type used for utility values
 * @param <S> type used for the solution
 */
public abstract class AbstractDCOPsolver < V extends Addable<V>, U extends Addable<U>, S extends Solution<V, U> > 
	extends AbstractSolver<DCOPProblemInterface<V, U>, V, U, S> {

	/**
	 * Dummy constructor
	 */
	protected AbstractDCOPsolver() {
		super();
		this.overrideMsgTypes();
	}
	
	/** Constructor from an agent configuration file
	 * @param agentDescFile 	the agent configuration file
	 */
	protected AbstractDCOPsolver (String agentDescFile) {
		super (agentDescFile);
		this.overrideMsgTypes();
	}
	
	/** Constructor from an agent configuration file
	 * @param agentDescFile 	the agent configuration file
	 * @param useTCP 			Whether to use TCP pipes or shared memory pipes
	 * @warning Using TCP pipes automatically disables simulated time. 
	 */
	protected AbstractDCOPsolver (String agentDescFile, boolean useTCP) {
		super (agentDescFile, useTCP);
		this.overrideMsgTypes();
	}
	
	/** Constructor
	 * @param agentDesc 	a JDOM Document for the agent description
	 */
	protected AbstractDCOPsolver (Document agentDesc) {
		super(agentDesc);
		this.overrideMsgTypes();
	}
	
	/** Constructor
	 * @param agentDesc 	a JDOM Document for the agent description
	 * @param useTCP 		Whether to use TCP pipes or shared memory pipes
	 * @warning Using TCP pipes automatically disables simulated time. 
	 */
	protected AbstractDCOPsolver (Document agentDesc, boolean useTCP) {
		super (agentDesc, useTCP);
		this.overrideMsgTypes();
	}
	
	/** Constructor
	 * @param agentDesc 	The agent description
	 * @param parserClass 	The class of the parser to be used
	 */
	protected AbstractDCOPsolver (Document agentDesc, Class< ? extends XCSPparser<V, U> > parserClass) {
		super(agentDesc, parserClass);
		this.overrideMsgTypes();
	}
	
	/** Constructor
	 * @param agentDesc 	The agent description
	 * @param parserClass 	The class of the parser to be used
	 * @param useTCP 		Whether to use TCP pipes or shared memory pipes
	 * @warning Using TCP pipes automatically disables simulated time. 
	 */
	protected AbstractDCOPsolver (Document agentDesc, Class< ? extends XCSPparser<V, U> > parserClass, boolean useTCP) {
		super(agentDesc, parserClass, useTCP);
		this.overrideMsgTypes();
	}
	
	/** @see AbstractSolver#solve(org.jdom.Document, int, boolean, java.lang.Long, boolean) */
	@Override
	public S solve (Document problem, int nbrElectionRounds, boolean measureMsgs, Long timeout, boolean cleanAfterwards) {
		
		agentDesc.getRootElement().setAttribute("measureMsgs", Boolean.toString(measureMsgs));
		this.setNbrElectionRounds(nbrElectionRounds);
		return this.solve(problem, cleanAfterwards, timeout);
	}
	
	/** @see AbstractSolver#solve(frodo2.solutionSpaces.ProblemInterface, int, boolean, java.lang.Long, boolean) */
	@Override
	public S solve (DCOPProblemInterface<V, U> problem, int nbrElectionRounds, boolean measureMsgs, Long timeout, boolean cleanAfterwards) {
		
		agentDesc.getRootElement().setAttribute("measureMsgs", Boolean.toString(measureMsgs));
		this.setNbrElectionRounds(nbrElectionRounds);
		return this.solve(problem, cleanAfterwards, timeout);
	}
	
	/** Sets the number of rounds of VariableElection
	 * @param nbrElectionRounds 	the number of rounds of VariableElection (must be greater than the diameter of the constraint graph)
	 */
	@SuppressWarnings("unchecked")
	protected void setNbrElectionRounds (int nbrElectionRounds) {
		for (Element module : (List<Element>) agentDesc.getRootElement().getChild("modules").getChildren()) 
			if (module.getAttributeValue("className").equals(VariableElection.class.getName())) 
				module.setAttribute("nbrSteps", Integer.toString(nbrElectionRounds));
	}
	
	/** Overrides message types if necessary */
	@SuppressWarnings("unchecked")
	private void overrideMsgTypes() {

		Element modsElmt = agentDesc.getRootElement().getChild("modules");
		
		try {
			if (modsElmt != null) {
				for (Element moduleElmt : (List<Element>) modsElmt.getChildren()) {

					String className = moduleElmt.getAttributeValue("className");
					Class< MessageListener<String> > moduleClass = (Class< MessageListener<String> >) Class.forName(className);
					Element allMsgsElmt = moduleElmt.getChild("messages");
					if (allMsgsElmt != null) {
						for (Element msgElmt : (List<Element>) allMsgsElmt.getChildren()) {

							// Look up the new value for the message type
							String newType = msgElmt.getAttributeValue("value");
							String ownerClassName = msgElmt.getAttributeValue("ownerClass");
							if (ownerClassName != null) { // the attribute "value" actually refers to a field in a class
								Class<?> ownerClass = Class.forName(ownerClassName);
								try {
									Field field = ownerClass.getDeclaredField(newType);
									newType = (String) field.get(newType);
								} catch (NoSuchFieldException e) {
									System.err.println("Unable to read the value of the field " + ownerClass.getName() + "." + newType);
									e.printStackTrace();
								}
							}

							// Set the message type to its new value
							try {
								SingleQueueAgent.setMsgType(moduleClass, msgElmt.getAttributeValue("name"), newType);
							} catch (NoSuchFieldException e) {
								System.err.println("Unable to find the field " + moduleClass.getName() + "." + msgElmt.getAttributeValue("name"));
								e.printStackTrace();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
