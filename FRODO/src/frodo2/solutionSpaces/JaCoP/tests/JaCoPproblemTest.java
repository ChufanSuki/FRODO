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

package frodo2.solutionSpaces.JaCoP.tests;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.jacop.constraints.Constraint;
import org.jacop.constraints.DecomposedConstraintCloneableInterface;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.ViolationMeasure;
import org.jacop.constraints.XeqCCloneable;
import org.jacop.constraints.geost.ExternalConstraintCloneable;
import org.jacop.constraints.geost.GeostObjectCloneable;
import org.jacop.constraints.geost.NonOverlappingCloneable;
import org.jacop.constraints.geost.ShapeCloneable;
import org.jacop.constraints.netflow.ArithmeticCloneable;
import org.jacop.constraints.netflow.NetworkBuilder;
import org.jacop.constraints.netflow.simplex.Node;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVarCloneable;
import org.jacop.core.IntervalDomain;
import org.jacop.core.StoreCloneable;
import org.jacop.util.fsm.FSMCloneable;
import org.jacop.util.fsm.FSMState;
import org.jacop.util.fsm.FSMTransition;

import frodo2.algorithms.AbstractDCOPsolver;
import frodo2.algorithms.Solution;
import frodo2.algorithms.adopt.ADOPTsolver;
import frodo2.algorithms.afb.AFBsolver;
import frodo2.algorithms.asodpop.ASODPOPsolver;
import frodo2.algorithms.dpop.DPOPsolver;
import frodo2.algorithms.dpop.privacy.P2_DPOPsolver;
import frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver;
import frodo2.algorithms.dpop.privacy.P_DPOPsolver;
import frodo2.algorithms.localSearch.dsa.DSAsolver;
import frodo2.algorithms.localSearch.mgm.MGMsolver;
import frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver;
import frodo2.algorithms.maxsum.MaxSumSolver;
import frodo2.algorithms.mpc_discsp.MPC_DisWCSP4solver;
import frodo2.algorithms.odpop.ODPOPsolver;
import frodo2.algorithms.synchbb.SynchBBsolver;
import frodo2.solutionSpaces.Addable;
import frodo2.solutionSpaces.AddableInteger;
import frodo2.solutionSpaces.UtilitySolutionSpace;
import frodo2.solutionSpaces.JaCoP.JaCoPproblem;
import frodo2.solutionSpaces.crypto.AddableBigInteger;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** A JUnit TestCase for the JaCoPproblem class
 * @author Thomas Leaute
 * @param <U> the class used for utility/cost values
 */
public class JaCoPproblemTest < U extends Addable<U> > extends TestCase {
	
	/** Adds 1 test per algorithm to the input test suite
	 * @param suite 		the test suite
	 * @param testMethod 	the name of the test method
	 * @param constraint 	the class of the JaCoP constraint
	 * @param maximize 		whether to test on a maximization or a minimization problem
	 */
	public static void addTests (TestSuite suite, String testMethod, String constraint, boolean maximize) {
		addTests(suite, testMethod, constraint, maximize, 0, 3);
	}
	
	/** Adds 1 test per algorithm to the input test suite
	 * @param suite 		the test suite
	 * @param testMethod 	the name of the test method
	 * @param constraint 	the class of the JaCoP constraint
	 * @param maximize 		whether to test on a maximization or a minimization problem
	 * @param min 			the minimum variable domain value
	 * @param max 			the maximum variable domain value
	 */
	public static void addTests (TestSuite suite, String testMethod, String constraint, boolean maximize, int min, int max) {
		addTests (suite, testMethod, constraint, maximize, min, max, true);
	}
	
	/** Adds 1 test per algorithm to the input test suite
	 * @param suite 		the test suite
	 * @param testMethod 	the name of the test method
	 * @param constraint 	the class of the JaCoP constraint
	 * @param maximize 		whether to test on a maximization or a minimization problem
	 * @param min 			the minimum variable domain value
	 * @param max 			the maximum variable domain value
	 * @param discsp 		if true, the problem is a pure DisCSP
	 */
	public static void addTests (TestSuite suite, String testMethod, String constraint, boolean maximize, int min, int max, boolean discsp) {
				
		// Test the complete algorithms against DPOP

		// ADOPT
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, new ADOPTsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/adopt/ADOPTagentJaCoP.xml", true, maximize ? 3 : 0), true, false));

		// AFB
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, new AFBsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/afb/AFBagentJaCoP.xml", true, maximize ? 3 : 0), true, false));

		// ASO-DPOP 
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, new ASODPOPsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/asodpop/ASODPOPagentJaCoP.xml", true), true, false));

		// DPOP 
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, new DPOPsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/dpop/DPOPagentJaCoP.xml", true), true, false));

		// MB-DPOP 
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, new DPOPsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/dpop/memory/MB-DPOPagentJaCoP.xml", true), true, false));

		// P-DPOP
		suite.addTest(new JaCoPproblemTest<AddableBigInteger> (testMethod, constraint, maximize, min, max, 
				AddableBigInteger.class, new P_DPOPsolver<AddableInteger> ("/frodo2/algorithms/dpop/privacy/P-DPOPagentJaCoP.xml", true), true, false));
		
		// P3/2-DPOP
		suite.addTest(new JaCoPproblemTest<AddableBigInteger> (testMethod, constraint, maximize, min, max, 
				AddableBigInteger.class, new P3halves_DPOPsolver<AddableInteger> ("/frodo2/algorithms/dpop/privacy/P1.5-DPOPagentJaCoP.xml", true), true, false));
		
		// P2-DPOP 
		P2_DPOPsolver<AddableInteger, AddableInteger> p2solver = new P2_DPOPsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/dpop/privacy/P2-DPOPagentJaCoP.xml", true, maximize ? 3 : 0);
		p2solver.setInfinity(3 + (maximize ? 3 : 0));
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, p2solver, true, false));
		
		// MPC-Dis(W)CSP4
		if (! maximize) { // MPC-Dis(W)CSP4 doesn't support maximization problems
			if (discsp) 
				suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
						AddableInteger.class, new MPC_DisWCSP4solver<AddableInteger, AddableInteger> ("/frodo2/algorithms/mpc_discsp/MPC-DisCSP4_JaCoP.xml", true), true, true));
			suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
					AddableInteger.class, new MPC_DisWCSP4solver<AddableInteger, AddableInteger> ("/frodo2/algorithms/mpc_discsp/MPC-DisWCSP4_JaCoP.xml", true), true, true));
		}
		
		// O-DPOP 
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, new ODPOPsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/odpop/ODPOPagentJaCoP.xml", true), true, false));

		// SynchBB 
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, new SynchBBsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/synchbb/SynchBBagentJaCoP.xml", true, maximize ? 3 : 0), true, false));

		
		// Test the incomplete algorithms 
		
		// DSA 
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, 
				new DSAsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/localSearch/dsa/DSAagentJaCoP.xml", true), false, false));
		
		// MGM
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, 
				new MGMsolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/localSearch/mgm/MGMagentJaCoP.xml", true), false, false));
		
		// MGM2
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, 
				new MGM2solver<AddableInteger, AddableInteger> ("/frodo2/algorithms/localSearch/mgm/mgm2/MGM2agentJaCoP.xml", true), false, false));

		// Max-Sum 
		suite.addTest(new JaCoPproblemTest<AddableInteger> (testMethod, constraint, maximize, min, max, 
				AddableInteger.class, new MaxSumSolver<AddableInteger, AddableInteger> ("/frodo2/algorithms/maxsum/MaxSumAgentJaCoP.xml", false), false, false));		
		
	}

	/** @return the test suite */
	public static TestSuite suite () {
		TestSuite suite = new TestSuite ("Tests for the JaCoP problem");
		
		String[] cons;
		
		// ValuePrecede constraint
		// (int, int, IntVar)
		cons = new String[] {
				"org.jacop.constraints.ValuePrecedeCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_ValuePrecede", constraint, false);
			addTests(suite, "test_ValuePrecede", constraint, true);
		}
		
		// SumInt constraint
		// (IntVar[], String, IntVar)
		cons = new String[] {
				"org.jacop.constraints.SumIntCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_Sum", constraint, false);
			addTests(suite, "test_Sum", constraint, true);
		}
		
		// SumBool constraint
		// (Store, IntVar[], String, IntVar)
		cons = new String[] {
				"org.jacop.constraints.SumBoolCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_Sum", constraint, false, 0, 1);
			addTests(suite, "test_Sum", constraint, true, 0, 1);
		}
		
		// LinearInt constraints
		// (IntVar[], int[], String, int)
		cons = new String[] {
				"org.jacop.constraints.LinearIntCloneable", 
				"org.jacop.constraints.LinearIntDomCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_LinearInt", constraint, false);
			addTests(suite, "test_LinearInt", constraint, true);
		}
		
		// Stretch constraint
		// (int[], int[], int[], IntVar[])
		cons = new String[] {
				"org.jacop.constraints.StretchCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_Stretch", constraint, false);
			addTests(suite, "test_Stretch", constraint, true);
		}
		
		// (IntVar, int)
		cons = new String[] {
				"org.jacop.constraints.XeqCCloneable", 
				"org.jacop.constraints.XgtCCloneable", 
				"org.jacop.constraints.XgteqCCloneable", 
				"org.jacop.constraints.XltCCloneable", 
				"org.jacop.constraints.XlteqCCloneable", 
				"org.jacop.constraints.XneqCCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_int", constraint, false);
			addTests(suite, "test_IntVar_int", constraint, true);
		}
		
		// (IntVar, IntDomain)
		cons = new String[] {
				"org.jacop.constraints.InCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntDomain", constraint, false);
			addTests(suite, "test_IntVar_IntDomain", constraint, true);
		}
		
		// (IntVar, IntVar)
		cons = new String[] {
				"org.jacop.constraints.AbsXeqYCloneable", 
				"org.jacop.constraints.XeqYCloneable", 
				"org.jacop.constraints.XgteqYCloneable", 
				"org.jacop.constraints.XgtYCloneable", 
				"org.jacop.constraints.XlteqYCloneable", 
				"org.jacop.constraints.XltYCloneable", 
				"org.jacop.constraints.XneqYCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVar", constraint, false);
			addTests(suite, "test_IntVar_IntVar", constraint, true);
		}
		
		// (IntVar, PrimitiveConstraint)
		cons = new String[] {
				"org.jacop.constraints.ImpliesCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_PrimitiveConstraint", constraint, false);
			addTests(suite, "test_IntVar_PrimitiveConstraint", constraint, true);
		}
		
		// (IntVar, int, IntVar)
		cons = new String[] {
				"org.jacop.constraints.XplusCeqZCloneable", 
				"org.jacop.constraints.XplusClteqZCloneable", 
				"org.jacop.constraints.XmulCeqZCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_int_IntVar", constraint, false);
			addTests(suite, "test_IntVar_int_IntVar", constraint, true);
		}
		
		// (IntVar, IntVar, int)
		cons = new String[] {
				"org.jacop.constraints.XmulYeqCCloneable", 
				"org.jacop.constraints.XplusYeqCCloneable", 
				"org.jacop.constraints.XplusYgtCCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVar_int", constraint, false);
			addTests(suite, "test_IntVar_IntVar_int", constraint, true);
		}
		
		// (IntVar, IntVar, IntVar)
		cons = new String[] {
				"org.jacop.constraints.DistanceCloneable", 
				"org.jacop.constraints.MaxSimpleCloneable", 
				"org.jacop.constraints.MinSimpleCloneable", 
				"org.jacop.constraints.XplusYeqZCloneable", 
				"org.jacop.constraints.XplusYlteqZCloneable", 
				"org.jacop.constraints.XdivYeqZCloneable",
				"org.jacop.constraints.XmodYeqZCloneable",
				"org.jacop.constraints.XmulYeqZCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVar_IntVar", constraint, false);
			addTests(suite, "test_IntVar_IntVar_IntVar", constraint, true);
		}
		
		// (BooleanVar, BooleanVar, BooleanVar)
		cons = new String[] {
				"org.jacop.constraints.IfThenBoolCloneable", 
				"org.jacop.constraints.AndBoolSimpleCloneable", 
				"org.jacop.constraints.OrBoolSimpleCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVar_IntVar", constraint, false, 0, 1);
			addTests(suite, "test_IntVar_IntVar_IntVar", constraint, true, 0, 1);
		}
		
		// (positive IntVar, positive IntVar, positive IntVar)
		cons = new String[] {
				"org.jacop.constraints.XexpYeqZCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVar_IntVar", constraint, false, 1, 3);
			addTests(suite, "test_IntVar_IntVar_IntVar", constraint, true, 1, 3);
		}

		// (IntVar, IntVar, int, IntVar)
		cons = new String[] {
				"org.jacop.constraints.XplusYplusCeqZCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVar_int_IntVar", constraint, false);
			addTests(suite, "test_IntVar_IntVar_int_IntVar", constraint, true);
		}
		
		// (IntVar, IntVar, IntVar, int)
		cons = new String[] {
				"org.jacop.constraints.XplusYplusQgtCCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVar_IntVar_int", constraint, false);
			addTests(suite, "test_IntVar_IntVar_IntVar_int", constraint, true);
		}
		
		// (IntVar, IntVar, IntVar, IntVar)
		cons = new String[] {
				"org.jacop.constraints.XplusYplusQeqZCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVar_IntVar_IntVar", constraint, false);
			addTests(suite, "test_IntVar_IntVar_IntVar_IntVar", constraint, true);
		}
		
		// (IntVar, int[], IntVar, int)
		cons = new String[] {
				"org.jacop.constraints.ElementIntegerCloneable", 
				"org.jacop.constraints.ElementIntegerFastCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_intArray_IntVar_int", constraint, false);
			addTests(suite, "test_IntVar_intArray_IntVar_int", constraint, true);
		}
		
		// (IntVar, IntVar[], IntVar, int)
		cons = new String[] {
				"org.jacop.constraints.ElementVariableCloneable", 
				"org.jacop.constraints.ElementVariableFastCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVar_IntVarArray_IntVar_int", constraint, false, 0, 2);
			addTests(suite, "test_IntVar_IntVarArray_IntVar_int", constraint, true, 0, 2);
		}
		
		// (IntVar[])
		cons = new String[] {
				"org.jacop.constraints.AlldifferentCloneable", 
				"org.jacop.constraints.AlldiffCloneable", 
				"org.jacop.constraints.CircuitCloneable", 
				"org.jacop.constraints.SubcircuitCloneable", 
				"org.jacop.constraints.AlldistinctCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray", constraint, false);
			addTests(suite, "test_IntVarArray", constraint, true);
		}
		
		// (IntVar, int, int)
		cons = new String[] {
				"org.jacop.constraints.AtLeastCloneable", 
				"org.jacop.constraints.AtMostCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_int_int", constraint, false);
			addTests(suite, "test_IntVarArray_int_int", constraint, true);
		}
		
		// (IntVar[], IntVar)
		cons = new String[] {
				"org.jacop.constraints.MaxCloneable", 
				"org.jacop.constraints.MemberCloneable", 
				"org.jacop.constraints.MinCloneable", 
				"org.jacop.constraints.SumCloneable", 
				"org.jacop.constraints.ValuesCloneable", 
				"org.jacop.constraints.ArgMaxCloneable", 
				"org.jacop.constraints.ArgMinCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVar", constraint, false);
			addTests(suite, "test_IntVarArray_IntVar", constraint, true);
		}

		// (BooleanVar[], BooleanVar)
		cons = new String[] {
				"org.jacop.constraints.EqBoolCloneable", 
				"org.jacop.constraints.XorBoolCloneable", 
				"org.jacop.constraints.AndBoolVectorCloneable", 
				"org.jacop.constraints.OrBoolVectorCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVar", constraint, false, 0, 1);
			addTests(suite, "test_IntVarArray_IntVar", constraint, true, 0, 1);
		}

		// (BooleanVar[], BooleanVar)
		cons = new String[] {
				"org.jacop.constraints.AndBoolCloneable",
				"org.jacop.constraints.OrBoolCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVar_Decomposed", constraint, false, 0, 1);
			addTests(suite, "test_IntVarArray_IntVar_Decomposed", constraint, true, 0, 1);
		}

		// (IntVar[], IntVar, ViolationMeasure)
		cons = new String[] {
				"org.jacop.constraints.SoftAlldifferentCloneable", /// @bug P3/2-DPOP & P2-DPOP tend to throw java.lang.AssertionError: non-optimal arcs
		};
		for (String constraint : cons) {
			addTests(suite, "test_SoftAlldifferent", constraint, false, 0, 3, false);
		}
		
		// (IntVar[], int[])
		cons = new String[] {
				"org.jacop.constraints.NoGoodCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_intArray", constraint, false);
			addTests(suite, "test_IntVarArray_intArray", constraint, true);
		}
		
		// (IntVar[], int[], IntVar)
		cons = new String[] {
				"org.jacop.constraints.SumWeightCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_intArray_IntVar", constraint, false);
			addTests(suite, "test_IntVarArray_intArray_IntVar", constraint, true);
		}

		// (IntVar[], int[][])
		cons = new String[] {
				"org.jacop.constraints.ExtensionalConflictVACloneable",
				"org.jacop.constraints.ExtensionalSupportMDDCloneable",
				"org.jacop.constraints.ExtensionalSupportSTRCloneable", 
				"org.jacop.constraints.ExtensionalSupportVACloneable", 
				"org.jacop.constraints.table.TableCloneable", 
				"org.jacop.constraints.table.SimpleTableCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_intArrayArray", constraint, false);
			addTests(suite, "test_IntVarArray_intArrayArray", constraint, true);
		}
		
		// (IntVar[], IntVar[])
		cons = new String[] {
				"org.jacop.constraints.AssignmentCloneable", 
				"org.jacop.constraints.LexOrderCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVarArray", constraint, false, 0, 2);
			addTests(suite, "test_IntVarArray_IntVarArray", constraint, true, 0, 2);
		}

		// (BooleanVar[], BooleanVar[])
		cons = new String[] {
				"org.jacop.constraints.BoolClauseCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVarArray", constraint, false, 0, 1);
			addTests(suite, "test_IntVarArray_IntVarArray", constraint, true, 0, 1);
		}

		// (IntVar[], IntVar[])
		cons = new String[] {
				"org.jacop.constraints.GCCCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_GCC", constraint, false);
			addTests(suite, "test_GCC", constraint, true);
		}
		
		// (IntVar[], IntVar[], IntVar)
		cons = new String[] {
				"org.jacop.constraints.AmongVarCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVarArray_IntVar", constraint, false, 0, 2);
			addTests(suite, "test_IntVarArray_IntVarArray_IntVar", constraint, true, 0, 2);
		}
		
		// (IntVar[], IntVar[], int[])
		cons = new String[] {
				"org.jacop.constraints.binpacking.BinpackingCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVarArray_intArray", constraint, false, 0, 2);
			addTests(suite, "test_IntVarArray_IntVarArray_intArray", constraint, true, 0, 2);
		}
		
		// (IntVar[], IntVar, int)
		cons = new String[] {
				"org.jacop.constraints.CountCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVar_int", constraint, false);
			addTests(suite, "test_IntVarArray_IntVar_int", constraint, true);
		}

		// (IntVar[], IntervalDomain, IntVar)
		cons = new String[] {
				"org.jacop.constraints.AmongCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntervalDomain_IntVar", constraint, false);
			addTests(suite, "test_IntVarArray_IntervalDomain_IntVar", constraint, true);
		}
		
		// (IntVar[], IntervalDomain, int, int, int)
		cons = new String[] {
				"org.jacop.constraints.SequenceCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntervalDomain_int_int_int", constraint, false);
			addTests(suite, "test_IntVarArray_IntervalDomain_int_int_int", constraint, true);
		}
		
		// (IntVar[], IntVar[], IntVar[], IntVar)
		cons = new String[] {
				"org.jacop.constraints.CumulativeBasicCloneable",
				"org.jacop.constraints.CumulativeCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVarArray_IntVarArray_IntVar", constraint, false, 1, 2);
			addTests(suite, "test_IntVarArray_IntVarArray_IntVarArray_IntVar", constraint, true, 1, 2);
		}

		// (IntVar[], IntVar[], IntVar[], IntVar)
		cons = new String[] {
				"org.jacop.constraints.CumulativeUnaryCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_CumulativeUnary", constraint, false, 1, 2);
			addTests(suite, "test_CumulativeUnary", constraint, true, 1, 2);
		}

		// (IntVar[], IntVar[], IntVar[], IntVar, ViolationMeasure)
		cons = new String[] {
				"org.jacop.constraints.SoftGCCCloneable", /// @bug P[3/2]-DPOP tends to throw java.lang.AssertionError: non-optimal arcs
		};
		for (String constraint : cons) {
			addTests(suite, "test_SoftGCC", constraint, false, 0, 2, false);
		}

		// (IntVar[], IntVar[], IntVar[], IntVar[])
		cons = new String[] {
				"org.jacop.constraints.DiffCloneable",
				"org.jacop.constraints.Diff2Cloneable",
				"org.jacop.constraints.DisjointCloneable",
				"org.jacop.constraints.diffn.DiffnCloneable",
				"org.jacop.constraints.diffn.NooverlapCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVarArray_IntVarArray_IntVarArray", constraint, false, 0, 1);
			addTests(suite, "test_IntVarArray_IntVarArray_IntVarArray_IntVarArray", constraint, true, 0, 1);
		}
		
		// (IntVar[], IntVar[], IntVar[], IntVar[])
		cons = new String[] {
				"org.jacop.constraints.diffn.DiffnDecomposedCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArray_IntVarArray_IntVarArray_IntVarArray_Decomposed", constraint, false, 1, 2);
			addTests(suite, "test_IntVarArray_IntVarArray_IntVarArray_IntVarArray_Decomposed", constraint, true, 1, 2);
		}
		
		// (IntVar[], IntVar[], IntVar[], IntVar[], ArrayList<ArrayList<Integer>>, ArrayList<? extends IntVar>)
		cons = new String[] {
				"org.jacop.constraints.DisjointConditionalCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_DisjointConditional", constraint, false);
			addTests(suite, "test_DisjointConditional", constraint, true);
		}

		// (IntVar[][])
		cons = new String[] {
				"org.jacop.constraints.LexCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_IntVarArrayArray", constraint, false);
			addTests(suite, "test_IntVarArrayArray", constraint, true);
		}
		
		// (GeostObject[], ExternalConstraint[], Shape[])
		cons = new String[] {
				"org.jacop.constraints.geost.GeostCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_Geost", constraint, false, 0, 1);
			addTests(suite, "test_Geost", constraint, true, 0, 1);
		}
		
		// (int[], int[], IntVar[], IntVar, IntVar)
		cons = new String[] {
				"org.jacop.constraints.knapsack.KnapsackCloneable", /// @bug NullPointerException -> bug in JaCoP https://github.com/radsz/jacop/issues/38
		};
		for (String constraint : cons) {
			addTests(suite, "test_Knapsack", constraint, false, 0, 1, false);
			addTests(suite, "test_Knapsack", constraint, true, 0, 1, false);
		}
		
		// (NetworkBuilder)
		cons = new String[] {
				"org.jacop.constraints.netflow.NetworkFlowCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_NetworkFlow", constraint, false, 0, 1);
			addTests(suite, "test_NetworkFlow", constraint, true, 0, 1);
		}
		
		// (PrimitiveConstraint)
		cons = new String[] {
				"org.jacop.constraints.NotCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_Not", constraint, false);
			addTests(suite, "test_Not", constraint, true);
		}
		
		// (PrimitiveConstraint, IntVar)
		cons = new String[] {
				"org.jacop.constraints.XorCloneable", 
				"org.jacop.constraints.ReifiedCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_PrimitiveConstraint_IntVar", constraint, false, 0, 1);
			addTests(suite, "test_PrimitiveConstraint_IntVar", constraint, true, 0, 1);
		}

		// (PrimitiveConstraint, PrimitiveConstraint)
		cons = new String[] {
				"org.jacop.constraints.AndCloneable",
				"org.jacop.constraints.EqCloneable",
				"org.jacop.constraints.IfThenCloneable",
				"org.jacop.constraints.OrCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_PrimitiveConstraint_PrimitiveConstraint", constraint, false);
			addTests(suite, "test_PrimitiveConstraint_PrimitiveConstraint", constraint, true);
		}

		// (PrimitiveConstraint, PrimitiveConstraint, PrimitiveConstraint)
		cons = new String[] {
				"org.jacop.constraints.IfThenElseCloneable",
		};
		for (String constraint : cons) {
			addTests(suite, "test_PrimitiveConstraint_PrimitiveConstraint_PrimitiveConstraint", constraint, false);
			addTests(suite, "test_PrimitiveConstraint_PrimitiveConstraint_PrimitiveConstraint", constraint, true);
		}
		
		// Regular constraint
		cons = new String[] {
				"org.jacop.constraints.regular.RegularCloneable", 
		};
		for (String constraint : cons) {
			addTests(suite, "test_Regular", constraint, false);
			addTests(suite, "test_Regular", constraint, true);
		}
		
		// Arithmetic constraint
		cons = new String [] {
				"org.jacop.constraints.netflow.ArithmeticCloneable",
		}; 
		for (String constraint : cons) {
			addTests(suite, "test_Arithmetic", constraint, false);
			addTests(suite, "test_Arithmetic", constraint, true);
		}
		
//		return frodo2.algorithms.test.AllTests.reverse(suite);
		return suite;
	}
	
	/** The name of the constraint class */
	private String constName;

	/** The class of the constraint */
	private Class<? extends DecomposedConstraintCloneableInterface> constClass;
	
	/** The class used for utility/cost values */
	private final Class<U> utilClass;

	/** The DCOP problem instance */
	private JaCoPproblem<U> problem;
	
	/** The solver */
	private final AbstractDCOPsolver<AddableInteger, U, ? extends Solution<AddableInteger, U>> solver;

	/** Whether the algorithm is complete and should be tested for optimality */
	private final boolean complete;

	/** The JaCoP store used to define the variables */
	private StoreCloneable store;

	/** Used to name variables */
	private int varID;
	
	/** Whether to test on a maximization or a minimization problem */
	private final boolean maximize;
	
	/** The minimum variable domain value */
	private final int min;
	
	/** The maximum variable domain value */
	private final int max;
	
	/** Whether the variables in the problem should be public */
	private final boolean publicVars;
	
	/** Constructor
	 * @param testMethod 	the name of the test method
	 * @param constraint 	the class of the JaCoP constraint
	 * @param maximize 		whether to test on a maximization or a minimization problem
	 * @param min 			the minimum variable domain value
	 * @param max 			the maximum variable domain value
	 * @param utilClass 	the class used for utility/cost values
	 * @param solver 		the solver
	 * @param complete 		whether the algorithm is complete
	 * @param publicVars 	whether the variables should be public
	 */
	public JaCoPproblemTest (String testMethod, String constraint, boolean maximize, int min, int max, 
			Class<U> utilClass, AbstractDCOPsolver<AddableInteger, U, ? extends Solution<AddableInteger, U>> solver, boolean complete, boolean publicVars) {
		super(testMethod);
		this.constName = constraint;
		this.maximize = maximize;
		this.min = min;
		this.max = max;
		this.utilClass = utilClass;
		this.solver = solver;
		this.complete = complete;
		this.publicVars = publicVars;
	}

	/** @see junit.framework.TestCase#setUp() */
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.store = new StoreCloneable ();
		this.problem = new JaCoPproblem<U> (this.maximize);
		this.problem.setUtilClass(this.utilClass);
		this.constClass = (Class<? extends DecomposedConstraintCloneableInterface>) Class.forName(this.constName);
	}

	/** Solves the problem with various algorithms */
	private void solve() {
		
		Solution<AddableInteger, U> solution = null; 
		
		if (this.publicVars) {
			
			// Use an equivalent problem with private constraints
			JaCoPproblem<U> prob2 = new JaCoPproblem<U> (this.problem.maximize(), false, true, false);
			prob2.setUtilClass(this.utilClass);
			
			// Add the variables
			for (IntVarCloneable var : this.problem.getJaCoPvars()) 
				prob2.addVariable(var, this.problem.getOwner(var.id()));
			
			// Add the constraints, assigning them owners
			for (UtilitySolutionSpace<AddableInteger, U> space : this.problem.getSolutionSpaces()) {
				UtilitySolutionSpace<AddableInteger, U> space2 = space.clone();
				space2.setOwner(prob2.getOwner(space2.getVariables()[0])); // assign the constraint the its first variable's owner
				prob2.addSolutionSpace(space2);
			}
						
			// Solve the new problem 
			solution = solver.solve(prob2, prob2.getNbrVars(), 30L * 60L * 1000L); // 30-min timeout
			
		} else // private variables
			solution = solver.solve(this.problem, this.problem.getNbrVars(), 5L * 60L * 1000L); // 5-min timeout
		
		assertNotNull(solver.getClass().getSimpleName() + " did not find any solution to " + this.constName, solution);
		
		if (this.complete) {
			
			// Find an optimal solution (according to DPOP)
			/// @todo Search for all solutions using JaCoP to check that DPOP outputs the optimal
			DPOPsolver<AddableInteger, U> dpopSolver = new DPOPsolver<AddableInteger, U> (
					"/frodo2/algorithms/dpop/DPOPagentJaCoP.xml", 
					AddableInteger.class, 
					this.utilClass, 
					true);
			Solution<AddableInteger, U> optSolution = dpopSolver.solve(this.problem, this.problem.getNbrVars(), 2L * 60L * 1000L); // 2-min timeout
			assertNotNull("DPOP did not find any solution to " + this.constName, optSolution);
			
			assert optSolution.getUtility().equals(optSolution.getReportedUtil()) : "Inconsistent utilities: " + optSolution;
			
			assertEquals(solver.getClass().getSimpleName() + " did not find the optimal solution to " + this.constName + ";", optSolution.getUtility(), solution.getUtility());
		}
	}

	/** Test method for DecomposedConstraints of constructor signature (int[], int[], int[], IntVar[]) 
	 * @throws Exception if something goes wrong
	 */
	public void test_Stretch () throws Exception {
		
		Constructor<? extends DecomposedConstraintCloneableInterface> constructor = 
				this.constClass.getConstructor(int[].class, int[].class, int[].class, IntVarCloneable[].class);
		
		DecomposedConstraintCloneableInterface constraint = constructor.newInstance(
				new int[] {0, 1, 2}, // the values taken by the variables
				new int[] {1, 1, 1}, // the min sequence length for each value
				new int[] {2, 2, 2}, // the max sequence length for each value
				newIntVarArray(3)
				);
		
		this.problem.addDecompConstraint(constraint);
		
		this.solve();
	}
	
	/** Test method for the SumInt and SumBool constraints
	 * @throws Exception if something goes wrong
	 */
	public void test_Sum () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, String.class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				"==", 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}
	
	/** Test method for the LinearInt constraints
	 * @throws Exception if something goes wrong
	 */
	public void test_LinearInt () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(
						IntVarCloneable[].class, int[].class, String.class, int.class);
		
		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				new int[] {1, 2, 3}, 
				"==", 
				10
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}
	
	/** Test method for Constraints of constructor signature (IntVar, int) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_int () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, int.class);
		
		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				2
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}
	
	/** Test method for Constraints of constructor signature (IntVar, int, int) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_int_int () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, int.class, int.class);
		
		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2), 
				2, 
				2
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}
	
	/** Test method for Constraints of constructor signature (IntVar, IntDomain) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_IntDomain () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, IntDomain.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				new IntervalDomain (1, 2)
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}
	
	/** Test method for Constraints of constructor signature (IntVar, IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar, int, IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_int_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, int.class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				2, 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar, IntVar, int) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_IntVar_int () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, IntVarCloneable.class, int.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				this.newIntVar(), 
				2
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar, IntVar, IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_IntVar_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, IntVarCloneable.class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				this.newIntVar(), 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar, IntVar, int, IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_IntVar_int_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, IntVarCloneable.class, int.class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				this.newIntVar(), 
				2, 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar, IntVar, IntVar, int) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_IntVar_IntVar_int () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, IntVarCloneable.class, IntVarCloneable.class, int.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				this.newIntVar(), 
				this.newIntVar(), 
				2
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar, IntVar, IntVar, IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_IntVar_IntVar_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, IntVarCloneable.class, IntVarCloneable.class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				this.newIntVar(), 
				this.newIntVar(), 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar, int[], IntVar, int) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_intArray_IntVar_int () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, int[].class, IntVarCloneable.class, int.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				new int[] {1, 2, 3}, 
				this.newIntVar(), 
				2
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar, IntVar[], IntVar, int) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_IntVarArray_IntVar_int () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, IntVarCloneable[].class, IntVarCloneable.class, int.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVar(), 
				this.newIntVarArray(3), 
				this.newIntVar(), 
				-1
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[]) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class);

		Constraint constraint = constructor.newInstance(
				new Object[] { this.newIntVarArray(3) } // That's what I would call a Java oddity
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for DecomposedConstraints of constructor signature (IntVar[], IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVar_Decomposed () throws Exception {
		
		Constructor<? extends DecomposedConstraintCloneableInterface> constructor = 
				(Constructor<? extends DecomposedConstraintCloneableInterface>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable.class);

		DecomposedConstraintCloneableInterface constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				this.newIntVar()
				);
		
		this.problem.addDecompConstraint(constraint);
		
		this.solve();
	}

	/** Test method for the SoftAlldifferent DecomposedConstraint 
	 * @throws Exception if something goes wrong
	 */
	public void test_SoftAlldifferent () throws Exception {
		
		Constructor<? extends DecomposedConstraintCloneableInterface> constructor = (Constructor<? extends DecomposedConstraintCloneableInterface>) 
				this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable.class, ViolationMeasure.class);

		IntVarCloneable costVar = this.newIntVar();
		DecomposedConstraintCloneableInterface constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				costVar, 
				ViolationMeasure.DECOMPOSITION_BASED
				);
		
		this.problem.addSoftDecompConstraint(constraint, costVar);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], int[], IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_intArray_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, int[].class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				new int[] {1, 2, 3}, 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}
	
	/** Test method for the Linear constraint
	 * @throws Exception if something goes wrong
	 */
	public void test_Linear () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(
						StoreCloneable.class, IntVarCloneable[].class, int[].class, String.class, int.class);

		Constraint constraint = constructor.newInstance(
				this.store, 
				this.newIntVarArray(3), 
				new int[] {1, 2, 3}, 
				"=", 
				4
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], int[]) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_intArray () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, int[].class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				new int[] {1, 2, 3}
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], int[][]) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_intArrayArray () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, int[][].class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				new int[][] {
					new int[] {0, 0, 0},
					new int[] {1, 1, 1},
					new int[] {2, 2, 2},
					}
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for constraints wiht signature (IntVar[], IntVar[])
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVarArray () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2), 
				this.newIntVarArray(2)
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for GCC constraints
	 * @throws Exception if something goes wrong
	 */
	public void test_GCC () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2, 0, 1), 
				this.newIntVarArray(2, 0, 2)
				);
		
		this.problem.addConstraint(constraint);
				
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntVar[], IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVarArray_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntVar[], int[]) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVarArray_intArray () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class, int[].class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				new int[] {1, 2}
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntVar, int) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVar_int () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable.class, int.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				this.newIntVar(), 
				2
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntervalDomain, IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntervalDomain_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntervalDomain.class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				new IntervalDomain(0, 4), 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for DecomposedConstraint of constructor signature (IntVar[], IntervalDomain, int, int, int) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntervalDomain_int_int_int () throws Exception {
		
		Constructor<? extends DecomposedConstraintCloneableInterface> constructor = 
				(Constructor<? extends DecomposedConstraintCloneableInterface>) this.constClass.getConstructor(
						IntVarCloneable[].class, IntervalDomain.class, int.class, int.class, int.class);

		DecomposedConstraintCloneableInterface constraint = constructor.newInstance(
				this.newIntVarArray(3), 
				new IntervalDomain(0, 4), 
				1, 
				2, 
				3
				);
		
		this.problem.addDecompConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntVar[], IntVar[], IntVar) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVarArray_IntVarArray_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				this.newIntVar()
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for the CumulativeUnary constraint
	 * @throws Exception if something goes wrong
	 */
	public void test_CumulativeUnary () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable.class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2, 0, 2), 
				this.newIntVarArray(2, 1, 2), 
				this.newIntVarArray(2, 1, 1), 
				this.newIntVar(1, 1)
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntVar[], IntVar[], IntVar, ViolationMeasure) 
	 * @throws Exception if something goes wrong
	 */
	public void test_SoftGCC () throws Exception {
		
		Constructor<? extends DecomposedConstraintCloneableInterface> constructor = (Constructor<? extends DecomposedConstraintCloneableInterface>) 
				this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable.class, ViolationMeasure.class);

		IntVarCloneable costVar = this.newIntVar();
		DecomposedConstraintCloneableInterface constraint = constructor.newInstance(
				this.newIntVarArray(2, 0, 1), 
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				costVar, 
				ViolationMeasure.VALUE_BASED
				);
		
		this.problem.addSoftDecompConstraint(constraint, costVar);
		
		this.solve();
	}

	/** Test method for NetworkFlow Constraints
	 * @throws Exception if something goes wrong
	 */
	public void test_NetworkFlow () throws Exception {

		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(NetworkBuilder.class);

		IntVarCloneable costVar = this.newIntVar();
		NetworkBuilder builder = new NetworkBuilder (costVar);
		Node source = builder.addNode("source", 2);
		Node sink = builder.addNode("sink", -2);
		builder.addArc(source, sink, 1, this.newIntVar());
		
		Constraint constraint = constructor.newInstance(builder);
		
		this.problem.addSoftConstraint(constraint, costVar);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntVar[], IntVar[], IntVar[]) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVarArray_IntVarArray_IntVarArray () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class);

		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				this.newIntVarArray(2)
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for DecomposedConstraints of constructor signature (IntVar[], IntVar[], IntVar[], IntVar[]) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArray_IntVarArray_IntVarArray_IntVarArray_Decomposed () throws Exception {
		
		Constructor<? extends DecomposedConstraintCloneableInterface> constructor = (Constructor<? extends DecomposedConstraintCloneableInterface>) 
		this.constClass.getConstructor(IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class);

		DecomposedConstraintCloneableInterface constraint = constructor.newInstance(
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				this.newIntVarArray(2), 
				this.newIntVarArray(2)
				);
		
		this.problem.addDecompConstraint(constraint);
		
		this.solve();
	}

	/** Test method for Constraints of constructor signature (IntVar[], IntVar[], IntVar[], IntVar[], ArrayList<ArrayList<Integer>>, ArrayList<? extends IntVar>) 
	 * @throws Exception if something goes wrong
	 */
	public void test_DisjointConditional () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(
						IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class, IntVarCloneable[].class, List.class, ArrayList.class);
		
		// The first (and only) 2 rectangles conditionally overlap
		List<List<Integer>> exceptionIndices = new ArrayList<List<Integer>> (1);
		ArrayList<Integer> tuple = new ArrayList<Integer> (1);
		tuple.add(0);
		tuple.add(1);
		exceptionIndices.add(tuple);
		
		ArrayList<IntVarCloneable> exceptionConditions = new ArrayList<IntVarCloneable> (1);
		exceptionConditions.add(this.newIntVar(0, 1));
		
		Constraint constraint = constructor.newInstance(
				this.newIntVarArray(2, 0, 1), 
				this.newIntVarArray(2, 0, 1), 
				this.newIntVarArray(2, 0, 1), 
				this.newIntVarArray(2, 0, 1), 
				exceptionIndices, 
				exceptionConditions
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}

	/** Test method for DecomposedConstraints of constructor signature (IntVar[][]) 
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVarArrayArray () throws Exception {
		
		Constructor<? extends DecomposedConstraintCloneableInterface> constructor = 
				(Constructor<? extends DecomposedConstraintCloneableInterface>) this.constClass.getConstructor(IntVarCloneable[][].class);

		DecomposedConstraintCloneableInterface constraint = constructor.newInstance(
				new Object[] { new IntVarCloneable[][] { this.newIntVarArray(2), this.newIntVarArray(2) } } // That's what I would call a Java oddity
				);
		
		this.problem.addDecompConstraint(constraint);
		
		this.solve();
	}

	/** Test method for the Geost constraint
	 * @throws Exception if something goes wrong
	 */
	public void test_Geost () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = (Constructor<? extends Constraint>) 
		this.constClass.getConstructor(GeostObjectCloneable[].class, ExternalConstraintCloneable[].class, ShapeCloneable[].class);
		
		GeostObjectCloneable[] objects = new GeostObjectCloneable [2];
		for (int i = 0; i < 2; i++) 
			objects[i] = new GeostObjectCloneable (i, this.newIntVarArray(2), this.newIntVar(), this.newIntVar(), this.newIntVar(), this.newIntVar());
		
		Constraint constraint = constructor.newInstance(
				objects, 
				new ExternalConstraintCloneable[] { new NonOverlappingCloneable (objects, new int[] { 0, 1, 2 }) }, 
				new ShapeCloneable[] { 	new ShapeCloneable (0, new int[] { 0, 0 }, new int[] { 1, 3 }), 
										new ShapeCloneable (1, new int[] { 0, 0 }, new int[] { 2, 4 }) }
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}
	
	/** Test method for the Knapsack constraint
	 * @throws Exception if something goes wrong
	 */
	public void test_Knapsack () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(int[].class, int[].class, IntVarCloneable[].class, IntVarCloneable.class, IntVarCloneable.class);
		
		IntVarCloneable costVar = this.newIntVar();
		Constraint constraint = constructor.newInstance(
				new int[] { 1, 2, 3 }, 
				new int[] { 1, 2, 3 }, 
				this.newIntVarArray(3), 
				this.newIntVar(), 
				costVar);
		
		this.problem.addSoftConstraint(constraint, costVar);
		
		this.solve();
	}
	
	/** Test method for the Not constraint
	 * @throws Exception if something goes wrong
	 */
	public void test_Not () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(PrimitiveConstraint.class);
		
		this.problem.addConstraint(constructor.newInstance(new XeqCCloneable (this.newIntVar(), 2)));
		
		this.solve();
	}
	
	/** Test method for constraints with constructor signature (PrimitiveConstraint, IntVar)
	 * @throws Exception if something goes wrong
	 */
	public void test_PrimitiveConstraint_IntVar () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(PrimitiveConstraint.class, IntVarCloneable.class);
		
		this.problem.addConstraint(constructor.newInstance(
				new XeqCCloneable (this.newIntVar(), 2), 
				this.newIntVar()));
		
		this.solve();
	}
	
	/** Test method for constraints with constructor signature (IntVar, PrimitiveConstraint)
	 * @throws Exception if something goes wrong
	 */
	public void test_IntVar_PrimitiveConstraint () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(IntVarCloneable.class, PrimitiveConstraint.class);
		
		this.problem.addConstraint(constructor.newInstance(
				this.newIntVar(0, 1), 
				new XeqCCloneable (this.newIntVar(), 2)
				));
		
		this.solve();
	}
	
	/** Test method for constraints with constructor signature (PrimitiveConstraint, PrimitiveConstraint)
	 * @throws Exception if something goes wrong
	 */
	public void test_PrimitiveConstraint_PrimitiveConstraint () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(PrimitiveConstraint.class, PrimitiveConstraint.class);
		
		this.problem.addConstraint(constructor.newInstance(
				new XeqCCloneable (this.newIntVar(), 2), 
				new XeqCCloneable (this.newIntVar(), 2)));
		
		this.solve();
	}
	
	/** Test method for constraints with constructor signature (PrimitiveConstraint, PrimitiveConstraint, PrimitiveConstraint)
	 * @throws Exception if something goes wrong
	 */
	public void test_PrimitiveConstraint_PrimitiveConstraint_PrimitiveConstraint () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(PrimitiveConstraint.class, PrimitiveConstraint.class, PrimitiveConstraint.class);
		
		this.problem.addConstraint(constructor.newInstance(
				new XeqCCloneable (this.newIntVar(), 2), 
				new XeqCCloneable (this.newIntVar(), 2), 
				new XeqCCloneable (this.newIntVar(), 2)));
		
		this.solve();
	}
	
	/** Test method for the Regular constraint
	 * @throws Exception if something goes wrong
	 */
	public void test_Regular () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(FSMCloneable.class, IntVarCloneable[].class);
		
		IntVarCloneable[] vars = this.newIntVarArray(2, 0, 1);
		
		// Build a finite state machine that enforces the two binary variables to be different
		FSMCloneable fsm = new FSMCloneable ();
		
		// 4 states: the init state, two states for the two possible values of the 1st var, and the end state
		FSMState[] states = new FSMState [4];
		for (int i = 0; i < 4; i++) 
			fsm.allStates.add(states[i] = new FSMState ());
		fsm.initState = states[0];
		fsm.finalStates.add(states[3]);
		
		states[0].transitions.add(new FSMTransition (new IntervalDomain (0, 0), states[1])); // var1 = 0 -> state1
		states[0].transitions.add(new FSMTransition (new IntervalDomain (1, 1), states[2])); // var1 = 1 -> state2
		
		states[1].transitions.add(new FSMTransition (new IntervalDomain (1, 1), states[3])); // state1 -> var2 = 1
		
		states[2].transitions.add(new FSMTransition (new IntervalDomain (0, 0), states[3])); // state2 -> var2 = 0
		
		this.problem.addConstraint(constructor.newInstance(fsm, vars));

		this.solve();
	}
	
	/** Test method for the Arithmetic constraint
	 * @throws Exception if something goes wrong
	 */
	public void test_Arithmetic () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<ArithmeticCloneable> constructor = 
				(Constructor<ArithmeticCloneable>) this.constClass.getConstructor();
		
		ArithmeticCloneable cons = constructor.newInstance();
		cons.addSum(this.newIntVarArray(3), this.newIntVar());
		
		this.problem.addDecompConstraint(cons);

		this.solve();
	}
	
	/** Test method for the ValuePreced constraint
	 * @throws Exception if something goes wrong
	 */
	public void test_ValuePrecede () throws Exception {
		
		@SuppressWarnings("unchecked")
		Constructor<? extends Constraint> constructor = 
				(Constructor<? extends Constraint>) this.constClass.getConstructor(int.class, int.class, IntVarCloneable[].class);
		
		Constraint constraint = constructor.newInstance(
				1, 
				2, 
				this.newIntVarArray(2, 0, 2)
				);
		
		this.problem.addConstraint(constraint);
		
		this.solve();
	}
	
	/**@return a JaCoP variable */
	private IntVarCloneable newIntVar() {
		return this.newIntVar(this.min, this.max);
	}

	/**
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return a JaCoP variable 
	 * 
	 * @todo All variables don't need to be owned by agents; some can be auxiliary variables that are hidden inside the spaces
	 */
	private IntVarCloneable newIntVar(int min, int max) {
		
		String varName = "v_" + (this.varID++);
		IntVarCloneable out = new IntVarCloneable (this.store, varName, min, max);
		this.problem.addVariable(out, "agent_" + varName);
		
		return out;
	}

	/** Constructs an array of new IntVars
	 * @param size 	the size of the array
	 * @return the array
	 */
	private IntVarCloneable[] newIntVarArray(final int size) {
		return this.newIntVarArray(size, this.min, this.max);
	}
	
	/** Constructs an array of new IntVars
	 * @param size 	the size of the array
	 * @param min 	the minimum variable domain value
	 * @param max 	the maximum variable domain value
	 * @return the array
	 */
	private IntVarCloneable[] newIntVarArray(final int size, int min, int max) {
		
		IntVarCloneable[] out = new IntVarCloneable [size];
		for (int i = 0; i < size; i++) {
			String varName = "v_" + (this.varID++);
			this.problem.addVariable(out[i] = new IntVarCloneable (this.store, varName, min, max), "agent_" + varName);
		}
		
		return out;
	}
}
