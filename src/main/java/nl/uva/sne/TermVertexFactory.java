/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class TermVertexFactory {

    public static TermVertex create(String synet, String language, String lemma, String theID) throws ParseException, UnsupportedEncodingException {
        TermVertex node = null;
        JSONObject jSynet = (JSONObject) JSONValue.parseWithException(synet);

        JSONArray categoriesArray = (JSONArray) jSynet.get("categories");
        List<String> categories = null;
        if (categoriesArray != null) {

            categories = new ArrayList<>();
            for (Object o : categoriesArray) {
                JSONObject cat = (JSONObject) o;
                String lang = (String) cat.get("language");
                if (lang.equals(language)) {
                    String category = ((String) cat.get("category")).toLowerCase();
                    categories.add(category);
                }
            }
        }

        JSONArray glossesArray = (JSONArray) jSynet.get("glosses");

        List<String> glosses = null;
        if (glossesArray != null) {
            glosses = new ArrayList<>();

            for (Object o : glossesArray) {
                JSONObject gloss = (JSONObject) o;
                String lang = (String) gloss.get("language");
                if (lang.equals(language)) {
                    String g = ((String) gloss.get("gloss")).toLowerCase();
                    glosses.add(g);
                }
            }
        }

        JSONArray senses = (JSONArray) jSynet.get("senses");
        if (senses != null) {
            for (Object o2 : senses) {
                JSONObject jo2 = (JSONObject) o2;
                JSONObject synsetID = (JSONObject) jo2.get("synsetID");
                String babelNetID = (String) synsetID.get("id");

                String lang = (String) jo2.get("language");

                String lemma1, lemma2;
                if (lang.equals(language)) {
                    List<String> altLables = new ArrayList<>();
                    String jlemma = (String) jo2.get("lemma");
                    jlemma = jlemma.toLowerCase().replaceAll("(\\d+,\\d+)|\\d+", "");
                    altLables.add(jlemma);
                    if (theID != null && babelNetID.equals(theID)) {
                        node = new TermVertex(jlemma);
                        node.setUID(babelNetID);
                        node.setCategories(categories);
                        node.setAlternativeLables(altLables);
                        node.setGlosses(glosses);
                        return node;
                    }
                    lemma = java.net.URLDecoder.decode(lemma, "UTF-8");
                    lemma = lemma.replaceAll(" ", "_");
                    int dist;
                    if (!jlemma.startsWith("(") && jlemma.contains("(")) {
                        int index = jlemma.indexOf("(") - 1;
                        String sub = jlemma.substring(0, index);
                        dist = edu.stanford.nlp.util.StringUtils.editDistance(lemma, sub);
                    } else {
                        dist = edu.stanford.nlp.util.StringUtils.editDistance(lemma, jlemma);
                    }
//                    dist = edu.stanford.nlp.util.StringUtils.editDistance(lemma, jlemma);
                    if (dist <= 0) {
                        node = new TermVertex(jlemma);
                        node.setUID(babelNetID);
                        node.setCategories(categories);
                        node.setAlternativeLables(altLables);
                        node.setGlosses(glosses);
                        return node;
                    }
                    if (jlemma.contains(lemma) && jlemma.contains("_")) {
                        String[] parts = jlemma.split("_");
                        for (String p : parts) {
                            if (lemma.contains(p)) {
                                jlemma = p;
                                break;
                            }
                            if (p.contains(lemma)) {
                                jlemma = lemma;
                                break;
                            }
                        }
                    }
                    dist = edu.stanford.nlp.util.StringUtils.editDistance(lemma, jlemma);
                    if (lemma.length() < jlemma.length()) {
                        lemma1 = lemma;
                        lemma2 = jlemma;
                    } else {
                        lemma2 = lemma;
                        lemma1 = jlemma;
                    }
                    if (dist <= 3 && lemma2.contains(lemma1)) {
                        node = new TermVertex(jlemma);
                        node.setUID(babelNetID);
                        node.setCategories(categories);
                        node.setAlternativeLables(altLables);
                        node.setGlosses(glosses);
                        return node;
                    }
                }
            }
        }

        return null;
    }

    static TermVertex merge(TermVertex tv1, TermVertex tv2) throws Exception {
        if (!tv1.getUID().equals(tv2.getUID())) {
            throw new Exception("Can't merge differant terms. UID must be the same");
        }
        Set<String> altLables = new HashSet<>();
        List<String> a1 = tv1.getAlternativeLables();
        if (a1 != null) {
            altLables.addAll(a1);
        }
        List<String> a2 = tv2.getAlternativeLables();
        if (a2 != null) {
            altLables.addAll(a2);
        }

        Set<TermVertex> broader = new HashSet<>();
        List<TermVertex> b1 = tv1.getBroader();
        if (b1 != null) {
            broader.addAll(b1);
        }
        List<TermVertex> b2 = tv2.getBroader();
        if (b2 != null) {
            broader.addAll(b2);
        }

//        List<TermVertex> broads1 = tv1.getBroader();
//        List<TermVertex> broads2 = tv2.getBroader();
//        List<TermVertex> broader = new ArrayList();
//        for (TermVertex b : broads1) {
//            if (!broader.contains(b)) {
//                broader.add(b);
//            }
//        }
//        for (TermVertex b : broads2) {
//            if (!broader.contains(b)) {
//                broader.add(b);
//            }
//        }
        Set<TermVertex> narrower = new HashSet<>();
        List<TermVertex> n1 = tv1.getNarrower();
        if (n1 != null) {
            narrower.addAll(n1);
        }
        List<TermVertex> n2 = tv2.getNarrower();
        if (n2 != null) {
            narrower.addAll(n2);
        }

//                List<TermVertex> narrower = new ArrayList();
//        List<TermVertex> narrs1 = tv1.getNarrower();
//         List<TermVertex> narrs2 = tv2.getNarrower();
//        for (TermVertex n : narrs1) {
//            if (!narrower.contains(n)) {
//                narrower.add(n);
//            }
//        }
//        for (TermVertex n : narrs2) {
//            if (!broader.contains(n)) {
//                narrower.add(n);
//            }
//        }
        Set<String> broaderUIDS = new HashSet<>();
        List<String> buid1 = tv1.getBroaderUIDS();
        if (buid1 != null) {
            broaderUIDS.addAll(buid1);
        }
        List<String> buid2 = tv2.getBroaderUIDS();
        if (buid2 != null) {
            broaderUIDS.addAll(buid2);
        }

        Set<String> categories = new HashSet<>();
        List<String> c1 = tv1.getCategories();
        if (c1 != null) {
            categories.addAll(c1);
        }

        List<String> c2 = tv2.getCategories();
        if (c2 != null) {
            categories.addAll(c2);
        }

        String foreignKey;
        if (tv1.getForeignKey() != null) {
            foreignKey = tv1.getForeignKey();
        } else {
            foreignKey = tv2.getForeignKey();
        }

        Set<String> glosses = new HashSet<>();
        List<String> g1 = tv1.getGlosses();
        if (g1 != null) {
            glosses.addAll(g1);
        }
        List<String> g2 = tv2.getGlosses();
        if (g1 != null) {
            glosses.addAll(g2);
        }

        boolean idFromDic = false;
        if (tv1.getIsFromDictionary() && tv2.getIsFromDictionary()) {
            idFromDic = true;
        }
        if (!tv1.getLemma().equals(tv2.getLemma())) {
            altLables.add(tv2.getLemma());
        }
        String lemma = tv1.getLemma();

        Set<String> narrowerUIDS = new HashSet<>();
        Set<String> nuid1 = tv1.getNarrowerUIDS();
        if (nuid1 != null) {
            narrowerUIDS.addAll(nuid1);
        }
        Set<String> nuid2 = tv2.getNarrowerUIDS();
        if (nuid2 != null) {
            narrowerUIDS.addAll(nuid2);
        }

        Set<TermVertex> synonyms = new HashSet<>();
        List<TermVertex> s1 = tv1.getSynonyms();
        if (s1 != null) {
            synonyms.addAll(s1);
        }

        List<TermVertex> s2 = tv2.getSynonyms();
        if (s2 != null) {
            synonyms.addAll(s2);
        }

        TermVertex mtv = new TermVertex(lemma);
        mtv.setUID(tv1.getUID());
        mtv.setAlternativeLables(new ArrayList<>(altLables));
        mtv.setBroader(new ArrayList<>(broader));
        mtv.setBroaderUIDS(new ArrayList<>(broaderUIDS));
        mtv.setCategories(new ArrayList<>(categories));
        mtv.setForeignKey(foreignKey);
        mtv.setGlosses(new ArrayList<>(glosses));
        mtv.setIsFromDictionary(idFromDic);
        mtv.setNarrowerUIDS(narrowerUIDS);
        mtv.setNarrower(new ArrayList<>(narrower));
        mtv.setSynonyms(new ArrayList<>(synonyms));

        return mtv;
    }
}
