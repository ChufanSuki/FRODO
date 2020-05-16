"""
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
"""

# For each algorithm, this script compares the performance of its current version (in the src/ folder) against its old version (from a JAR)

# Add the FRODO benchmarks folder to the Python path and import the frodo2 module
import sys
sys.path.append("..")
import frodo2

# The command to call java and the JVM parameters
root = "../../../../"
java = "java"
javaParamsOld = [
			"-Xmx2G", 
			"-classpath", root + "frodo2.18.1.jar", # includes a (presumably older) version of FRODO from a JAR
			]
javaParamsNew = [
			"-Xmx2G", 
			"-classpath", root + "bin:" + root + "lib/jacop-4.7.0.jar:" + root + "lib/jdom-2.0.6.jar", # includes the current version of FRODO
			]

# Partly define the problem generator (the input parameters will depend on the algorithm)
generator = "frodo2.benchmarks.maxdiscsp.MaxDisCSPProblemGenerator"
problemFile = "random_Max-DisCSP.xml"

# Define the experiments to run, and on which problem sizes
# Each experiment is [algoName, genParams, [algo_version_1, algo_version_2...]]
# where algo_version_i is [algoName, solverClassName, agentConfigFilePath, inputProblemFilePath, javaParams]
experiments = [
	["ADOPT", [10, 10, .4, [.4, .5, .6, .7, .8, .9]], [
		["ADOPT 2.18", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagent.xml", problemFile, javaParamsOld], 
		["ADOPT 2.x", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagent.xml", problemFile, javaParamsNew], 
			]], 
	["ADOPT+JaCoP", [10, 10, .4, [.4, .5, .6, .7, .8]], [
		["ADOPT+JaCoP 2.18", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagentJaCoP.xml", problemFile, javaParamsOld], 
		["ADOPT+JaCoP 2.x", "frodo2.algorithms.adopt.ADOPTsolver", root + "agents/ADOPT/ADOPTagentJaCoP.xml", problemFile, javaParamsNew], 
			]], 
	["AFB", [11, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["AFB 2.18", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagent.xml", problemFile, javaParamsOld], 
		["AFB 2.x", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagent.xml", problemFile, javaParamsNew], 
			]],
	["AFB+JaCoP", [11, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["AFB+JaCoP 2.18", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagentJaCoP.xml", problemFile, javaParamsOld], 
		["AFB+JaCoP 2.x", "frodo2.algorithms.afb.AFBsolver", root + "agents/AFB/AFBagentJaCoP.xml", problemFile, javaParamsNew], 
			]],
	["DPOP", [12, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["DPOP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagent.xml", problemFile, javaParamsOld], 
		["DPOP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagent.xml", problemFile, javaParamsNew], 
			]], 
	["DPOP+JaCoP", [10, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
		["DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
			]], 
	["ASO-DPOP", [10, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["ASO-DPOP 2.18", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagent.xml", problemFile, javaParamsOld], 
		 ["ASO-DPOP 2.x", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagent.xml", problemFile, javaParamsNew], 
			]], 
	["ASO-DPOP+JaCoP", [10, 8, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["ASO-DPOP+JaCoP 2.18", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
		 ["ASO-DPOP+JaCoP 2.x", "frodo2.algorithms.asodpop.ASODPOPsolver", root + "agents/DPOP/ASO-DPOP/ASO-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
			]], 
	["MB-DPOP", [12, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["MB-DPOP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagent.xml", problemFile, javaParamsOld], 
		 ["MB-DPOP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagent.xml", problemFile, javaParamsNew], 
			 ]], 
	["MB-DPOP+JaCoP", [11, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["MB-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
		 ["MB-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.DPOPsolver", root + "agents/DPOP/MB-DPOP/MB-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
			 ]], 
	["O-DPOP", [11, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		 ["O-DPOP 2.18", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagent.xml", problemFile, javaParamsOld], 
		 ["O-DPOP 2.x", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagent.xml", problemFile, javaParamsNew], 
			]], 
	["O-DPOP+JaCoP", [10, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		 ["O-DPOP+JaCoP 2.18", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
		 ["O-DPOP+JaCoP 2.x", "frodo2.algorithms.odpop.ODPOPsolver", root + "agents/DPOP/O-DPOP/O-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
			]], 
	["P-DPOP", [10, 5, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["P-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagent.xml", problemFile, javaParamsOld], 
		["P-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagent.xml", problemFile, javaParamsNew], 
			]],
	["P-DPOP+JaCoP", [10, 5, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["P-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
		["P-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P_DPOPsolver", root + "agents/DPOP/P-DPOP/P-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
			]],
	["P1.5-DPOP", [7, 7, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["P3/2-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagent.xml", problemFile, javaParamsOld], 
		["P3/2-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagent.xml", problemFile, javaParamsNew], 
			]],
	["P1.5-DPOP+JaCoP", [7, 7, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["P3/2-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagentJaCoP.xml", problemFile, javaParamsOld], 
		["P3/2-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P3halves_DPOPsolver", root + "agents/DPOP/P-DPOP/P1.5-DPOPagentJaCoP.xml", problemFile, javaParamsNew], 
			]],
	["P2-DPOP", [5, 5, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["P2-DPOP 2.18", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagent.xml", problemFile, javaParamsOld],
		["P2-DPOP 2.x", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagent.xml", problemFile, javaParamsNew],
			 ]],
	["P2-DPOP+JaCoP", [5, 5, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["P2-DPOP+JaCoP 2.18", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagentJaCoP.xml", problemFile, javaParamsOld],
		["P2-DPOP+JaCoP 2.x", "frodo2.algorithms.dpop.privacy.P2_DPOPsolver", root + "agents/DPOP/P-DPOP/P2-DPOPagentJaCoP.xml", problemFile, javaParamsNew],
			 ]],
    ["DUCT", [30, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
        ["DUCT 2.18", "frodo2.algorithms.duct.DUCTsolver", root + "agents/DUCT/DUCTagent.xml", problemFile, javaParamsOld], 
        ["DUCT 2.x", "frodo2.algorithms.duct.DUCTsolver", root + "agents/DUCT/DUCTagent.xml", problemFile, javaParamsNew], 
             ]],
	["DSA", [100, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		 ["DSA 2.18", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagent.xml", problemFile, javaParamsOld], 
		["DSA 2.x", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagent.xml", problemFile, javaParamsNew], 
			 ]],
	["DSA+JaCoP", [50, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["DSA+JaCoP 2.18", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagentJaCoP.xml", problemFile, javaParamsOld], 
		["DSA+JaCoP 2.x", "frodo2.algorithms.localSearch.dsa.DSAsolver", root + "agents/DSA/DSAagentJaCoP.xml", problemFile, javaParamsNew], 
			 ]],
	["MaxSum", [20, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["MaxSum 2.18", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgent.xml", problemFile, javaParamsOld], 
		["MaxSum 2.x", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgent.xml", problemFile, javaParamsNew], 
			]],
	["MaxSum+JaCoP", [11, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["MaxSum+JaCoP 2.18", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentJaCoP.xml", problemFile, javaParamsOld], 
		["MaxSum+JaCoP 2.x", "frodo2.algorithms.maxsum.MaxSumSolver", root + "agents/MaxSum/MaxSumAgentJaCoP.xml", problemFile, javaParamsNew], 
			]],
	["MGM", [100, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["MGM 2.18", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagent.xml", problemFile, javaParamsOld], 
		["MGM 2.x", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagent.xml", problemFile, javaParamsNew], 
			]],
	["MGM+JaCoP", [50, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["MGM+JaCoP 2.18", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagentJaCoP.xml", problemFile, javaParamsOld], 
		["MGM+JaCoP 2.x", "frodo2.algorithms.localSearch.mgm.MGMsolver", root + "agents/MGM/MGMagentJaCoP.xml", problemFile, javaParamsNew], 
			]],
	["MGM2", [100, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		 ["MGM2 2.18", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agent.xml", problemFile, javaParamsOld], 
		["MGM2 2.x", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agent.xml", problemFile, javaParamsNew], 
			 ]],
	["MGM2+JaCoP", [10, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["MGM2+JaCoP 2.18", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agentJaCoP.xml", problemFile, javaParamsOld], 
		["MGM2+JaCoP 2.x", "frodo2.algorithms.localSearch.mgm.mgm2.MGM2solver", root + "agents/MGM/MGM2agentJaCoP.xml", problemFile, javaParamsNew], 
			 ]],
	["SynchBB", [11, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["SynchBB 2.18", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagent.xml", problemFile, javaParamsOld], 
		["SynchBB 2.x", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagent.xml", problemFile, javaParamsNew], 
			]],
	["SynchBB+JaCoP", [10, 10, .4, [.4, .5, .6, .7, .8, .9, .99]], [
		["SynchBB+JaCoP 2.18", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagentJaCoP.xml", problemFile, javaParamsOld], 
		["SynchBB+JaCoP 2.x", "frodo2.algorithms.synchbb.SynchBBsolver", root + "agents/SynchBB/SynchBBagentJaCoP.xml", problemFile, javaParamsNew], 
			]],
]
timeout = 500 # in seconds
nbrProblems = 101

# Run each experiment nbrProblems times
outputs = [] # the names of the CSV output files
interrupted = False
for i in range(0, nbrProblems):
	print("\n" + str(i + 1) + "/" + str(nbrProblems))
	
	# Run each experiment
	for exp in experiments: 
		if not interrupted: 
			print("\n" + exp[0]) # prints the algorithm
		
		# The CSV file to which the statistics should be written
		output = exp[0] + ".csv"
		outputs += [output]
		
		# Run the experiment
		if not interrupted: 
			interrupted = frodo2.run(java, javaParamsNew, generator, exp[1], 1, exp[2], timeout, output, saveProblems = False)

	if interrupted: 
		break

# Plot the results for each experiment
inputCol = 9 # tightness (p2)
for j in range(0, len(experiments)): 
	exp = experiments[j]

	# The CSV file to which the statistics should have been written
	output = outputs[j]

	# Plot curves with x = problem size and y = performance of each algorithm
#	 frodo2.plot(output, xCol = inputCol, yCol = 15, block = False) # 15 = message size
#	 frodo2.plot(output, xCol = inputCol, yCol = 13, block = (j == len(experiments)-1)) # 13 = runtime

	# Scatter plot with one data point per instance, x = old algorithm and y = new algorithm
	xAlgo = exp[2][0][0]
	yAlgo = exp[2][1][0]
	frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 19, inputCol = inputCol, timeouts = False, block = False, loglog = False, annotate = False) # 19 = solution quality 
	frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 15, inputCol = inputCol, timeouts = True, block = False, annotate = False) # 15 = message size
	frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 15, inputCol = inputCol, timeouts = False, block = False, annotate = False) # 15 = message size
	frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 13, inputCol = inputCol, timeouts = True, block = False, annotate = False) # 13 = runtime
	frodo2.plotScatter(output, xAlgo, yAlgo, metricsCol = 13, inputCol = inputCol, timeouts = False, block = (j == len(experiments)-1), annotate = False) # 13 = runtime
