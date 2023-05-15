package ee.ut.cs.dsg.confcheck;

/**
 * This is our edited Runner.java class. The original Runner class is not well structured (hard to read) and the code is not runnable without doing a lot of changes.
 * Hence, we edited the class to allow a smooth running experience without needing to change lines depending on which data is used.
 * This is especially useful for 2015 + sepsis data.
 *
 * How to run:
 * Use Java Jdk 1.8 or add dependency in Pom (for Java Jdk above 1.8)
 * Change myPaths: C://Users//....
 *
 */

import ee.ut.cs.dsg.confcheck.alignment.Alignment;
import ee.ut.cs.dsg.confcheck.trie.Trie;
import ee.ut.cs.dsg.confcheck.util.AlphabetService;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logfiltering.algorithms.ProtoTypeSelectionAlgo;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.PrototypeType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SimilarityMeasure;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.SamplingReturnType;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static ee.ut.cs.dsg.confcheck.util.Configuration.ConformanceCheckerType;
import static ee.ut.cs.dsg.confcheck.util.Configuration.LogSortType;

public class Runner_OnlyRelevantCode {

    // Global variables. Change to edit configuration.
    public static final String myPath = "C:\\Users\\ebeha\\Documents\\Uni Mannheim\\3. Semester\\IE 692 Advanced Process Mining\\Group Project\\";
    public static final int maxTrials = 100_000; // iterations for tree-based approach (parameter)

    private static double totalCostsBaseline = 0;
    private static int totalCostsTree = 0;

    private static AlphabetService service;

    public static void main(String... args)
    {
        // 2015
        String randomProxyLog = myPath + "\\BPI2015\\randomLog.xml";
        String clusteredLog = myPath + "\\BPI2015\\sampledClusteredLog.xml";
        String simulatedLog = myPath + "\\BPI2015\\simulatedLog.xml";
        String reducedActivityLog = myPath + "\\BPI2015\\reducedLogActivity.xml";
        String frequencyActivityLog = myPath +"\\BPI2015\\frequencyLog.xml";
        String sampleLog = myPath + "\\BPI2015\\sampledLog.xml";
        String singular = myPath + "\\BPI2015\\Singular.xes";

        // Sepsis
        String randomSepsisProxyLog = myPath + "\\Sepsis\\randomLog.xml";
        String clusteredSepsisLog = myPath + "\\Sepsis\\sampledClusteredLog.xml";
        String simulatedSepsisLog = myPath + "\\Sepsis\\simulatedLog.xml";
        String frequencySepsisLog = myPath + "\\Sepsis\\frequencyLog.xml";
        String reducedSepsisActivityLog = myPath + "\\Sepsis\\reducedLogActivity.xml";
        String sampleSepsisLog = myPath + "\\Sepsis\\sampledLog.xml";

        // BPI 2019
        String originalLog2019 = myPath + "\\BPI2019\\BPI_Challenge_2019.xml";
        String random2019ProxyLog = myPath +"\\BPI2019\\randomLog.xml";
        String clustered2019Log = myPath + "\\BPI2019\\sampledClusteredLog.xml";
        String simulated2019Log = myPath + "\\BPI2019\\simulatedLog.xml";
        String reduced2019ActivityLog = myPath + "\\BPI2019\\reducedLogActivity.xml";
        String sample2019Log = myPath + "\\BPI2019\\sampledLog.xml";
        String frequency2019Log = myPath + "\\BPI2019\\frequencyLog.xml";

        // BPI 2012
        String originalLog2012 = myPath + "\\BPI2012\\BPIC2012.xes";
        String random2012ProxyLog = myPath + "\\BPI2012\\randomLog.xml";
        String clustered2012Log = myPath + "\\BPI2012\\sampledClusteredLog.xml";
        String simulated2012Log = myPath + "\\BPI2012\\simulatedLog.xml";
        String reduced2012ActivityLog = myPath + "\\BPI2012\\reducedLogActivity.xml";
        String sample2012Log = myPath + "\\BPI2012\\sampledLog.xml";
        String frequency2012Log = myPath + "\\BPI2012\\frequencyLog.xml";

        // BPI 2017
        String originalLog2017 = myPath + "\\BPI2017\\BPIC2017.xes.xes";
        String random2017ProxyLog = myPath + "\\BPI2017\\rand_randomLog.xml";
        String clustered2017Log = myPath + "\\BPI2017\\sampledClusteredLog.xml";
        String simulated2017Log = myPath + "\\BPI2017\\simulatedLog.xml";
        String reduced2017ActivityLog = myPath + "\\BPI2017\\reducedLogActivity.xml";
        String sample2017Log = myPath + "\\BPI2017\\sampledLog.xml";
        String frequency2017Log = myPath + "\\BPI2017\\freq_frequencyLog.xml";

        // Additional data investigation (Group 3)
        String dataNatalie = myPath+ "\\Group3\\PrepaidTravelCost.xml";
        String dataNatalieBPI2018 = myPath+ "\\Group3\\BPI2018.xes";
        String dataNatalieAalst = myPath+ "\\Group3\\Aalst\\review_example_large.xml";
        String dataNatalieCf = myPath+ "\\Group3\\cf\\Chapter_7\\Lfull.xml"; // https://processmining.org/old-version/event-book.html
        String dataNatalieHos = myPath+ "\\Group3\\hos\\Hospital_log.xes";

        /** Run one of these approaches. Change parameter to run different data and approach:
        First parameter must be clustered, simulated, frequency, random, reduced of any year. This is the proxy log which will be used to construct the tree.
        Second parameter must be a sampleLog of any year. The years of first and second parameter must fit.
        Third parameter must be ConformanceCheckerType.TRIE_RANDOM_STATEFUL or ConformanceCheckerType.TRIE_RANDOM for tree-based approach
        or ConformanceCheckerType.DISTANCE to run baseline approach.
        Last parameter does not change results. Just for preferred sorted output. */

        String modelLog = frequency2017Log;
        String inputLog = sample2017Log;

        testOnConformanceApproximationResults(modelLog, inputLog, ConformanceCheckerType.TRIE_RANDOM, LogSortType.NONE);

        /**
         * Also run those lines to be able to compare the distance-based and tree based approach against each other.
         * Then the MAE get calculated.
        */
        testOnConformanceApproximationResults(modelLog, inputLog, ConformanceCheckerType.DISTANCE, LogSortType.NONE);
        // compute MAE of tree-based approach and distance-based approach
        double mae = (totalCostsBaseline - totalCostsTree) / 101;
        System.out.println("Alignment costs tree-based approach: " + totalCostsTree);
        System.out.println("Alignment costs distance-based approach: " + totalCostsBaseline);
        System.out.println("Mean absoulte error: " + mae);


//        // BPI 2015
//        printLogStatistics(simulatedLog);
////        printLogStatistics(sampleLog);
//        printLogStatistics(clusteredLog);
//        printLogStatistics(randomProxyLog);
//        printLogStatistics(frequencyActivityLog);
//        printLogStatistics(reducedActivityLog);

//        // BPI 2012
//        printLogStatistics(simulated2012Log);
////        printLogStatistics(sample2012Log);
//        printLogStatistics(clustered2012Log);
//        printLogStatistics(random2012ProxyLog);
//        printLogStatistics(frequency2012Log);
//        printLogStatistics(reduced2012ActivityLog);

//        // BPI 2017
//        printLogStatistics(simulated2017Log);
////        printLogStatistics(sample2017Log);
//        printLogStatistics(clustered2017Log);
//        printLogStatistics(random2017ProxyLog);
//        printLogStatistics(frequency2017Log);
//        printLogStatistics(reduced2017ActivityLog);

//        // BPI 2019
//        printLogStatistics(simulated2019Log);
////        printLogStatistics(sample2019Log);
//        printLogStatistics(clustered2019Log);
//        printLogStatistics(random2019ProxyLog);
//        printLogStatistics(frequency2019Log);
//        printLogStatistics(reduced2019ActivityLog);

        //SEPSIS
//        printLogStatistics(simulatedSepsisLog);
////        printLogStatistics(sampleSepsisLog);
//        printLogStatistics(clusteredSepsisLog);
//        printLogStatistics(randomSepsisProxyLog);
//        printLogStatistics(frequencySepsisLog);
//        printLogStatistics(reducedActivityLog);
    }


    private static Pnml importPnmlFromStream(InputStream input) throws
            XmlPullParserException, IOException {
        FullPnmlElementFactory pnmlFactory = new FullPnmlElementFactory();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(input, null);
        int eventType = xpp.getEventType();
        Pnml pnml = new Pnml();
        synchronized (pnmlFactory) {
            pnml.setFactory(pnmlFactory);
            /*
             * Skip whatever we find until we've found a start tag.
             */
            while (eventType != XmlPullParser.START_TAG) {
                eventType = xpp.next();
            }
            /*
             * Check whether start tag corresponds to PNML start tag.
             */
            if (xpp.getName().equals(Pnml.TAG)) {
                /*
                 * Yes it does. Import the PNML element.
                 */
                pnml.importElement(xpp, pnml);
            } else {
                /*
                 * No it does not. Return null to signal failure.
                 */
                pnml.log(Pnml.TAG, xpp.getLineNumber(), "Expected pnml");
            }
            if (pnml.hasErrors()) {
                return null;
            }
            return pnml;
        }
    }

    private static void init()
    {
        service = new AlphabetService();
    }


    private static void printLogStatistics(String inputLog)
    {
        init();
        long startTs = System.currentTimeMillis();
        Trie t = constructTrie(inputLog);
        long endTs = System.currentTimeMillis();

        System.out.println(String.format("Stats for trace from %s", inputLog));
        System.out.println(String.format("Max length of a trace %d", t.getMaxTraceLength()));
        System.out.println(String.format("Min length of a trace %d", t.getMinTraceLength()));
        System.out.println(String.format("Avg length of a trace %d", t.getAvgTraceLength()));
        System.out.println(String.format("Number of nodes in the trie %d", t.getSize()));
        System.out.println(String.format("Total number of events %d", t.getNumberOfEvents()));
        System.out.println(String.format("Trie construction time %d ms", (endTs-startTs)));
    }

    /** 1) */
    /**
     *
     * @param inputProxyLogFile We create the trie with this file, this represents our Model M'
     * @param inputSampleLogFile This is our log with the traces and events. E.g. this contains <a,b,e> and we check the alignment cost for this trace.
     * @param confCheckerType
     * @param sortType
     */
    private static void testOnConformanceApproximationResults(String inputProxyLogFile, String inputSampleLogFile, ConformanceCheckerType confCheckerType, LogSortType sortType)
    {
        init();
        Trie trie = constructTrie(inputProxyLogFile);

        //Configuration variables
        XLog inputSamplelog;
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        XesXmlParser parser = new XesXmlParser();

        try{
            InputStream is = new FileInputStream(inputSampleLogFile);
            inputSamplelog = parser.parse(is).get(0);


            List<String> templist = new ArrayList<>();
            List<String> tracesToSort = new ArrayList<>();

            // Use trie to create checker
            ConformanceChecker checker; // default: TRIE_RANDOM_STATEFUL
            if (confCheckerType == ConformanceCheckerType.TRIE_PREFIX)
                          checker = new PrefixConformanceChecker(trie,1,1, false);
            else if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM)
                checker = new RandomConformanceChecker(trie,1,1, 50000, maxTrials);// --> it is 10mio but should be 100k !!!
//                checker = new RandomConformanceChecker(trie,1,1, 50000, 10_000_000);// --> it is 10mio but should be 100k !!!
            else if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM_STATEFUL)
                checker = new StatefulRandomConformanceChecker(trie,1,1, 50000, maxTrials);
//                checker = new StatefulRandomConformanceChecker(trie,1,1, 50000, 1_000_000);
            else
            {
                testVanellaConformanceApproximation(inputProxyLogFile,inputSampleLogFile);
                return;
            }

            // Only tree based conformance checking here:
            Alignment alg;
            HashMap<String, Integer> sampleTracesMap = new HashMap<>();
            long start;
            long totalTime=0;
            int skipTo =0;
            int current = -1;
            int takeTo = 100;
            DeviationChecker devChecker = new DeviationChecker(service);
            int cnt = 1;
            for (XTrace trace : inputSamplelog)
            {
                current++;
                if (current < skipTo)
                    continue;
                if (current> takeTo)
                    break;
                templist = new ArrayList<String>();

                for (XEvent e: trace)
                {
                    String label = "";
                    if (inputSamplelog.getClassifiers().size() == 0) { // Group3: for 2015 + sepsis data
                        label = e.getAttributes().get(XLogInfoImpl.NAME_CLASSIFIER.getDefiningAttributeKeys()[0]).toString();
                    }
                    else {
                        label = e.getAttributes().get(inputSamplelog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
                    }

                    templist.add(Character.toString(service.alphabetize(label)));
                }

                StringBuilder sb = new StringBuilder(templist.size());
                sb.append(cnt).append((char)63); // we prefix the trace with its ID

                Arrays.stream(templist.toArray()).forEach( e-> sb.append(e));

                sampleTracesMap.put(sb.toString(),cnt);
                cnt++;

                tracesToSort.add(sb.toString());
            }

//            // i added this
//            if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM) {
//                if (sortType == LogSortType.TRACE_LENGTH_ASC || sortType == LogSortType.TRACE_LENGTH_DESC)
//                    tracesToSort.sort(Comparator.comparingInt(String::length));
//            }

            if (confCheckerType == ConformanceCheckerType.TRIE_RANDOM_STATEFUL) {

                if (sortType == LogSortType.TRACE_LENGTH_ASC || sortType == LogSortType.TRACE_LENGTH_DESC)
                    tracesToSort.sort(Comparator.comparingInt(String::length));
                else if (sortType == LogSortType.LEXICOGRAPHIC_ASC || sortType == LogSortType.LEXICOGRAPHIC_DESC)
                    Collections.sort(tracesToSort);
            }

            System.out.println("Trace#, Alignment cost");

            if (sortType == LogSortType.LEXICOGRAPHIC_DESC || sortType == LogSortType.TRACE_LENGTH_DESC)
            {
                for (int i = tracesToSort.size() -1; i>=0; i--)
                {
                    totalTime = computeAlignment(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i);
                }
            }
//
            else {
                for (int i = 0; i < tracesToSort.size(); i++) {
                    totalTime = computeAlignment(tracesToSort, checker, sampleTracesMap, totalTime, devChecker, i);
                }
            }


            System.out.println(String.format("Time taken for trie-based conformance checking %d milliseconds",totalTime));

//            for (String label: devChecker.getAllActivities())
//            {
//                System.out.println(String.format("%s, %f",label, devChecker.getDeviationPercentage(label)));
//            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /** 4) */
    private static long computeAlignment(List<String> tracesToSort, ConformanceChecker checker, HashMap<String, Integer> sampleTracesMap, long totalTime, DeviationChecker devChecker, int i) {
        long start;
        Alignment alg;
        List<String> trace = new ArrayList<String>();

        int pos = tracesToSort.get(i).indexOf((char)63);
        int traceNum = Integer.parseInt(tracesToSort.get(i).substring(0,pos)); // trace#
        String actualTrace = tracesToSort.get(i).substring(pos+1); // e.g. ABCDEFGHPIJMKLIJMKLIJMKILJMKLPIKJMQLKILEJMKLIJMKLQKLIJMKL
//        System.out.println(actualTrace);
        for (char c : actualTrace.toCharArray()) {
            trace.add(new StringBuilder().append(c).toString()); // adds every activity (e.g. 'A') to the List<String> trace
        }
        start = System.currentTimeMillis();
        alg = checker.check(trace); // alg contains the number of synchronous and asynchronous moves, the cost for each move, total cost IMPORTANT
        totalTime += System.currentTimeMillis() - start;
        if (alg != null) {
            System.out.print(sampleTracesMap.get(tracesToSort.get(i))); // i is i. trace. this prints the first column: trace# number
            System.out.println(", " + alg.getTotalCost());

            totalCostsTree = totalCostsTree + alg.getTotalCost();

//                        System.out.println(alg.toString(service));
//            devChecker.processAlignment(alg);
//                    System.out.println(alg.toString());
//                        t.printTraces();
        } else //if (usePrefixChecker == false)
            System.out.println("Couldn't find an alignment under the given constraints");
        return totalTime;
    }

    /** 3) */
    private static XLog loadLog(String inputProxyLogFile)
    {
        XLog inputProxyLog;//, inputSamplelog;
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        XesXmlParser parser = new XesXmlParser();

        try {
            InputStream is = new FileInputStream(inputProxyLogFile);
            inputProxyLog = parser.parse(is).get(0);
            // XLogInfo logInfo = inputProxyLog.getInfo(eventClassifier);
            // logInfo = XLogInfoFactory.createLogInfo(inputProxyLog, inputProxyLog.getClassifiers().get(0));
            return inputProxyLog;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /** 2) */
    private static Trie constructTrie(String inputProxyLogFile)
    {
        XLog inputProxyLog = loadLog(inputProxyLogFile);
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
//        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER; // It seems, Name and standard classifier do the same. Do they also do the same as Activitiy Classifier? Yes

        try {
            XLogInfo logInfo ;
            if (inputProxyLog.getClassifiers().size() == 0) { // Group3: needed to run 2015 data and other data that does not work otherwise
                logInfo = XLogInfoFactory.createLogInfo(inputProxyLog, eventClassifier);
            } else { // run every other data
                logInfo = XLogInfoFactory.createLogInfo(inputProxyLog, XLogInfoImpl.NAME_CLASSIFIER);
            }

            int count = 0;
            for (XEventClass clazz : logInfo.getNameClasses().getClasses()) {
                count++;
//                        System.out.println(clazz.toString()); // e.g. A_ACTIVATED
            }
//            System.out.println("Number of unique activities " + count);

            //Let's construct the trie from the proxy log
            Trie t = new Trie(count);
            List<String> templist;
//            count=1;
            count=1;
//            System.out.println("Proxy log size "+inputProxyLog.size());
            for (XTrace trace : inputProxyLog) {
                templist = new ArrayList<String>();
                for (XEvent e : trace) {
                    String label = e.getAttributes().get(eventClassifier.getDefiningAttributeKeys()[0]).toString();
                    if (inputProxyLog.getClassifiers().size() == 0) { // Group3: if 2015 data, then use this line
                        label = e.getAttributes().get(eventClassifier.getDefiningAttributeKeys()[0]).toString();
                    } else {
                        label = e.getAttributes().get(inputProxyLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
                    }

                    templist.add(Character.toString(service.alphabetize(label)));
                }
//                System.out.println(templist.toString());
                if (templist.size() > 0 ) {
                    t.addTrace(templist);

//                    if (count == 37)
//                    StringBuilder sb = new StringBuilder();
//                    templist.stream().forEach(e -> sb.append(e));
//                    System.out.println(sb.toString());

                }
                count++;
            }
            return t;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void testVanellaConformanceApproximation(String inputProxyLogFile, String inputSampleLogFile)
    {
        XLog proxyLog, sampleLog;
        StringBuilder sb;
        List<String> proxyTraces = new ArrayList<>();
        List<String> sampleTraces = new ArrayList<>();
        proxyLog = loadLog(inputProxyLogFile);
        sampleLog = loadLog(inputSampleLogFile);
        HashMap<String, Integer> sampleTracesMap = new HashMap<>();
        init();

        for (XTrace trace : proxyLog) {
            sb = new StringBuilder();
            for (XEvent e : trace) {
                String label = "";
                if (proxyLog.getClassifiers().size() == 0) { // Group3: for 2015 and some other
                    label = e.getAttributes().get(XLogInfoImpl.NAME_CLASSIFIER.getDefiningAttributeKeys()[0]).toString();
                } else {
                    label = e.getAttributes().get(proxyLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
                }

                sb.append(service.alphabetize(label));
            }
            proxyTraces.add(sb.toString());

        }
        int cnt=1;
        for (XTrace trace : sampleLog) {
            sb = new StringBuilder();
            for (XEvent e : trace) {
                String label = "";
                if (sampleLog.getClassifiers().size() == 0) { // Group3: for 2015 and some other
                    label = e.getAttributes().get(XLogInfoImpl.NAME_CLASSIFIER.getDefiningAttributeKeys()[0]).toString();
                } else {
                    label = e.getAttributes().get(sampleLog.getClassifiers().get(0).getDefiningAttributeKeys()[0]).toString();
                }

                sb.append(service.alphabetize(label)); // e.g. W_Completeren aanvraag -> D
            }
            sampleTraces.add(sb.toString());
            sampleTracesMap.put(sb.toString(),cnt);
            cnt++;
        }
        // sampleTracesMap ist einfach nur eine Übersetzung des ProxyLogs mit langen Activity Namen zu kurzen Activity namen, sodass wir sowas haben wie ABCDDEGFHIJDJQFHIJJJJJJJJJQTJ.

        DeviationChecker deviationChecker = new DeviationChecker(service);
        // Now compute the alignments
        long start=System.currentTimeMillis(),timeTaken=0 ;
        int skipTo =0;
        int current = -1;
        int takeTo = 100;
        try {
            System.out.println("Trace#, Alignment cost");

            for (String logTrace : sampleTraces) {
                current++;
                if (current < skipTo)
                    continue;
                if (current > takeTo)
                    break;
                double minCost = Double.MAX_VALUE;
                String bestTrace = "";
                String bestAlignment = "";
                start = System.currentTimeMillis();
                for (String proxyTrace : proxyTraces) {

                    ProtoTypeSelectionAlgo.AlignObj obj = ProtoTypeSelectionAlgo.levenshteinDistancewithAlignment(logTrace, proxyTrace); // one of sampleLog, one of M'
                    if (obj.cost < minCost) { // nata: obj.cost is alignment cost
                        minCost = obj.cost;
                        bestAlignment = obj.Alignment;
                        bestTrace = proxyTrace;
                        if (obj.cost == 0) // optimal alignment found
                            break;
                    }
                }
                timeTaken += System.currentTimeMillis() - start;
//            System.out.println("Total proxy traces "+proxyTraces.size());
//            System.out.println("Total candidate traces to inspect "+proxyTraces.size());
                //print trace number
                System.out.print(sampleTracesMap.get(logTrace));
                // print cost
                System.out.println(", " + minCost);

                totalCostsBaseline = totalCostsBaseline + minCost;

//            System.out.println(bestAlignment);
//                Alignment alg = AlignmentFactory.createAlignmentFromString(bestAlignment);
//              System.out.println(alg.toString());
//                deviationChecker.processAlignment(alg);
//            System.out.println("Log trace "+logTrace);
//            System.out.println("Aligned trace "+bestTrace);
//            System.out.println("Trace number "+sampleTracesMap.get(bestTrace));
            }
            System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", timeTaken ));

            for (String label: deviationChecker.getAllActivities())
            {
//                System.out.println("DeviationPercentage: "); // natalie: Debug and look at  deviationChecker object to see everything (nonSynchronousMovesCount)
//                System.out.println(String.format("%s, %f",label, deviationChecker.getDeviationPercentage(label)));
            }

        }
        catch (Exception e)
        {
            System.out.println(String.format("Time taken for Distance-based approximate conformance checking %d milliseconds", System.currentTimeMillis() - start));
            e.printStackTrace();

        }
    }

    private static void testConformanceApproximation()
    {
        //This method is used to test the approach by Fani Sani
        XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
        XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
        XesXmlParser parser = new XesXmlParser();
        XLog inputLog;

        try {
            InputStream is = new FileInputStream("C:\\Work\\DSG\\Data\\BPI2015Reduced2014.xml");
            inputLog = parser.parse(is).get(0);
            Pnml pnml = importPnmlFromStream(new FileInputStream("C:\\Work\\DSG\\Data\\IM_Petrinet.pnml"));
            Petrinet pn = PetrinetFactory.newPetrinet(pnml.getLabel());
            Marking imk=new Marking();
            Collection<Marking> fmks = new HashSet<>();
            GraphLayoutConnection glc = new GraphLayoutConnection(pn);
            pnml.convertToNet(pn,imk, fmks,glc);
            MatrixFilterParameter parameter = new MatrixFilterParameter(10, inputLog.getClassifiers().get(0), SimilarityMeasure.Levenstein, SamplingReturnType.Traces, PrototypeType.KMeansClusteringApprox);
            //now the target
            String result = ProtoTypeSelectionAlgo.apply(inputLog,pn,parameter,null);

            System.out.println(result);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    private static void testJNI()
    {
        try {
            // Create a problem with 4 variables and 0 constraints
            LpSolve solver = LpSolve.makeLp(0, 4);

            // add constraints
            solver.strAddConstraint("3 2 2 1", LpSolve.LE, 4);
            solver.strAddConstraint("0 4 3 1", LpSolve.GE, 3);

            // set objective function
            solver.strSetObjFn("2 3 -2 3");

            // solve the problem
            solver.solve();

            // print solution
            System.out.println("Value of objective function: " + solver.getObjective());
            double[] var = solver.getPtrVariables();
            for (int i = 0; i < var.length; i++) {
                System.out.println("Value of var[" + i + "] = " + var[i]);
            }

            // delete the problem and free memory
            solver.deleteLp();
        }
        catch (LpSolveException e) {
            e.printStackTrace();
        }
    }


    private static void testBed3()
    {
        String model = "AEFwBCDCJIKLOMlmGonpqtMrZuvN\u0081OPQRSTUV\\[`WabHXgYcdfeheji";
        String trace = "FGHBCDEAJICKLMlmOonptqusrvNOPQRSTUVWabMhg[i^Y\\_]c`eXZjdfe";

        List<String> modelTrace = new ArrayList<>(model.length());


        for (char c : model.toCharArray())
        {
            modelTrace.add(String.valueOf(c));
        }

        List<String> traceTrace = new ArrayList<>(model.length());


        for (char c : trace.toCharArray())
        {
            traceTrace.add(String.valueOf(c));
        }

        Trie t = new Trie(100);
        t.addTrace(modelTrace);
        long start = System.currentTimeMillis();
        ConformanceChecker cnfChecker = new RandomConformanceChecker(t,1,1,100000);
        Alignment alg = cnfChecker.check(traceTrace);
        long total = System.currentTimeMillis() - start;
        System.out.println(alg.toString());
        System.out.println(String.format("Total time %d", total));


    }
}
