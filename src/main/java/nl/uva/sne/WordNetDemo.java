/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.dictionary.Dictionary;

/**
 *
 * code from http://shiffman.net/itp/classes/a2z/wordnet/WordNetDemo.java
 */
public class WordNetDemo {

    private static Dictionary dictionary;

    static {
        try {
            JWNL.initialize(new FileInputStream(System.getProperty("user.home")
                    + File.separator + "workspace" + File.separator + "TEXT"
                    + File.separator + "etc" + File.separator + "file_properties.xml"));
        } catch (JWNLException | FileNotFoundException ex) {
            Logger.getLogger(WordNetDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void demonstrateTreeOperation(String word) throws JWNLException {
        // Get all the hyponyms (children) of the first sense of <var>word</var>
        IndexWord dicWord = Dictionary.getInstance().getIndexWord(POS.NOUN, word);
        if (dicWord != null) {
            PointerTargetTree hyponyms = PointerUtils.getInstance().getHyponymTree(dicWord.getSense(1));
            System.out.println("Hyponyms of \"" + dicWord.getLemma() + "\":");
            hyponyms.print();
        }
    }

    public static String lemmatize(String word) throws JWNLException {
        dictionary = getDictionary();
        IndexWordSet set = dictionary.lookupAllIndexWords(word);
        IndexWord[] words = set.getIndexWordArray();
        if (words.length < 1) {
            return word;
        }
        for (int i = 0; i < words.length; i++) {
            return words[i].getLemma();

        }
        return null;
    }

    public static PointerTargetTree showRelatedTree(Synset sense, int depth, PointerType type) throws JWNLException {

        PointerTargetTree relatedTree;
        // Call a different function based on what type of relationship you are looking for
        if (type == PointerType.HYPERNYM) {
            relatedTree = PointerUtils.getInstance().getHypernymTree(sense, depth);
        } else if (type == PointerType.HYPONYM) {
            relatedTree = PointerUtils.getInstance().getHyponymTree(sense, depth);
        } else {
            relatedTree = PointerUtils.getInstance().getSynonymTree(sense, depth);
        }
        // If we really need this info, we wil have to write some code to Process the tree
        // Not just display it  
        relatedTree.print();
        return relatedTree;
    }

    public static PointerTargetTree showRelatedTree(IndexWord word, int depth, PointerType type) throws JWNLException {
        if (word.getSense(1) != null) {
            return showRelatedTree(word.getSense(1), depth, type);
        }
        return null;
    }

    public static PointerTargetTree showRelatedTree(String word, int depth) throws JWNLException {
        System.out.println("root: " + word);
        dictionary = getDictionary();

        IndexWordSet set = dictionary.lookupAllIndexWords(word);
        IndexWord[] words = set.getIndexWordArray();
        for (int i = 0; i < words.length; i++) {
//            for (Synset s : words[i].getSenses()) {
//                System.out.println("getGloss: " + s.getGloss());
//                System.out.println("getLexFileName: " + s.getLexFileName());
//                System.out.println("getWords: " + s.getWords());
//            }
            return showRelatedTree(words[i].getSense(1), depth, PointerType.HYPERNYM);
        }
        return null;
    }

    private static Dictionary getDictionary() {
        if (dictionary == null) {
            dictionary = Dictionary.getInstance();
        }
        return dictionary;
    }

    private static POS[] getPOS(String s) throws JWNLException {
        // Look up all IndexWords (an IndexWord can only be one POS)
        dictionary = getDictionary();
        IndexWordSet set = dictionary.lookupAllIndexWords(s);
        // Turn it into an array of IndexWords
        IndexWord[] words = set.getIndexWordArray();
        // Make the array of POS
        POS[] pos = new POS[words.length];
        for (int i = 0; i < words.length; i++) {
            pos[i] = words[i].getPOS();
        }
        return pos;
    }

    public static void listGlosses(IndexWord word) throws JWNLException {
        System.out.println("\n\nDefinitions for " + word.getLemma() + ":");
        // Get an array of Synsets for a word
        Synset[] senses = word.getSenses();
        // Display all definitions
        for (int i = 0; i < senses.length; i++) {
            System.out.println(word + ": " + senses[i].getGloss());
        }
    }

    public static void listGlosses(String word) throws JWNLException {
        dictionary = getDictionary();
        POS[] pos = getPOS(word);
        for (POS p : pos) {
            IndexWord dicWord = dictionary.getIndexWord(p, word);
            if (dicWord != null) {
                listGlosses(dicWord);
            }

        }
    }

    public static void findRelatedWords(String word) throws JWNLException {
        POS[] pos = getPOS(word);
        for (POS p : pos) {
            IndexWord dicWord = dictionary.getIndexWord(p, word);
            if (dicWord != null) {
                findRelatedWords(dicWord, PointerType.SIMILAR_TO);
            }
        }
    }

    public static void findRelatedWords(IndexWord w, PointerType type) throws JWNLException {
        System.out.println("\n\nI am now going to find related words for " + w.getLemma() + ", listed in groups by sense.");
        System.out.println("We'll look for relationships of type " + type.getLabel() + ".");

        // Call a function that returns an ArrayList of related senses
        ArrayList a = getRelated(w, type);
        if (a == null || a.isEmpty()) {
            System.out.println("Hmmm, I didn't find any related words.");
        } else {
            // Display the words for all the senses
            for (int i = 0; i < a.size(); i++) {
                Synset s = (Synset) a.get(i);
                Word[] words = s.getWords();
                for (int j = 0; j < words.length; j++) {
                    System.out.print(words[j].getLemma());
                    if (j != words.length - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }
        }
    }

    // Related words for a given sense (do synonyms by default)
    // Probably should implement all PointerTypes
    public static ArrayList getRelated(Synset sense, PointerType type) throws JWNLException, NullPointerException {
        PointerTargetNodeList relatedList;
        // Call a different function based on what type of relationship you are looking for
        if (type == PointerType.HYPERNYM) {
            relatedList = PointerUtils.getInstance().getDirectHypernyms(sense);
        } else if (type == PointerType.HYPONYM) {
            relatedList = PointerUtils.getInstance().getDirectHyponyms(sense);
        } else {
            relatedList = PointerUtils.getInstance().getSynonyms(sense);
        }
        // Iterate through the related list and make an ArrayList of Synsets to send back
        Iterator i = relatedList.iterator();
        ArrayList a = new ArrayList();
        while (i.hasNext()) {
            PointerTargetNode related = (PointerTargetNode) i.next();
            Synset s = related.getSynset();
            a.add(s);
        }
        return a;
    }

    // Just gets the related words for first sense of a word
    // Revised to get the list of related words for the 1st Synset that has them
    // We might want to try all of them
    public static ArrayList getRelated(IndexWord word, PointerType type) throws JWNLException {
        try {
            Synset[] senses = word.getSenses();
            // Look for the related words for all Senses
            for (int i = 0; i < senses.length; i++) {
                ArrayList a = getRelated(senses[i], type);
                // If we find some, return them
                if (a != null && !a.isEmpty()) {
                    return a;
                }
            }
        } catch (NullPointerException e) {
            // System.out.println("Oops, NULL problem: " + e);
        }
        return null;
    }
}
