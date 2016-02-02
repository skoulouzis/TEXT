/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 *
 * @author S. Koulouzis
 */
public class TermVertexFactory {

    public static TermVertex create(String synet, String language, String lemma) throws ParseException, UnsupportedEncodingException {
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
//                System.err.println("category: " + category);
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
                    lemma = java.net.URLDecoder.decode(lemma, "UTF-8");
                    lemma = lemma.replaceAll(" ", "_");
                    int dist = edu.stanford.nlp.util.StringUtils.editDistance(lemma, jlemma);
                    if (lemma.length() < jlemma.length()) {
                        lemma1 = lemma;
                        lemma2 = jlemma;
                    } else {
                        lemma2 = lemma;
                        lemma1 = jlemma;
                    }
//                    System.err.println("original: " + lemma + " jlemma: " + jlemma + " id: " + babelNetID + " lang " + lang + " dist: " + dist);
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
//        else {
//            System.err.println(synet + " lemma: " + lemma);
//        }

        return null;
    }
}
