package nl.uva.sne;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


import net.didion.jwnl.JWNLException;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.edge.DirectedWeightedEdge;
import org._3pq.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.semanticweb.skos.AddAssertion;
import org.semanticweb.skos.SKOSChange;
import org.semanticweb.skos.SKOSChangeException;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSConceptScheme;
import org.semanticweb.skos.SKOSCreationException;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSEntityAssertion;
import org.semanticweb.skos.SKOSStorageException;
import org.semanticweb.skosapibinding.SKOSFormatExt;

//import weka.clusterers.HierarchicalClusterer;
//import weka.core.Attribute;
//import weka.core.EuclideanDistance;
//import weka.core.FastVector;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.gui.hierarchyvisualizer.HierarchyVisualizer;
public class App {

    private static Map<String, Integer> keywordsDictionaray;
    private static int maxNGrams = 2;
//    private static UberLanguageDetector inst;
    private static Map<String, List<String>> nGramsMap;
//    private static int numOfWords = 500;
    private static File graphFile = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator
            + "etc" + File.separator + "graphFile1.dot");
    private static File graphFile2 = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator
            + "etc" + File.separator + "graphFile2.dot");
    private static File skosFile = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "etc" + File.separator + "taxonomy.rdf");
    private static boolean generateNgrams = true;
    private static int depth = 3;
    private static BabelNet bbn;

    public static void main(String[] args) {
        try {

            String jsonDocsPath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "jsondocs";
            String textDocsPath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "textdocs";
            String indexPath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "index";
            String keywordsDictionarayFile = System.getProperty("user.home") + File.separator
                    + "Downloads" + File.separator + "textdocs" + File.separator + "dictionary.csv";
            File taxonomyFile = new File(System.getProperty("user.home")
                    + File.separator + "workspace" + File.separator + "TEXT"
                    + File.separator + "etc" + File.separator + "ACMComputingClassificationSystemSKOSTaxonomy.rdf");



            boolean json2text = false, createIndex = false, creatDict = false, buildTree = false;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    //-json2text $HOME/Downloads/jsondocs/ $HOME/Downloads/textdocs/ 
                    if (args[i].equals("-json2text")) {
                        json2text = true;
                        File in = new File(args[i + 1]);
                        if (in.exists()) {
                            jsonDocsPath = in.getAbsolutePath();
                        } else {
                            throw new Exception(in.getAbsolutePath() + " not found");
                        }
                        File out = new File(args[i + 2]);
                        if (out.isDirectory()) {
                            textDocsPath = out.getAbsolutePath();
                        } else {
                            throw new Exception(out.getAbsolutePath() + " not a directory");
                        }

                    }
                    //-i $HOME/Downloads/textdocs/ $HOME/Downloads/index
                    if (args[i].equals("-i")) {
                        createIndex = true;
                        File in = new File(args[i + 1]);
                        if (in.exists() && in.isDirectory()) {
                            textDocsPath = in.getAbsolutePath();
                        } else {
                            throw new Exception(in.getAbsolutePath() + " not found");
                        }
                        indexPath = new File(args[i + 2]).getAbsolutePath();

                    }
                    //-d $HOME/Downloads/textdocs/ $HOME/Downloads/textdocs/dictionary.csv
                    if (args[i].equals("-d")) {
                        creatDict = true;
                        File in = new File(args[i + 1]);
                        if (in.exists() && in.isDirectory()) {
                            textDocsPath = in.getAbsolutePath();
                        } else {
                            throw new Exception(in.getAbsolutePath() + " not found");
                        }
                        keywordsDictionarayFile = new File(args[i + 2]).getAbsolutePath();
                        if (!FilenameUtils.getExtension(keywordsDictionarayFile).equalsIgnoreCase("cvs")) {
                            keywordsDictionarayFile += ".csv";
                        }
                    }
                    if (args[i].equals("-t")) {
                        buildTree = true;
                        File in = new File(args[i + 1]);
                        if (in.exists()) {
                            if (FilenameUtils.getExtension(in.getName()).endsWith("rdf") || FilenameUtils.getExtension(in.getName()).endsWith("xml")) {
                                taxonomyFile = in;
                                keywordsDictionarayFile = null;
                            } else {
                                keywordsDictionarayFile = in.getAbsolutePath();
                                taxonomyFile = null;
                            }
                            indexPath = new File(args[i + 2]).getAbsolutePath();
                        } else {
                            throw new Exception(in.getAbsolutePath() + " not found");
                        }

                    }
                }
            }
            bbn = new BabelNet();
            if (json2text) {
                jobDescription2TextFile(jsonDocsPath, textDocsPath);
            }
            if (createIndex) {
                createIndex(textDocsPath, indexPath);
            }
            if (creatDict) {
                createTermDictionary(textDocsPath, keywordsDictionarayFile, true);
            }
            if (buildTree && keywordsDictionarayFile != null) {
                buildHyperymTree(keywordsDictionarayFile, indexPath);
            } else if (buildTree && taxonomyFile != null) {
                List<TermVertex> leaves = getTermsFromTaxonomy(taxonomyFile, "en", 2);
                buildHyperymTree(leaves, indexPath, keywordsDictionarayFile);
            }




//            DefaultDirectedWeightedGraph g = taxonomy2Graph(taxonomyFile, "en");

        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    private static void hierarchicalClusteringExample() throws Exception {
//        HierarchicalClusterer clusterer = new HierarchicalClusterer();
//        clusterer.setOptions(new String[]{"-L", "COMPLETE"});
//        clusterer.setDebug(true);
//        clusterer.setNumClusters(2);
//        clusterer.setDistanceFunction(new EuclideanDistance());
//        clusterer.setDistanceIsBranchLength(true);
//
//        // Declare the feature vector
//        FastVector fv = new FastVector();
//        fv.addElement(new Attribute("A"));
//        fv.addElement(new Attribute("B"));
//        fv.addElement(new Attribute("C"));
//
//
//        Instances data = new Instances("Weka test", fv, fv.size());
//
//
//        // Add data
//        data.add(new Instance(1.0, new double[]{1.0, 0.0, 1.0})); //vector1
//        data.add(new Instance(1.0, new double[]{0.5, 0.0, 1.0}));
//        data.add(new Instance(1.0, new double[]{0.0, 1.0, 0.0}));
//        data.add(new Instance(1.0, new double[]{0.0, 1.0, 0.3}));//vector4
//
//        // Cluster network
//        clusterer.buildClusterer(data);
//
//        // Print normal
//        clusterer.setPrintNewick(false);
//        System.out.println(clusterer.graph());
//        // Print Newick
//        clusterer.setPrintNewick(true);
//        System.out.println(clusterer.graph());
//
//        // Let's try to show this clustered data!
//        JFrame mainFrame = new JFrame("Weka Test");
//        mainFrame.setSize(600, 400);
//        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        Container content = mainFrame.getContentPane();
//        content.setLayout(new GridLayout(1, 1));
//
//        HierarchyVisualizer visualizer = new HierarchyVisualizer(clusterer.graph());
//        content.add(visualizer);
//
//        mainFrame.setVisible(true);
//    }
    private static String getRawText(String path) throws FileNotFoundException, IOException, ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(path));

        JSONObject jsonObject = (JSONObject) obj;
        JSONObject jp = (JSONObject) jsonObject.get("jobPosting");
        JSONObject desc = (JSONObject) jp.get("description");
        if (desc != null) {
            return (String) desc.get("rawText");
        } else {
            return (String) jsonObject.get("description");
        }
    }

    private static void createTermDictionary(String inputJsonDocsPath, String outkeywordsDictionarayFile, boolean tokenize) throws FileNotFoundException, IOException, ParseException, JWNLException, MalformedURLException, Exception {
        File dir = new File(inputJsonDocsPath);
        if (keywordsDictionaray == null) {
            keywordsDictionaray = new HashMap();
        }
        int count = 0;
        for (File f : dir.listFiles()) {
            if (FilenameUtils.getExtension(f.getName()).endsWith("txt")) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    for (String text; (text = br.readLine()) != null;) {
                        count++;
                        String lang = Utils.detectLang(text);
                        if (lang.toLowerCase().equals("en")) {
                            if (tokenize) {
                                Logger.getLogger(App.class.getName()).log(Level.INFO, "Tokenizing: {0} {1} / {2}", new Object[]{f.getAbsolutePath(), count, dir.list().length});
                                List<String> tokens = tokenize(text, generateNgrams);

                                for (String t : tokens) {
//                                POS[] pos = BabelNet.getPOS(t);
//                                if (pos.length == 1 && pos[0].equals(POS.NOUN)) {
                                    Integer tf;
                                    if (keywordsDictionaray.containsKey(t)) {
                                        tf = keywordsDictionaray.get(t);
                                        tf++;
                                    } else {
                                        tf = 1;
                                    }
                                    keywordsDictionaray.put(t, tf);
//                                }
                                }
                            } else {
                                //                                POS[] pos = BabelNet.getPOS(t);
//                                if (pos.length == 1 && pos[0].equals(POS.NOUN)) {
                                Integer tf;
                                if (keywordsDictionaray.containsKey(text.toLowerCase())) {
                                    tf = keywordsDictionaray.get(text.toLowerCase());
                                    tf++;
                                } else {
                                    tf = 1;
                                }
                                keywordsDictionaray.put(text.toLowerCase(), tf);
                            }
                        }
                    }
                }
            }
        }

        ValueComparator bvc = new ValueComparator(keywordsDictionaray);
        Map<String, Integer> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(keywordsDictionaray);


        //remove terms that only apear with others. e.g. if we only 
        //have 'machine learning' there is no point to keep 'machine' or 'learning'
        Logger.getLogger(App.class.getName()).log(Level.INFO, "Filtering out terms");
        List<String> toRemove = new ArrayList<>();
        Integer singleTermRank = 0;
        for (String key1 : sorted_map.keySet()) {
            singleTermRank++;
            Integer multiTermRank = 0;
            for (String key2 : sorted_map.keySet()) {
                multiTermRank++;
                if (!key1.contains("_") && key2.contains("_") && key2.split("_")[0].equals(key1)) {
                    int diff = multiTermRank - singleTermRank;
//                    System.err.println(key1 + ":" + singleTermRank + " " + key2 + ":" + multiTermRank + " diff: " + diff);
                    if (diff <= 5 && diff > 0) {
//                        System.err.println(key1 + ":" + singleTermRank + " " + key2 + ":" + multiTermRank + " diff: " + diff);
                        if (!toRemove.contains(key1)) {
                            Logger.getLogger(App.class.getName()).log(Level.INFO, "Will remove: {0}", key1);
                            toRemove.add(key1);
                        }
                    }
                    break;
                }
            }
        }
        for (String k : toRemove) {
//            System.err.println("removing: " + k);
            keywordsDictionaray.remove(k);
        }
        bvc = new ValueComparator(keywordsDictionaray);
        sorted_map = new TreeMap(bvc);
        sorted_map.putAll(keywordsDictionaray);

        Logger.getLogger(App.class.getName()).log(Level.INFO, "Writing : {0}", outkeywordsDictionarayFile);

        try (PrintWriter out = new PrintWriter(outkeywordsDictionarayFile)) {
            for (String key : sorted_map.keySet()) {
                out.print(key + "," + keywordsDictionaray.get(key) + "\n");
            }
        }
    }

    private static List<String> tokenize(String text, boolean generateNgrams) throws IOException, JWNLException, FileNotFoundException, MalformedURLException, ParseException, Exception {
        if (bbn == null) {
            bbn = new BabelNet();
        }
//        text = text.replaceAll("((mailto\\:|(news|(ht|f)tp(s?))\\://){1}\\S+)", "");
//        text = text.replaceAll("[^a-zA-Z\\s]", "");
//        text = text.replaceAll("(\\d+,\\d+)|\\d+", "");
//        text = text.replaceAll("-", "");
        text = text.replaceAll("  ", " ");
        text = text.toLowerCase();

        ArrayList<String> words = new ArrayList<>();
        Analyzer analyzer = new ArmenianAnalyzer(Version.LUCENE_42, Utils.getCharArrayStopwords());
        StringBuilder sb = new StringBuilder();
        try (TokenStream tokenStream = analyzer.tokenStream("field", new StringReader(text))) {
            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String lemma;
                try {
                    lemma = bbn.lemmatize(term.toString(), "EN");
                } catch (Exception ex) {
                    lemma = term.toString();
                }
                if (!Utils.isStopWord(text)) {
                    words.add(lemma);
                    sb.append(lemma).append(" ");
                }
            }
            tokenStream.end();
        }
        if (generateNgrams) {
            StandardTokenizer source = new StandardTokenizer(Version.LUCENE_42, new StringReader(sb.toString()));
            TokenStream tokenStream = new StandardFilter(Version.LUCENE_42, source);
            try (ShingleFilter sf = new ShingleFilter(tokenStream, 2, maxNGrams)) {
                sf.setOutputUnigrams(false);
                CharTermAttribute charTermAttribute = sf.addAttribute(CharTermAttribute.class);
                sf.reset();
                while (sf.incrementToken()) {
                    String word = charTermAttribute.toString();
                    words.add(word.replaceAll(" ", "_"));
                }
                sf.end();
            }
        }
        return words;
    }

    public static double tf(String doc, String term) throws FileNotFoundException, IOException {
        double result = 0;
        double numOfLines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(doc))) {
            String word;
            while ((word = br.readLine()) != null) {
                numOfLines++;
                if (term.equalsIgnoreCase(word)) {
                    result++;
                }
            }
        }
        return result / numOfLines;
    }

    public static double idf(File docsFolder, String term) throws FileNotFoundException, IOException {
        double n = 0;
        File[] docs = docsFolder.listFiles();
        for (File doc : docs) {
            try (BufferedReader br = new BufferedReader(new FileReader(doc))) {
                String word;
                while ((word = br.readLine()) != null) {
                    if (term.equalsIgnoreCase(word)) {
                        n++;
                        break;
                    }
                }
            }
        }
        return Math.log(docs.length / n);
    }

    public static double tfIdf(String doc, File docsFolder, String term) throws FileNotFoundException, IOException {
        return tf(doc, term) * idf(docsFolder, term);
    }

    public static double tfIdf(List<String> doc, List<List<String>> docs, String term) {
        return tf(doc, term) * idf(docs, term);
    }

    public static double idf(List<List<String>> docs, String term) {
        double n = 0;
        for (List<String> doc : docs) {
            for (String word : doc) {
                if (term.equalsIgnoreCase(word)) {
                    n++;
                    break;
                }
            }
        }
        if (n <= 0) {
            n = 1;
        }
        return Math.log(docs.size() / n);
    }

    public static double tf(List<String> doc, String term) {
        double result = 0;
        for (String word : doc) {
            if (term.equalsIgnoreCase(word)) {
                result++;
            }
        }
        return result / doc.size();
    }

    private static void buildHyperymTree(String termDictionaryPath, String indexPath) throws FileNotFoundException, IOException, JWNLException, ParseException, ClassCastException, ClassNotFoundException, MalformedURLException, Exception {
        if (bbn == null) {
            bbn = new BabelNet();
        }

        List<TermVertex> allTerms = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(termDictionaryPath))) {
            String line;
            int limit = 9999;
            int count = 0;
            while ((line = br.readLine()) != null) {
                ++count;
                if (count >= limit) {
                    break;
                }
                String trem = line.split(",")[0];
                String lemma = bbn.lemmatize(trem, "EN");

                if (Integer.valueOf(line.split(",")[1]) > 2) {
                    List<TermVertex> terms = getTermVertices(lemma, null, depth, true, bbn, indexPath, termDictionaryPath, null);
                    if (terms != null && !terms.isEmpty()) {
                        allTerms.addAll(terms);
                    }
                }
            }

        } finally {
            bbn.saveCache();
            DefaultDirectedWeightedGraph g = buildGraph(allTerms);
            export2DOT(g, graphFile);
            DefaultDirectedWeightedGraph pg = pruneGraph(g, 3);
            export2DOT(pg, graphFile2);
//            rapper -o dot ~/workspace/TEXT/etc/taxonomy.rdf | dot -Kfdp -Tsvg -o taxonomy.svg
            export2SKOS(pg, skosFile);
        }
    }

    private static void buildHyperymTree(List<TermVertex> leaves, String indexPath, String termDictionaryPath) throws FileNotFoundException, IOException, JWNLException, MalformedURLException, ParseException, Exception {
        if (bbn == null) {
            bbn = new BabelNet();
        }
        List<TermVertex> allTerms = new ArrayList<>();
        try {
            int limit = 2;
            int count = 0;
            for (TermVertex tv : leaves) {
                ++count;
                if (count >= limit) {
                    break;
                }

                String lemma = bbn.lemmatize(tv.getLemma(), "EN");

                List<TermVertex> terms = getTermVertices(URLEncoder.encode(lemma, "UTF-8"), null, depth, true, bbn, indexPath, termDictionaryPath, null);
                if (terms != null && !terms.isEmpty()) {
                    allTerms.addAll(terms);
                } else {
                    List<String> alt = tv.getAlternativeLables();
                    if (alt != null) {
                        for (String a : alt) {
                            lemma = bbn.lemmatize(a, "EN");
                            terms = getTermVertices(URLEncoder.encode(lemma, "UTF-8"), null, depth, true, bbn, indexPath, termDictionaryPath, null);
                            if (terms != null && !terms.isEmpty()) {
                                allTerms.addAll(terms);
                                break;
                            }
                        }
                    }
                }

            }

        } finally {
            bbn.saveCache();
            DefaultDirectedWeightedGraph g = buildGraph(allTerms);
            export2DOT(g, graphFile);
            DefaultDirectedWeightedGraph pg = pruneGraph(g, 4);
            export2DOT(pg, graphFile2);
//            rapper -o dot ~/workspace/TEXT/etc/taxonomy.rdf | dot -Kfdp -Tsvg -o taxonomy.svg
            export2SKOS(pg, skosFile);
        }
    }

    private static void jobDescription2TextFile(String inputJsonDocsPath, String outputTextDocsPath) throws FileNotFoundException, IOException, ParseException {
        File dir = new File(inputJsonDocsPath);
        Logger.getLogger(App.class.getName()).log(Level.INFO, "Reading: {0}", dir.getAbsolutePath());
        for (File f : dir.listFiles()) {
            if (FilenameUtils.getExtension(f.getName()).endsWith("json")) {
                String text = getRawText(f.getAbsolutePath());
                String lang = Utils.detectLang(text);
                if (lang.toLowerCase().equals("en")) {
                    String fileNameWithOutExt = FilenameUtils.removeExtension(f.getName());
                    try (PrintWriter out = new PrintWriter(outputTextDocsPath + File.separator + fileNameWithOutExt + ".txt")) {
                        out.print(text.replaceAll(" &amp; ", " and "));
                    }
                }
            }
        }
        Logger.getLogger(App.class.getName()).log(Level.INFO, "Text files in: {0}", outputTextDocsPath);
    }

    private static void createIndex(String textDocsPath, String indexDir) throws IOException {
        Logger.getLogger(App.class.getName()).log(Level.INFO, "Indexing *.txt files in : {0}", textDocsPath);

        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_42, analyzer);
        Directory iDir = FSDirectory.open(new File(indexDir));
        try (IndexWriter indexWriter = new IndexWriter(iDir, conf)) {
            File dir = new File(textDocsPath);
            File[] files = dir.listFiles();
            for (File file : files) {
                if (FilenameUtils.getExtension(file.getName()).endsWith("txt")) {

                    Document document = new Document();
                    String path = file.getCanonicalPath();

                    FileReader fr = new FileReader(path);
                    document.add(new TextField("content", fr));

                    document.add(new StringField("path", path, Field.Store.YES));
                    indexWriter.addDocument(document);
                }

            }
        }
        Logger.getLogger(App.class.getName()).log(Level.INFO, "Index in : {0}", indexDir);
    }

//    private static String getScentsens(String searchString, int numOfWords, String INDEX_DIRECTORY) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
//        if (searchString.contains("_")) {
//            searchString = searchString.replaceAll("_", " ");
//        }
//        Directory directory = FSDirectory.open(new File(INDEX_DIRECTORY));
//        IndexReader indexReader = DirectoryReader.open(directory);
//        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//        IndexReader reader = indexSearcher.getIndexReader();
//
//        Query q = buildQuery(searchString, false);
//        ScoreDoc[] hits = getDocs(q, indexSearcher, 10);
//        if (hits.length < 1) {
//            q = buildQuery(searchString, true);
//            hits = getDocs(q, indexSearcher, 10);
//        }
//
//
////        System.err.println("Found " + hits.length + " hits.");
//        StringBuilder scentence = new StringBuilder();
//        StringBuilder candidateScentence = new StringBuilder();
//        int count = 0;
//        for (int i = 0; i < hits.length; ++i) {
//            int docId = hits[i].doc;
//
//            String path = reader.document(docId).getField("path").stringValue();
////            System.err.println("path: " + path + "score: " + hits[i].score);
//
//            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
//                for (String line; (line = br.readLine()) != null;) {
//                    line = line.replaceAll(" &amp; ", " and ");
////                    String regex = "^.*" + searchString + ".*$";
////                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
////
////                    Matcher matcher = pattern.matcher(line);
////                    matrchi = matcher.group();
//
//                    String[] parts = line.split(" ");
//                    for (int j = 0; j < parts.length; j++) {
//                        candidateScentence.append(parts[j]).append(" ");
//
//                        if (parts[j].endsWith(".") || parts[j].endsWith("?") || parts[j].endsWith("!") || parts[j].endsWith(";") || j >= parts.length) {
//                            if (candidateScentence.toString().toLowerCase().contains(searchString)) {
//
//                                scentence.append(candidateScentence.toString()).append(" ");
//                                count += scentence.toString().split(" ").length;
//                                if (count >= numOfWords) {
//                                    return scentence.toString().replaceAll("  ", " ");
//                                }
//                            }
//                            candidateScentence.setLength(0);
//                        }
//                    }
//                }
//            }
//        }
//        return scentence.toString().replaceAll("  ", " ");
//    }
////
//    private static List<String> getDocuments(String searchString, int numOfWords, int maxDoxs, String INDEX_DIRECTORY) throws IOException, org.apache.lucene.queryparser.classic.ParseException {
//        if (searchString.contains("_")) {
//            searchString = searchString.replaceAll("_", " ");
//        }
//        Directory directory = FSDirectory.open(new File(INDEX_DIRECTORY));
//        IndexReader indexReader = DirectoryReader.open(directory);
//        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//        IndexReader reader = indexSearcher.getIndexReader();
//
//        Query q = buildQuery(searchString, false);
//        ScoreDoc[] hits = getDocs(q, indexSearcher, maxDoxs);
//        if (hits.length < 1) {
//            q = buildQuery(searchString, true);
//            hits = getDocs(q, indexSearcher, maxDoxs);
//        }
//        List<String> docs = new ArrayList<>(hits.length);
//
////        System.err.println("Found " + hits.length + " hits.");
//
//        StringBuilder candidateScentence = new StringBuilder();
//        int count = 0;
//        for (int i = 0; i < hits.length; ++i) {
//            int docId = hits[i].doc;
//
//            String path = reader.document(docId).getField("path").stringValue();
//            StringBuilder scentence = new StringBuilder();
//            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
//                for (String line; (line = br.readLine()) != null;) {
//                    line = line.replaceAll(" &amp; ", " and ");
//                    String[] parts = line.split(" ");
//                    for (int j = 0; j < parts.length; j++) {
//                        candidateScentence.append(parts[j]).append(" ");
//                        if (parts[j].endsWith(".") || parts[j].endsWith("?") || parts[j].endsWith("!") || parts[j].endsWith(";") || j >= parts.length) {
//                            if (candidateScentence.toString().toLowerCase().contains(searchString)) {
//                                scentence.append(candidateScentence.toString()).append(" ");
//                                count += scentence.toString().split(" ").length;
//                                if (count >= numOfWords) {
//                                    docs.add(scentence.toString().replaceAll("  ", " "));
//                                    break;
//                                }
//                            }
//                            candidateScentence.setLength(0);
//                        }
//                    }
//                }
//            }
//        }
//        return docs;
//
//    }
    private static List<String> getNGrams(String lemma, String keywordsDictionarayFile) throws FileNotFoundException, IOException {
        if (nGramsMap == null) {
            nGramsMap = new HashMap<>();
        }
        List<String> nGrams = nGramsMap.get(lemma);
        if (nGrams != null) {
            return nGrams;
        }
        nGrams = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(keywordsDictionarayFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String keyword = line.split(",")[0];
                if (keyword.contains(lemma) && keyword.contains("_")) {
                    nGrams.add(keyword);
                }
            }
        }
        nGramsMap.put(lemma, nGrams);
        return nGrams;
    }

    private static List<TermVertex> getTermVertices(String lemma, String id, int depth, boolean isFromDiec, BabelNet bbn, String indexPath, String termDictionaryPath, List<TermVertex> terms) throws IOException, MalformedURLException, ParseException, Exception {
        if (terms == null) {
            terms = new ArrayList<>();
        }
        TermVertex termVertex = null;
        List<TermVertex> possibleTerms = null;
        if (isFromDiec) {
            possibleTerms = bbn.getTermNodeByLemma(lemma, isFromDiec);
        } else {
            termVertex = bbn.getTermNodeByID(lemma, id, isFromDiec);
        }
        if (possibleTerms != null && termVertex == null) {
            List<String> ngarms = getNGrams(lemma, termDictionaryPath);
            possibleTerms = resolveTerms(possibleTerms, lemma, ngarms);
            if (possibleTerms == null || possibleTerms.isEmpty()) {
//                String scentense = getScentsens(lemma, numOfWords, indexPath);
//                ngarms.add(scentense);
                possibleTerms = bbn.disambiguate("EN", lemma, ngarms);
            }

        }
        if (possibleTerms == null) {
            possibleTerms = new ArrayList<>();
        }
        if (termVertex != null) {
            possibleTerms.add(termVertex);
        }

        for (TermVertex tv : possibleTerms) {
            tv.setIsFromDictionary(isFromDiec);
            terms.add(tv);
            if (depth > 1) {
                List<TermVertex> hyper = tv.getBroader();
                if (hyper != null) {
                    for (TermVertex h : hyper) {
                        if (h != null) {
//                            System.err.println("lemma: " + h.getLemma() + " id: " + h.getUID());
                            getTermVertices(h.getLemma(), h.getUID(), --depth, false, bbn, indexPath, termDictionaryPath, terms);
                        }
                    }
                }
            }
        }

        return terms;
    }

    private static DefaultDirectedWeightedGraph buildGraph(List<TermVertex> terms) {
        DefaultDirectedWeightedGraph g = new DefaultDirectedWeightedGraph();
        for (TermVertex tv : terms) {
            if (!g.containsVertex(tv)) {
                g.addVertex(tv);
            }
            List<TermVertex> hyper = tv.getBroader();
            if (hyper != null) {
                for (TermVertex h : tv.getBroader()) {
                    if (!g.containsVertex(h)) {
                        g.addVertex(h);
                    }
                    if (!g.containsEdge(h, tv) && !h.getLemma().equals(tv.getLemma())) {
                        g.addEdge(h, tv);
                    }
                }
            }
        }
        return g;
    }

    public static void export2DOT(DefaultDirectedWeightedGraph g, File graphFile) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(graphFile, false))) {
            Set<Edge> set = g.edgeSet();
            bw.write("digraph G {");
            bw.newLine();
            for (Edge e : set) {
                nl.uva.sne.TermVertex sVertex = (nl.uva.sne.TermVertex) e.getSource();
                String s = sVertex.getLemma().replaceAll("-", "_");
                s = s.replaceAll("[()]", "");
                nl.uva.sne.TermVertex tVertex = (nl.uva.sne.TermVertex) e.getTarget();
                String t = tVertex.getLemma().replaceAll("-", "_");
                t = t.replaceAll("[()]", "");
//                System.err.println("\"" + s + "\" -- \"" + t + "\"");
//                System.err.println("outDegreeOf: " + t + " = " + g.outDegreeOf(sVertex));
//                System.err.println("outDegreeOf: " + s + " = " + g.outDegreeOf(sVertex));
//                System.err.println("inDegreeOf: " + t + " = " + g.inDegreeOf(tVertex));
//                System.err.println("inDegreeOf: " + s + " = " + g.inDegreeOf(tVertex));
//                System.err.println("incomingEdgesOf: " + s + " = " + g.incomingEdgesOf(tVertex).size());                


                bw.write("\"" + s + "\" -> \"" + t + "\"");
                bw.newLine();
                if (sVertex.getIsFromDictionary()) {
                    bw.write("\"" + s + "\"" + " [shape=rectangle]");
                    bw.newLine();
                }
                if (tVertex.getIsFromDictionary()) {
                    bw.write("\"" + t + "\"" + " [shape=rectangle]");
                    bw.newLine();
                }
            }
            Set<TermVertex> vSet = g.vertexSet();
            if (set.isEmpty()) {
                for (TermVertex tv : vSet) {
                    String v = tv.getLemma().replaceAll("-", "_");
                    bw.write("\"" + v + "\"");
                    bw.newLine();
                    if (tv.getIsFromDictionary()) {
                        bw.write(v + " [shape=rectangle]");
                        bw.newLine();
                    }
                }
            }
            for (Edge e : set) {
                nl.uva.sne.TermVertex sVertex = (nl.uva.sne.TermVertex) e.getSource();
                nl.uva.sne.TermVertex tVertex = (nl.uva.sne.TermVertex) e.getTarget();
                for (TermVertex tv : vSet) {
                    if (!tv.getLemma().equals(sVertex.getLemma()) && !tv.getLemma().equals(tVertex.getLemma())) {
                        String v = tv.getLemma().replaceAll("-", "_");
                        v = v.replaceAll("[()]", "");
                        bw.write("\"" + v + "\"");
                        bw.newLine();
                        if (tv.getIsFromDictionary()) {
                            bw.write("\"" + v + "\"" + " [shape=rectangle]");
                            bw.newLine();
                        }
                    }
                }
            }
            bw.write("}");
            bw.newLine();
            bw.flush();
        }
    }

//    private static Query buildQuery(String searchString, boolean useWildcard) throws org.apache.lucene.queryparser.classic.ParseException {
//        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
//        QueryParser qp = new QueryParser(Version.LUCENE_42, "content", analyzer);
//        qp.setAllowLeadingWildcard(true);
//        if (useWildcard) {
//            return qp.parse("*" + QueryParser.escape(searchString) + "*");
//        } else {
//            return qp.parse(QueryParser.escape(searchString));
//        }
//    }
    private static ScoreDoc[] getDocs(Query q, IndexSearcher indexSearcher, int maxDoxs) throws IOException {
        TopScoreDocCollector collector = TopScoreDocCollector.create(maxDoxs, true);
        indexSearcher.search(q, collector);
        return collector.topDocs().scoreDocs;
    }

    private static DefaultDirectedWeightedGraph pruneGraph(DefaultDirectedWeightedGraph g, int depth) {
        Set<TermVertex> vs = g.vertexSet();
        List<TermVertex> toRemove = new ArrayList<>();
        for (TermVertex tv : vs) {
            if (!tv.getIsFromDictionary()) {
                List<DirectedWeightedEdge> outEdges = g.outgoingEdgesOf(tv);
                List<DirectedWeightedEdge> inEdges = g.incomingEdgesOf(tv);
                if (outEdges.size() <= 0 && inEdges.size() <= 0) {
                    toRemove.add(tv);
                }
                if (outEdges.size() == 1) {
                    DirectedWeightedEdge out = outEdges.get(0);
                    TermVertex target = (TermVertex) out.getTarget();
//                    TermVertex source = (TermVertex) out.getSource();
                    if (!target.getIsFromDictionary()) {
                        toRemove.add(tv);
                    }
                }
            }
        }
        g.removeAllVertices(toRemove);
        depth--;
        if (depth >= 1) {
            pruneGraph(g, depth);
        }
        return g;
    }

    private static void export2SKOS(DefaultDirectedWeightedGraph g, File skosFile) throws ParseException, SKOSCreationException, SKOSChangeException, SKOSStorageException {

        SKOSConceptScheme scheme = SkosUtils.getSKOSDataFactory().getSKOSConceptScheme(URI.create(SkosUtils.SKOS_URI + "DS-BoK"));

        List<SKOSChange> change = new ArrayList<>();
        SKOSEntityAssertion schemaAss = SkosUtils.getSKOSDataFactory().getSKOSEntityAssertion(scheme);
        change.add(new AddAssertion(SkosUtils.getSKOSDataset(), schemaAss));
        Set<DirectedWeightedEdge> edges = g.edgeSet();

        for (DirectedWeightedEdge e : edges) {
            change.addAll(SkosUtils.create(e, "EN"));
        }
        SkosUtils.getSKOSManager().applyChanges(change);
        SkosUtils.getSKOSManager().save(SkosUtils.getSKOSDataset(), SKOSFormatExt.RDFXML, skosFile.toURI());
    }

    private static DefaultDirectedWeightedGraph taxonomy2Graph(File taxonomyFile, String language) throws SKOSCreationException {
        SKOSDataset dataset = SkosUtils.getSKOSManager().loadDatasetFromPhysicalURI(taxonomyFile.toURI());
        DefaultDirectedWeightedGraph g = new DefaultDirectedWeightedGraph();
        Map<String, TermVertex> idMap = new HashMap<>();



        for (SKOSConcept concept : dataset.getSKOSConcepts()) {

            String value = SkosUtils.getPrefLabelValue(dataset, concept, language);
            TermVertex term = new TermVertex(value);
            String uid = SkosUtils.getUID(concept, taxonomyFile);
            term.setUID(uid);
            List<String> altLables = SkosUtils.getAltLabelValues(dataset, concept, language);
            term.setAlternativeLables(altLables);
            List<String> buids = SkosUtils.getBroaderUIDs(dataset, concept);
            term.setBroaderUIDS(buids);
            List<String> nuids = SkosUtils.getNarrowerUIDs(dataset, concept);
            term.setNarrowerUIDS(nuids);
            idMap.put(uid, term);
        }
        Collection<TermVertex> vs = idMap.values();
        for (TermVertex tv : vs) {
            if (!g.containsVertex(tv)) {
                g.addVertex(tv);
            }
            List<String> hyper = tv.getBroaderUIDS();
            if (hyper != null) {
                for (String hid : hyper) {
                    TermVertex h = idMap.get(hid);
                    if (!g.containsVertex(h)) {
                        g.addVertex(h);
                    }
                    if (!g.containsEdge(h, tv) && !h.getLemma().equals(tv.getLemma())) {
                        g.addEdge(h, tv);
                    }
                }
            }

        }
        return g;
    }

    private static List<TermVertex> getTermsFromTaxonomy(File taxonomyFile, String language, int levels) throws SKOSCreationException {
        SKOSDataset dataset = SkosUtils.getSKOSManager().loadDatasetFromPhysicalURI(taxonomyFile.toURI());
        List<TermVertex> leaves = new ArrayList<>();
        for (SKOSConcept concept : dataset.getSKOSConcepts()) {
            List<String> nuids = SkosUtils.getNarrowerUIDs(dataset, concept);
//            if (nuids == null || nuids.isEmpty()) {
            String value = SkosUtils.getPrefLabelValue(dataset, concept, language).toLowerCase();
            TermVertex term = new TermVertex(value);
//                String uid = SkosUtils.getUID(concept, taxonomyFile);
//                term.setUID(uid);
            List<String> altLables = SkosUtils.getAltLabelValues(dataset, concept, language);
            term.setAlternativeLables(altLables);
//                List<String> buids = SkosUtils.getBroaderUIDs(dataset, concept);
//                term.setBroaderUIDS(buids);
//                term.setNarrowerUIDS(nuids);
            leaves.add(term);
//            }
        }
        return leaves;

    }

    private static List<TermVertex> resolveTerms(List<TermVertex> possibleTerms, String lemma, List<String> nGrams) throws IOException, JWNLException, FileNotFoundException, MalformedURLException, ParseException, Exception {

        List<List<String>> allDocs = new ArrayList<>();
        Map<String, List<String>> docs = new HashMap<>();
        for (TermVertex tv : possibleTerms) {
            Set<String> doc = new HashSet<>();
            List<String> g = tv.getGlosses();
            if (g != null) {
                for (String s : g) {
                    doc.addAll(tokenize(s, false));
                }
            }
            List<String> al = tv.getAlternativeLables();
            if (al != null) {
                for (String s : al) {
                    doc.addAll(tokenize(s, false));
                }
            }
            List<String> cat = tv.getCategories();
            if (cat != null) {
                for (String s : cat) {
                    doc.addAll(tokenize(s, false));
                }
            }
//            doc.addAll(doc);
            allDocs.add(new ArrayList<>(doc));
//            System.err.println(doc);
            docs.put(tv.getUID(), new ArrayList<>(doc));
        }


        Set<String> contextDoc = new HashSet<>();
        for (String s : nGrams) {
            String[] parts = s.split("_");
            for (String token : parts) {

                if (token.length() > 1) {
//                    contextDoc.addAll(tokenize(token, false, bbn));
                    contextDoc.add(token);
                }
            }
        }
//        System.err.println(contextDoc);
        docs.put("context", new ArrayList<>(contextDoc));
        //        List<String> contextDocs = getDocuments(lemma, 100, 10, indexPath);
        //        for (String s : contextDocs) {
        //            List<String> doc = tokenize(s, false, bbn);
        //            allDocs.add(doc);
        //            docs.put("context", doc);
        //        }
        Map<String, Map<String, Double>> featureVectors = new HashMap<>();
        for (String k : docs.keySet()) {
            List<String> doc = docs.get(k);
            Map<String, Double> featureVector = new TreeMap<>();
            for (String term : doc) {
                if (!featureVector.containsKey(term)) {
                    double score = tfIdf(doc, allDocs, term);
                    featureVector.put(term, score);
                }
            }
//            System.err.println(featureVector);
            featureVectors.put(k, featureVector);
        }

        double highScore = 0.03;
        String winner = null;
        Map<String, Double> contextVector = featureVectors.remove("context");
        for (String key : featureVectors.keySet()) {
            Double similarity = Utils.cosineSimilarity(contextVector, featureVectors.get(key));
            if (similarity > highScore) {
                highScore = similarity;
                winner = key;
            }
        }
        List<TermVertex> terms = new ArrayList<>();
        for (TermVertex t : possibleTerms) {
            if (t.getUID().equals(winner)) {

                System.err.println("Winner: " + winner + " score: " + highScore);
                terms.add(t);
            }
        }
        if (!terms.isEmpty()) {
            return terms;
        } else {
            return null;//return possibleTerms;
        }
    }
}
