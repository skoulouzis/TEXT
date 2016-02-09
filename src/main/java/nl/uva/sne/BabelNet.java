/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne;

import edu.stanford.nlp.util.Pair;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

/**
 *
 * @author S. Koulouzis
 */
public class BabelNet {

    private static Dictionary wordNetdictionary;
    private static String babelNetKey;
    private static Map<String, String> synsetCache;
    private static File synsetCacheFile = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "cache" + File.separator + "synsetCacheFile.csv");
    private static Map<String, List<String>> wordIDCache;
    private static File wordIDCacheFile = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "cache" + File.separator + "wordCacheFile.csv");
//    private final File keywordsDictionarayFile;
    static Set<String> nonLematizedWords = new HashSet();
    private static File nonLematizedWordsFile = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "etc" + File.separator + "nonLematizedWords");
    private static File disambiguateCacheFile = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "cache" + File.separator + "disambiguateCacheFile.csv");
    private static Map<String, String> disambiguateCache = new HashMap<>();
    private static HTreeMap<String, String> edgesCache;
    private static File edgesCacheFile = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "cache" + File.separator + "edgesCacheFile.csv");
    private static File cacheDBFile = new File(System.getProperty("user.home")
            + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "cache" + File.separator + "cacheDB");

    static {
        try {
            JWNL.initialize(new FileInputStream(System.getProperty("user.home")
                    + File.separator + "workspace" + File.separator + "TEXT"
                    + File.separator + "etc" + File.separator + "file_properties.xml"));
        } catch (JWNLException | FileNotFoundException ex) {
            Logger.getLogger(BabelNet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private HashMap<String, String> lemmaCache;

    private static String getKey() throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home")
                + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "etc" + File.separator + "babelnetKey")))) {
            babelNetKey = br.readLine();
            br.close();
        }
        return babelNetKey;
    }
    private DB db;

    public String lemmatize(String word, String language) throws JWNLException, FileNotFoundException, MalformedURLException, IOException, ParseException, Exception {
        if (nonLemetize(word) || word.contains("_")) {
            return word;
        }


        wordNetdictionary = getWordNetDictionary();
        IndexWordSet set = wordNetdictionary.lookupAllIndexWords(word);
        for (IndexWord iw : set.getIndexWordArray()) {
            return iw.getLemma();
        }
//        word = lmmtizeFromOnlineWordNet(word, language);
//        word = lemmatizeFromBabelNet(word, language);


        return word;
    }

    private static Dictionary getWordNetDictionary() {
        if (wordNetdictionary == null) {
            wordNetdictionary = Dictionary.getInstance();
        }
        return wordNetdictionary;
    }

    TermVertex getTermNodeByID(String word, String id, boolean fromDiec) throws FileNotFoundException, IOException, Exception {
        TermVertex node = null;
        String language = "EN";
        String key = getKey();
        String synet = getBabelnetSynset(id, language, key);
        node = TermVertexFactory.create(synet, language, word, id);
        if (node != null) {
            List<TermVertex> h = getHypernyms(language, id, key);
            node.setBroader(h);
        }
        return node;
    }

    public List<TermVertex> getTermNodeByLemma(String word, boolean isFromDictionary) throws IOException, MalformedURLException, ParseException, Exception {
        String key = getKey();
        String language = "EN";

        List<String> ids = getcandidateWordIDs(language, word, key);
        List<TermVertex> nodes = null;
        if (ids != null) {
            nodes = new ArrayList<>(ids.size());
            for (String id : ids) {
                String synet = getBabelnetSynset(id, language, key);
                TermVertex node = TermVertexFactory.create(synet, language, word, null);
                if (node != null) {
                    try {
                        List<TermVertex> h = getHypernyms(language, id, key);
                        node.setBroader(h);
                    } catch (Exception ex) {
                    }
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }

    public static POS[] getPOS(String s) throws JWNLException {
        // Look up all IndexWords (an IndexWord can only be one POS)
        wordNetdictionary = getWordNetDictionary();
        IndexWordSet set = wordNetdictionary.lookupAllIndexWords(s);
        // Turn it into an array of IndexWords
        IndexWord[] words = set.getIndexWordArray();
        // Make the array of POS
        POS[] pos = new POS[words.length];
        for (int i = 0; i < words.length; i++) {
            pos[i] = words[i].getPOS();
        }
        return pos;
    }

    public boolean hasPOS(POS[] keywordPOS, POS[] targetPOS) {
        for (POS p : keywordPOS) {
            for (POS t : targetPOS) {
                if (p.equals(t)) {
                    return true;
                }
            }
        }
        return false;
    }
//
//    private boolean canCompareKeyword(String line) throws JWNLException {
//        String[] parts = line.split(",");
//        String keyword = parts[0];
//        Integer count = Integer.valueOf(parts[1]);
//        if (count < 3) {
//            return false;
//        }
//        if (keyword.length() < 3) {
//            return false;
//        }
//        POS[] keywordPOS = getPOS(keyword);
//        boolean notExclusizeNoun = hasPOS(keywordPOS, new POS[]{POS.ADVERB, POS.ADJECTIVE, POS.VERB});
//        return !notExclusizeNoun;
//    }
//    private Synset getSynsetFromWordNet(IndexWord[] words) throws FileNotFoundException, IOException, JWNLException {
//        int index = -1;
//        try (BufferedReader br = new BufferedReader(new FileReader(keywordsDictionarayFile))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String keyword = line.split(",")[0];
//                if (canCompareKeyword(line)) {
//                    for (int i = 0; i < words.length; i++) {
//                        for (Synset s : words[i].getSenses()) {
//                            if (s.getGloss().contains(keyword)) {
//                                index = i;
//                                return s;
//                            }
//                        }
//
//                    }
//                }
//            }
//        }
//        return null;
//    }
//    public PointerTargetTree getRelatedTree(Synset sense, int depth, PointerType type) throws JWNLException {
//        PointerTargetTree relatedTree;
//
//        // Call a different function based on what type of relationship you are looking for
//        if (type == PointerType.HYPERNYM) {
//            relatedTree = PointerUtils.getInstance().getHypernymTree(sense, depth);
//        } else if (type == PointerType.HYPONYM) {
//            relatedTree = PointerUtils.getInstance().getHyponymTree(sense, depth);
//        } else {
//            relatedTree = PointerUtils.getInstance().getSynonymTree(sense, depth);
//        }
//        return relatedTree;
//    }

    private List<String> getcandidateWordIDs(String language, String word, String key) throws MalformedURLException, IOException, ParseException, Exception {
        if (db == null || db.isClosed()) {
            loadCache();
        }
        List<String> ids = wordIDCache.get(word);
        if (ids != null && ids.size() == 1 && ids.get(0).equals("NON-EXISTING")) {
            return null;
        }

        if (ids == null || ids.isEmpty()) {
            ids = new ArrayList<>();
            URL url = new URL("https://babelnet.io/v2/getSynsetIds?word=" + word + "&langs=" + language + "&langs=" + language + "&key=" + key);
            String genreJson = IOUtils.toString(url);
            handleKeyLimitException(genreJson);
            Object obj = JSONValue.parseWithException(genreJson);
            if (obj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) obj;
                for (Object o : jsonArray) {
                    JSONObject jo = (JSONObject) o;
                    if (jo != null) {
                        String id = (String) jo.get("id");
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                }
            } else if (obj instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) obj;
                String id = (String) jsonObj.get("id");
                if (id != null) {
                    ids.add(id);
                }
            }
            if (ids.isEmpty()) {
                ids.add("NON-EXISTING");
                wordIDCache.put(word, ids);
                db.commit();
                return null;
            }
            wordIDCache.put(word, ids);
            db.commit();
        }
        return ids;
    }

    private String getBabelnetSynset(String id, String lan, String key) throws IOException, Exception {
        if (db == null || db.isClosed()) {
            loadCache();
        }

        if (id == null || id.length() < 1) {
            return null;
        }
        String json = synsetCache.get(id);
        if (json != null && json.equals("NON-EXISTING")) {
            return null;
        }
        if (json == null) {
            URL url = new URL("https://babelnet.io/v2/getSynset?id=" + id + "&filterLangs=" + lan + "&langs=" + lan + "&key=" + key);
            json = IOUtils.toString(url);
            handleKeyLimitException(json);
            if (json != null) {
                synsetCache.put(id, json);
                db.commit();
            } else {
                synsetCache.put(id, "NON-EXISTING");
                db.commit();
            }
        }

        return json;
    }

    private void loadCache() throws FileNotFoundException, IOException {
        Logger.getLogger(BabelNet.class.getName()).log(Level.INFO, "Loading cache");
        db = DBMaker.newFileDB(cacheDBFile).make();
        synsetCache = db.getHashMap("synsetCacheDB");
        if (synsetCache == null) {
            synsetCache = db.createHashMap("synsetCacheDB").keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).make();

            if (synsetCacheFile.exists() && synsetCacheFile.length() > 1) {
                Logger.getLogger(BabelNet.class.getName()).log(Level.CONFIG, "Loading: {0}", synsetCacheFile.getAbsolutePath());
                try (BufferedReader br = new BufferedReader(new FileReader(synsetCacheFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.length() > 2) {
                            String[] parts = line.split("\t");
                            if (parts.length > 1) {
                                synsetCache.put(parts[0], parts[1]);
                            }
                        }
                    }
                }
            }
        }
        wordIDCache = db.get("wordIDCacheDB");
        if (wordIDCache == null) {
            wordIDCache = db.createHashMap("wordIDCacheDB").keySerializer(Serializer.STRING).valueSerializer(Serializer.BASIC).make();
            if (wordIDCacheFile.exists() && wordIDCacheFile.length() > 1) {
                Logger.getLogger(BabelNet.class.getName()).log(Level.CONFIG, "Loading: {0}", wordIDCacheFile.getAbsolutePath());
                try (BufferedReader br = new BufferedReader(new FileReader(wordIDCacheFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.length() > 2) {
                            String[] parts = line.split("\t");
                            if (parts.length > 1) {
                                List<String> ids = new ArrayList<>();
                                for (int i = 1; i < parts.length; i++) {
                                    ids.add(parts[i]);
                                }
                                wordIDCache.put(parts[0], ids);
                            }
                        }
                    }
                }
            }
        }

        disambiguateCache = db.get("disambiguateCacheDB");
        if (disambiguateCache == null) {
            disambiguateCache = db.createHashMap("").keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).make();
            if (disambiguateCacheFile.exists() && disambiguateCacheFile.length() > 1) {
                Logger.getLogger(BabelNet.class.getName()).log(Level.CONFIG, "Loading: {0}", disambiguateCacheFile.getAbsolutePath());
                try (BufferedReader br = new BufferedReader(new FileReader(disambiguateCacheFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.length() > 2) {
                            String[] parts = line.split("\t");
                            disambiguateCache.put(parts[0], parts[1]);
                        }
                    }
                }
            }
        }

        edgesCache = db.getHashMap("edgesCacheDB");
        if (edgesCache == null) {
            edgesCache = db.createHashMap("edgesCacheDB").keySerializer(Serializer.STRING).valueSerializer(Serializer.STRING).make();
            if (edgesCacheFile.exists() && edgesCacheFile.length() > 1) {
                Logger.getLogger(BabelNet.class.getName()).log(Level.CONFIG, "Loading: {0}", edgesCacheFile.getAbsolutePath());
                try (BufferedReader br = new BufferedReader(new FileReader(edgesCacheFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.length() > 2) {
                            String[] parts = line.split("\t");
                            edgesCache.put(parts[0], parts[1]);
                        }
                    }
                }
            }
        }


        db.commit();
    }

    public void saveCache() throws FileNotFoundException, IOException {
        Logger.getLogger(BabelNet.class.getName()).log(Level.INFO, "Saving cache");
//        deleteEntry("bn:03316494n");
//        deleteEntry("bn:00023236n");
        if (db != null) {
            if (!db.isClosed()) {
                db.commit();
                db.close();
            }
        }



//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(wordIDCacheFile, false))) {
//            for (String key : wordIDCache.keySet()) {
//                StringBuilder value = new StringBuilder();
//                for (String v : wordIDCache.get(key)) {
//                    value.append(v).append("\t");
//                }
//                bw.write(key + "\t" + value);
//                bw.newLine();
//                bw.flush();
//            }
//        }
//
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(synsetCacheFile, false))) {
//            for (String key : synsetCache.keySet()) {
//                bw.write(key + "\t" + synsetCache.get(key));
//                bw.newLine();
//                bw.flush();
//            }
//        }
//
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(disambiguateCacheFile, false))) {
//            for (String key : disambiguateCache.keySet()) {
//                bw.write(key + "\t" + disambiguateCache.get(key));
//                bw.newLine();
//                bw.flush();
//            }
//        }
//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(edgesCacheFile, false))) {
//            for (String key : edgesCache.keySet()) {
//                bw.write(key + "\t" + edgesCache.get(key));
//                bw.newLine();
//                bw.flush();
//            }
//        }
    }

    private static boolean nonLemetize(String word) throws FileNotFoundException, IOException {
        if (nonLematizedWords.isEmpty() || nonLematizedWords == null) {
            loadNonLematizeWords();
        }
        return nonLematizedWords.contains(word);
    }

    List<TermVertex> disambiguate(String language, String lemma, List<String> ngarms) throws MalformedURLException, IOException, ParseException, JWNLException, Exception {
        if (ngarms.isEmpty()) {
            return null;
        }
        if (ngarms.size() == 1 && ngarms.get(0).length() <= 1) {
            return null;
        }
        Logger.getLogger(BabelNet.class.getName()).log(Level.INFO, "lemma: {0}", lemma);
        HashMap<String, Double> idsMap = new HashMap<>();
        Map<String, TermVertex> termMap = new HashMap<>();
        List<TermVertex> terms = new ArrayList<>();
        int count = 0;
        int breaklimit = 1000;
        int oneElementlimit = 65;
        int difflimit = 60;
        Double persent;
        for (String n : ngarms) {
            if (n.length() <= 1) {
                continue;
            }
            count++;
            if (idsMap.size() == 1 && count > oneElementlimit) {
//                Double score = idsMap.values().iterator().next();
//                if (score >= 10) {
                break;
//                }
            }

            if ((count % 2) == 0 && idsMap.size() >= 2 && count > difflimit) {
                ValueComparator bvc = new ValueComparator(idsMap);
                TreeMap<String, Double> sorted_map = new TreeMap(bvc);
                sorted_map.putAll(idsMap);
                Iterator<String> iter = sorted_map.keySet().iterator();
                Double first = idsMap.get(iter.next());
                Double second = idsMap.get(iter.next());

                persent = first / (first + second);
//                System.err.println("first: " + first + " second: " + second + " persent: " + persent);
                if (persent > 0.65) {
                    break;
                }
            }
            if (count > breaklimit) {
                break;
            }

            String clearNg = n.replaceAll("_", " ");
            if (clearNg == null) {
                continue;
            }
            Pair<TermVertex, Double> termPair = null;
            try {
                termPair = disambiguate(language, lemma, clearNg);
            } catch (Exception ex) {
            }
            if (termPair != null) {
                termMap.put(termPair.first.getUID(), termPair.first);
                Double score;
                if (idsMap.containsKey(termPair.first.getUID())) {
                    score = idsMap.get(termPair.first.getUID());
//                    score++;
                    score += termPair.second;
                } else {
//                    score = 1.0;
                    score = termPair.second;
                }
//                System.err.println(termPair.first.getUID() + " : " + score + " : " + termPair.first.getLemma());
                idsMap.put(termPair.first.getUID(), score);
            }
        }
        if (!idsMap.isEmpty()) {
            ValueComparator bvc = new ValueComparator(idsMap);
            TreeMap<String, Double> sorted_map = new TreeMap(bvc);
            sorted_map.putAll(idsMap);
//            System.err.println(sorted_map);
            count = 0;
            Double firstScore = idsMap.get(sorted_map.firstKey());
            terms.add(termMap.get(sorted_map.firstKey()));
            idsMap.remove(sorted_map.firstKey());
            for (String tvID : sorted_map.keySet()) {
                if (count >= 1) {
                    Double secondScore = idsMap.get(tvID);
                    persent = secondScore / (firstScore + secondScore);
                    if (persent > 0.2) {
                        terms.add(termMap.get(tvID));
                    }
                    if (count >= 2) {
                        break;
                    }
                }
                count++;
            }
            return terms;
        }
        return null;
    }

    public Pair<TermVertex, Double> disambiguate(String language, String lemma, String sentence) throws MalformedURLException, IOException, ParseException, JWNLException, Exception {
        if (lemma == null || lemma.length() < 1) {
            return null;
        }
        if (db == null || db.isClosed()) {
            loadCache();
        }
        String key = getKey();
        sentence = sentence.replaceAll("_", " ");
        sentence = URLEncoder.encode(sentence, "UTF-8");
        String genreJson = disambiguateCache.get(sentence);
        if (genreJson != null && genreJson.equals("NON-EXISTING")) {
            return null;
        }
        if (genreJson == null) {
            URL url = new URL("https://babelfy.io/v1/disambiguate?text=" + sentence + "&lang=" + language + "&key=" + key);
            genreJson = IOUtils.toString(url);
            handleKeyLimitException(genreJson);
            if (!genreJson.isEmpty() || genreJson.length() < 1) {
                disambiguateCache.put(sentence, genreJson);
            } else {
                disambiguateCache.put(sentence, "NON-EXISTING");
            }
            db.commit();
        }
//        System.err.println(sentence);
        Object obj = JSONValue.parseWithException(genreJson);
//        TermVertex term = null;
        if (obj instanceof JSONArray) {
            JSONArray ja = (JSONArray) obj;
            for (Object o : ja) {
                JSONObject jo = (JSONObject) o;
                String id = (String) jo.get("babelSynsetID");
                Double score = (Double) jo.get("score");
                Double globalScore = (Double) jo.get("globalScore");
                Double coherenceScore = (Double) jo.get("coherenceScore");
                double someScore = (score + globalScore + coherenceScore) / 3.0;
                String synet = getBabelnetSynset(id, language, key);
                TermVertex t = TermVertexFactory.create(synet, language, lemma, null);
                if (t != null) {
                    List<TermVertex> h = getHypernyms(language, t.getUID(), key);
                    t.setBroader(h);
//                    System.err.println("id: " + id + " lemma: " + lemma + " score: " + score + " globalScore: " + globalScore + " coherenceScore: " + coherenceScore + " someScore: " + someScore);
                    return new Pair<>(t, someScore);
                }
            }
        }
        return null;
    }

    private Map<String, Double> getEdgeIDs(String language, String id, String relation, String key) throws MalformedURLException, IOException, ParseException, Exception {
        if (db == null || db.isClosed()) {
            loadCache();
        }
        String genreJson = edgesCache.get(id);
        if (genreJson == null) {
            URL url = new URL("https://babelnet.io/v2/getEdges?id=" + id + "&key=" + key);
            genreJson = IOUtils.toString(url);
            handleKeyLimitException(genreJson);
            if (genreJson != null) {
                edgesCache.put(id, genreJson);
            }
            if (genreJson == null) {
                edgesCache.put(id, "NON-EXISTING");
            }
            db.commit();
        }
        Object obj = JSONValue.parseWithException(genreJson);
        JSONArray edgeArray = (JSONArray) obj;
        Map<String, Double> map = new HashMap<>();
        for (Object o : edgeArray) {
            JSONObject pointer = (JSONObject) ((JSONObject) o).get("pointer");
            String relationGroup = (String) pointer.get("relationGroup");
//            System.err.println("relationGroup: "+relationGroup);
            if (relationGroup.equals(relation)) {
                String target = (String) ((JSONObject) o).get("target");
                Double normalizedWeight = (Double) ((JSONObject) o).get("normalizedWeight");
                Double weight = (Double) ((JSONObject) o).get("weight");
//                System.err.println("hyponym: " + id + " target: " + target + " normalizedWeight: " + normalizedWeight + " weight: " + weight);
                map.put(target, ((normalizedWeight + weight) / 2.0));
            }
        }
        return map;
    }

//    void getRelatedTree(String lemma, int i, PointerType HYPERNYM) throws JWNLException {
//        IndexWord iw = getWordNetDictionary().getIndexWord(POS.NOUN, lemma);
//        for (Synset s : iw.getSenses()) {
//            getRelatedTree(s, i, HYPERNYM);
//        }
//    }
//    private List<TermVertex> getSynonymsFromWordNet(IndexWordSet set) throws JWNLException {
//        List<TermVertex> synonyms = new ArrayList<>();
//        for (IndexWord iw : set.getIndexWordArray()) {
//            for (Synset s : iw.getSenses()) {
//                PointerTargetTree tree = getRelatedTree(s, 1, PointerType.SIMILAR_TO);
//                PointerTargetTreeNodeList children = tree.getRootNode().getChildTreeList();
//                for (Object ch : children) {
//                    PointerTargetTreeNode chNode = (PointerTargetTreeNode) ch;
//                    for (Word w : chNode.getSynset().getWords()) {
//                        String syn = w.getLemma().toLowerCase().replaceAll("(\\d+,\\d+)|\\d+", "");
//                        System.err.println("synonyms: " + syn);
//                        synonyms.add(new TermVertex(syn));
//                    }
//                }
//            }
//        }
//        return synonyms;
//    }
//    private String getBabelNetID(List<String> ids, String language, String key, StringBuilder categories) throws ParseException, IOException, JWNLException {
//        for (String id : ids) {
//            String synet = getBabelnetSynset(id, language, key);
//            JSONObject jSynet = (JSONObject) JSONValue.parseWithException(synet);
//            JSONArray categoriesArray = (JSONArray) jSynet.get("categories");
//            for (Object o : categoriesArray) {
//                JSONObject cat = (JSONObject) o;
//
//                String lang = (String) cat.get("language");
//                if (lang.equals(language)) {
//                    String[] parts = categories.toString().split("_");
//                    String category = ((String) cat.get("category")).toLowerCase();
//                    if (isInkeywordsDictionaray(lemmatize(category))) {
//                        return id;
//                    }
//                    for (String s : parts) {
//                        if (category.contains(s.toLowerCase())) {
//                            return id;
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }
//    private boolean isInkeywordsDictionaray(String hyper) throws FileNotFoundException, IOException {
//        try (BufferedReader br = new BufferedReader(new FileReader(keywordsDictionarayFile))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] parts = line.split(",");
//                Integer score = Integer.valueOf(parts[1]);
//                String keyword = parts[0];
//                if (score > 1 && keyword.equals(hyper)) {
//                    return true;
//                }
//            }
//        }
////        try (BufferedReader br = new BufferedReader(new FileReader(keywordsDictionarayFile))) {
////            String[] words = hyper.split("_");
////            String line;
////            for (String w : words) {
////                while ((line = br.readLine()) != null) {
////                    String[] parts = line.split(",");
////                    Integer score = Integer.valueOf(parts[1]);
////                    String keyword = parts[0];
////                    if (score > 1 && keyword.contains(w)) {
////                        return true;
////                    }
////                }
////            }
////        }
//        return false;
//    }
//    private TermVertex getTermFromGraph(String word) {
//        if (g != null) {
//            Set<TermVertex> vSet = g.vertexSet();
//            for (TermVertex term : vSet) {
//                List<TermVertex> hyper = term.getBroader();
//                if (hyper == null || hyper.isEmpty()) {
//                    continue;
//                }
//                List<TermVertex> syn = term.getSynonyms();
//                if (syn != null) {
//                    for (TermVertex s : syn) {
//                        int dist = edu.stanford.nlp.util.StringUtils.editDistance(s.getLemma(), word);
//                        if (dist < 15) {
//                            System.err.println("dist " + s.getLemma() + " " + word + "= " + dist);
//                        }
//                        if (s.getLemma().equals(word)) {
//                            return term;
//                        }
//                    }
//                }
//                if (term.getLemma().contains(word)) {
//                    return term;
//                }
//                if (term.getLemma().contains("_") && word.contains("_")) {
//                    String[] termParts = term.getLemma().split("_");
//                    String[] wordParts = word.split("_");
//                    for (String tp : termParts) {
//                        for (String wp : wordParts) {
//                            if (tp.equals(wp)) {
//                                TermVertex t = new TermVertex(word);
//                                t.setBroader(hyper);
//                                ArrayList<TermVertex> synon = new ArrayList<>();
//                                synon.add(term);
//                                t.setSynonyms(synon);
//                                return t;
//                            }
//                        }
//                    }
//                } else if (!term.getLemma().contains("_") && word.contains("_")) {
//                    String[] wordParts = word.split("_");
//                    for (String wp : wordParts) {
//                        if (term.getLemma().equals(wp)) {
//                            TermVertex t = new TermVertex(word);
//                            t.setBroader(hyper);
//                            ArrayList<TermVertex> synon = new ArrayList<>();
//                            synon.add(term);
//                            t.setSynonyms(synon);
//                            return t;
//                        }
//                    }
//
//                } else if (term.getLemma().contains("_") && !word.contains("_")) {
//                    String[] termParts = term.getLemma().split("_");
//                    for (String tp : termParts) {
//                        if (tp.equals(word)) {
//                            TermVertex t = new TermVertex(word);
//                            t.setBroader(hyper);
//                            ArrayList<TermVertex> synon = new ArrayList<>();
//                            synon.add(term);
//                            t.setSynonyms(synon);
//                            return t;
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }
    private static void loadNonLematizeWords() throws FileNotFoundException, IOException {
        if (nonLematizedWordsFile.exists() && nonLematizedWordsFile.length() > 1) {
            Logger.getLogger(BabelNet.class.getName()).log(Level.INFO, "Loading: {0}", nonLematizedWordsFile.getAbsolutePath());
            try (BufferedReader br = new BufferedReader(new FileReader(nonLematizedWordsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    nonLematizedWords.add(line);
                }
            }
        }
    }

    private List<TermVertex> getHypernyms(String language, String correctID, String key) throws MalformedURLException, IOException, ParseException, Exception {
        Map<String, Double> hypenymMap = getEdgeIDs(language, correctID, "HYPERNYM", key);
        List<TermVertex> hypernyms = new ArrayList<>();

        ValueComparator bvc = new ValueComparator(hypenymMap);
        Map<String, Double> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(hypenymMap);
        int maxNumOfHyper = 3;

        for (String h : sorted_map.keySet()) {
            if (maxNumOfHyper <= 0) {
                break;
            }
            String synetHyper = getBabelnetSynset(h, language, key);
            JSONObject jSynetHyper = (JSONObject) JSONValue.parseWithException(synetHyper);
            JSONArray sensestHyper = (JSONArray) jSynetHyper.get("senses");
            for (Object o : sensestHyper) {
                JSONObject jo = (JSONObject) o;
                String lang = (String) jo.get("language");
                if (lang.equals(language)) {
                    String lemma = (String) jo.get("lemma");
                    String id = (String) ((JSONObject) jo.get("synsetID")).get("id");
                    lemma = lemma.toLowerCase();
                    //                    String hyper = lemma.toLowerCase().replaceAll("(\\d+,\\d+)|\\d+", "");
                    //                    String detectedLang = Utils.detectLang(hyper);

                    if (lemma.length() > 1) {
                        TermVertex v = new TermVertex(lemma);
                        v.setUID(id);
                        v.setIsFromDictionary(false);
//                        System.err.println("hypo: " + correctID + " hyper: " + lemma + " id: " + id);
//                        hMap.put(lemma, v);
                        hypernyms.add(v);
                        break;
                    }

                }
            }
            maxNumOfHyper--;
        }
//        List list;
//        if (hMap.values() instanceof List) {
//            list = (List) hMap.values();
//        } else {
//            list = new ArrayList(hMap.values());
//        }
        return hypernyms;
    }

//    private List<String> getCategories(String id, String language, String key) throws IOException, ParseException, Exception {
//        String synet = getBabelnetSynset(id, language, key);
//        JSONObject jSynet = (JSONObject) JSONValue.parseWithException(synet);
//        JSONArray categoriesArray = (JSONArray) jSynet.get("categories");
//        List<String> categories = new ArrayList<>();
//        for (Object o : categoriesArray) {
//            JSONObject cat = (JSONObject) o;
//            String lang = (String) cat.get("language");
//            if (lang.equals(language)) {
//                String category = ((String) cat.get("category")).toLowerCase();
//                categories.add(category);
//            }
//        }
//        return categories;
//    }
    private void handleKeyLimitException(String genreJson) throws Exception {
        if (genreJson.contains("Your key is not valid or the daily requests limit has been reached")) {
            saveCache();
            throw new Exception(genreJson);
        }
    }

    private void deleteEntry(String key) {
        synsetCache.remove(key);
        wordIDCache.remove(key);
        disambiguateCache.remove(key);
        edgesCache.remove(key);
    }

    private String lmmtizeFromOnlineWordNet(String word, String language) throws IOException {
        if (lemmaCache == null) {
            lemmaCache = new HashMap<>();
        }
        String lemma = lemmaCache.get(word);
        if (lemma != null) {
            return lemma;
        } else {
            Document doc = Jsoup.connect("http://wordnetweb.princeton.edu/perl/webwn?s=" + word).get();
            Elements elements = doc.getElementsContainingText(" S: (");
            for (Element e : elements) {
                if (e.text().contains("S: (")) {
                    String wNetlemma = e.text().substring(e.text().indexOf("S: (") + "S: (".length());
                    wNetlemma = wNetlemma.substring(wNetlemma.indexOf(") ") + 2);
                    wNetlemma = wNetlemma.replaceAll("[^a-zA-Z\\s]", "");
                    wNetlemma = wNetlemma.substring(0, wNetlemma.indexOf(" ")).toLowerCase();

                    int dist = edu.stanford.nlp.util.StringUtils.editDistance(word, wNetlemma);
                    if (dist >= 4) {
                        lemmaCache.put(word, word);
                        return word;
                    }
                    if (dist <= 2) {
                        lemmaCache.put(word, wNetlemma);
                        return wNetlemma;
                    }

                    String longLemma, shortLemma;
                    String tmpWord = word;
                    if (language.equals("EN")) {
                        if (word.endsWith("ing") || word.endsWith("ies")) {
                            tmpWord = word.substring(0, 3);
                        }
                    }

//                    tmpWord = tmpWord.substring(0, word.length() - 1);
                    if (tmpWord.length() > wNetlemma.length()) {
                        longLemma = tmpWord;
                        shortLemma = wNetlemma;
                    } else {
                        shortLemma = tmpWord;
                        longLemma = wNetlemma;
                    }
//                    System.err.println("original: " + word + " shortLemma: " + shortLemma + " longLemma: " + longLemma + " dist: " + dist);
                    if (dist <= 3 && longLemma.startsWith(shortLemma)) {
                        lemmaCache.put(word, wNetlemma);
//                        System.err.println("return: " + wNetlemma);
                        return wNetlemma;
                    }
                }

            }
            lemmaCache.put(word, word);
        }
        return word;
    }

    private String lemmatizeFromBabelNet(String word, String language) throws MalformedURLException, UnsupportedEncodingException, IOException, ParseException, Exception {
        String key = getKey();
        String enWord = URLEncoder.encode(word, "UTF-8");
        List<String> ids = getcandidateWordIDs(language, enWord, key);
        if (ids == null || ids.isEmpty()) {
            return word;
        }
        for (String id : ids) {
            String synet;
            try {
                synet = getBabelnetSynset(id, language, key);
            } catch (Exception ex) {
                return word;
            }
            JSONObject jSynet = (JSONObject) JSONValue.parseWithException(synet);
            JSONArray senses = (JSONArray) jSynet.get("senses");
            if (senses != null) {
                for (Object o2 : senses) {
                    JSONObject jo2 = (JSONObject) o2;
//                    JSONObject synsetID = (JSONObject) jo2.get("synsetID");

                    String lang = (String) jo2.get("language");
                    if (lang.equals(language)) {
                        String jlemma = ((String) jo2.get("lemma")).toLowerCase();
//                        word = word.replaceAll("[^a-zA-Z ]", "");
                        word = word.replaceAll(" ", "_");
                        int dist = edu.stanford.nlp.util.StringUtils.editDistance(word, jlemma);
                        String lemma1, lemma2;
//                        System.err.println("original: " + word + " jlemma: " + jlemma + " lang " + lang + " dist: " + dist);
                        if (word.length() < jlemma.length()) {
                            lemma1 = word;
                            lemma2 = jlemma;
                        } else {
                            lemma2 = word;
                            lemma1 = jlemma;
                        }
                        if (dist <= 3 && lemma2.contains(lemma1)) {
                            return jlemma.replaceAll("_", " ");
                        }
                    }
                }
            }
        }
        return word;
    }
}
