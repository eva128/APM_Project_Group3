package ee.ut.cs.dsg.confcheck;

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.alignment.Move;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.trie.TrieNode;
import flanagan.analysis.Stat;

import java.util.*;

public class RandomConformanceChecker extends ConformanceChecker{



    private boolean optForLongerSubstrings=false;
    private boolean verbose = false;
    private int numTrials=0;
    public RandomConformanceChecker(Trie trie, int logCost, int modelCost, int maxStatesInQueue) {
        super(trie, logCost, modelCost, maxStatesInQueue);
        rnd = new Random(19);
        inspectedLogTraces = new Trie(trie.getMaxChildren());
    }

    private State pickRandom(State candidateState)
    {

//        toCheck.sort(new Comparator<State>() {
//            @Override
//            public int compare(State o1, State o2) {
//                return o1.compareTo(o2);
//            }
//        });

        //= (State[]) nextChecks.toArray();
//        if (numTrials % cleanseFrequency == 0)
//            successiveHalving();
        int index;
        State s;
        if (cntr % 29 == 0) {

            State[] elements =  new State[nextChecks.size()];
            nextChecks.toArray(elements);





            int upperBound = nextChecks.size();
            int lowerBound = nextChecks.size() - upperBound;
            index = rnd.nextInt( upperBound);

            s = elements[lowerBound +index];
            nextChecks.remove(s);
//            System.out.println("Getting a random state at position "+index +" from a total of "+toCheck.size() +" states.");
            cntr = 0;
        }
        else {
            s = nextChecks.poll();
        }


        return s;
    }
    private void successiveHalving()
    {
//        if (nextChecks.size() < 100000)
//            return  ;
        List<State> result = new ArrayList<>(nextChecks.size()/2);
        State[] elements = new State[nextChecks.size()];
        nextChecks.toArray(elements);

        Arrays.sort(elements);
        int quantile = nextChecks.size()/4;
        nextChecks.clear();
        for(int i = 0; i < quantile*4; i+=2)
        {
            nextChecks.add(elements[i]);
        }
//        for (int i = quantile*2; i < quantile*3; i++)
//        {
//            nextChecks.add(elements[i]);
//        }

    }
    public Alignment check(List<String> trace)
    {

        nextChecks.clear();
        cntr=1;
        traceSize = trace.size();
        maxModelTraceSize = trie.getRoot().getMaxPathLengthToEnd();
        TrieNode node;
        Alignment alg = new Alignment();
        // Speed up by looking up identical log traces first before computing the alignment
//        node = inspectedLogTraces.match(trace);
//        if (node !=null && node.getLevel() == trace.size())
//        {
//            int cost = node.getLinkedTraces().get(0);
//            alg = new Alignment(cost);
//            System.out.println("An identical trace has been seen before, just getting the previous result");
//            return alg;
//        }

        List<String> traceSuffix;
        State state = new State(alg,trace,trie.getRoot(),0);
        nextChecks.add(state);

        State candidateState = null;
        String event;
        numTrials = 1;
//        cleanseFrequency = Math.max(maxTrials/10, 100000);
        cleanseFrequency = maxTrials/20;
        while(nextChecks.size() >0  && numTrials < maxTrials)
        {
            state = pickRandom(candidateState);
            if (state==null)
                continue;
            numTrials++;

            if (numTrials % 100000 == 0 && verbose) {
                System.out.println("Trials so far " + numTrials);
                System.out.println("Queue size "+nextChecks.size());
            }

            event = null;
            traceSuffix = state.getTracePostfix();

            // we have to check what is remaining
            if (traceSuffix.size() == 0 && state.getNode().isEndOfTrace())// We're done
            {
                //return state.getAlignment();

                if (candidateState == null)
                {
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());
                    if(verbose)
                        System.out.println("1-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
//                    cntr=0;
                }
                else if (state.getAlignment().getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(state.getAlignment(), state.getTracePostfix(),state.getNode(), state.getAlignment().getTotalCost());
                    if (verbose)
                        System.out.println("2-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());

//                    cntr=0;
                }

                // candidates.add(candidateState);
//                System.out.println("Remaining alignments to check " +toCheck.size());
//                System.out.println("Best alignment so far " +candidateState.getCostSoFar());
//                return candidateState.getAlignment();
                if (candidateState.getAlignment().getTotalCost()==0)
                    break;
                else
                    continue;
            }
            else if (traceSuffix.size() ==0)
            {
                // we still have model moves to do
                // we should pick the shortest path to an end node
//                System.out.println("Log trace ended! We can only follow the shortest path of the model behavior to the end");
                alg = state.getAlignment();

                node = state.getNode();
                node = node.getChildOnShortestPathToTheEnd();
                while (node != null)
                {
                    Move modelMove = new Move(">>", node.getContent(),1);
                    alg.appendMove(modelMove);
                    node = node.getChildOnShortestPathToTheEnd();
                }
//                System.out.println("Alignment found costs "+alg.getTotalCost());
                if (candidateState == null)
                {
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
                    if(verbose)
                        System.out.println("3-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    cntr=0;
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
                    if (verbose)
                        System.out.println("4-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    cntr=0;
                }
                else
                {
//                    System.out.println("Current alignment is more expensive "+alg.getTotalCost());
                }
                if (candidateState.getAlignment().getTotalCost()==0)
                    break;
                else
                    continue;

            }
            else if (state.getNode().isEndOfTrace() & !state.getNode().hasChildren()) // and no more children
            {
//                System.out.println("Model trace ended! We can only follow the remaining log trace to the end");
                alg = state.getAlignment();
                for (String ev: state.getTracePostfix())
                {
                    Move logMove = new Move(ev, ">>",1);
                    alg.appendMove(logMove);
                }
                if (candidateState == null)
                {
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
                    if(verbose)
                        System.out.println("5-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
//                    cntr=0;
                }
                else if (alg.getTotalCost() < candidateState.getAlignment().getTotalCost())
                {
                    leastCostSoFar = Math.min(leastCostSoFar, candidateState.getAlignment().getTotalCost());
                    candidateState = new State(alg, traceSuffix,null, alg.getTotalCost());
                    if (verbose)
                        System.out.println("6-Better alignment reached with cost "+candidateState.getAlignment().getTotalCost());
//                    System.out.println("Queue size "+toCheck.size());
//                    cntr=0;
                }
                if (candidateState.getAlignment().getTotalCost()==0)
                    break;
                else
                    continue;
            }
            else {
                event = traceSuffix.remove(0);
                node = state.getNode().getChild(event);
            }
            if (node != null && !node.getContent().equals(event)) // just because of hashing collision!
                node = null;
            List<State> newStates = new ArrayList<>();
            if (node != null) // we found a match => synchronous    move
            {
                alg = state.getAlignment();
                TrieNode prev;
                do {
//                    System.out.println("Following sync moves");
                    Move syncMove = new Move(event,event,0);

                    alg.appendMove(syncMove);
                    prev = node;
                    if (traceSuffix.size() > 0)
                    {
                        event = traceSuffix.remove(0);

                        node = node.getChild(event);
                    }
                    else
                    {
                        event = null;
                        node = null;
                    }
                }
                while(node != null);
                // put the event back that caused non sync move
                if (event != null)
                    traceSuffix.add(0,event);

                //go strait with sync moves as there are



                State syncState;
                int cost = 0;
//                if (!optForLongerSubstrings)
//                    cost= maxModelTraceSize + traceSize -(state.getNode().getLevel() + (traceSize-traceSuffix.size()) ) - (alg.getMoves().size() - alg.getTotalCost());
                syncState = new State(alg,traceSuffix,prev,cost);
                addStateToTheQueue(syncState, candidateState);
//

//                if(!optForLongerSubstrings) {
//
//                    newStates.add(handleLogMove(traceSuffix, state, candidateState, event));
//                    newStates.addAll(handleModelMoves(traceSuffix, state, candidateState));
//
//
//                }

            }
            // On 27th of May 2021. we need to give the option to a log move as well as a model move
            else // there is no match, we have to make the model move and the log move
            {
                // let make the log move if there are still more moves


                newStates.add(handleLogMove(traceSuffix, state, candidateState, event));
                newStates.addAll(handleModelMoves(traceSuffix, state, candidateState));
            }

            //Now randomly add those states to the queue so that if they have the same cost we can pick them differently
            int size = newStates.size();
            for (int i = size; i >0;i--)
            {
                State s = newStates.get(rnd.nextInt(i));
                if (s != null)
                    addStateToTheQueue(s, candidateState);
            }
        }
//        if (candidateState != null)
//            inspectedLogTraces.addTrace(trace, candidateState.getAlignment().getTotalCost());
        if(verbose)
            System.out.println(String.format("Queue Size %d and num trials %d", nextChecks.size(),numTrials));
        return candidateState != null? candidateState.getAlignment():null;
        //return alg;
    }

    protected void addStateToTheQueue(State state, State candidateState) {

//        if (seenBefore.contains(state)) {
//            System.out.println("This state has been seen before, skipping it...");
//            return;
//        }
//        else
//            seenBefore.add(state);
//        if (state.getCostSoFar() < 0)
//            return;
//        if (cntr==maxStatesInQueue) {
//            System.out.println("Max queue size reached. New state is not added!");
//            return;
//        }
        cntr++;
        if (nextChecks.size() == maxStatesInQueue)
        {
//            if (verbose)
//                System.out.println("Max queue size reached. New state is not added!");
           if (state.getCostSoFar() < nextChecks.peek().getCostSoFar())
            // if (state.getAlignment().getTotalCost() < nextChecks.peek().getAlignment().getTotalCost())
            {
//                System.out.println(String.format("Adding a good candidate whose cost is %d which is less that the least cost so far %d", state.getAlignment().getTotalCost(), nextChecks.peek().getAlignment().getTotalCost()));
//                System.out.println(String.format("Replacement state suffix length %d, number of model moves %d", state.getTracePostfix().size(), state.getNode().getLevel()));
                nextChecks.poll();
                nextChecks.add(state);
            }
            return;
        }
        if (candidateState != null) {
            if ((state.getAlignment().getTotalCost()+Math.min(Math.abs(state.getTracePostfix().size() - state.getNode().getMinPathLengthToEnd()),Math.abs(state.getTracePostfix().size() - state.getNode().getMaxPathLengthToEnd())))  < candidateState.getAlignment().getTotalCost())// && state.getNode().getLevel() > candidateState.getNode().getLevel())
            {

                nextChecks.add(state);
//                states.add(state);
            }
            else {
//                System.out.println(String.format("State is not promising cost %d is greater than the best solution so far %d",(state.getAlignment().getTotalCost()+Math.abs(state.getTracePostfix().size() - state.getNode().getMinPathLengthToEnd())),candidateState.getAlignment().getTotalCost()) );
//
//                System.out.println("Least cost to check next "+nextChecks.peek().getCostSoFar());
            }
        }
        else //if (state.getCostSoFar()< (nextChecks.size() == 0? Integer.MAX_VALUE: nextChecks.peek().getCostSoFar()))
        {
            nextChecks.add(state);

        }

    }
}