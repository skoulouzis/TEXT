/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author S. Koulouzis
 */
public class TermVertex {

    private String lemma;
    private List<TermVertex> broader;
    private String uid;
    private List<TermVertex> synonyms;
    private List<String> categories;
    private boolean fromDictionary;
    private List<String> altLables;
    private List<String> glosses;
    private List<String> buids;
    private List<String> nuids;
    private String fKey;

    public TermVertex(String lemma) {
        this.lemma = lemma;
    }

    void setBroader(List<TermVertex> broader) {
        broader.equals(this);
        this.broader = broader;
    }

    List<TermVertex> getBroader() {
        return this.broader;
    }

    void setUID(String uid) {
        this.uid = uid;
    }

    void setSynonyms(List<TermVertex> synonyms) {
        this.synonyms = synonyms;
    }

    List<TermVertex> getSynonyms() {
        return this.synonyms;
    }

    String getLemma() {
        return lemma;
    }

    void setIsFromDictionary(boolean fromDict) {
        this.fromDictionary = fromDict;
    }

    boolean getIsFromDictionary() {
        return this.fromDictionary;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TermVertex) {
            TermVertex v = (TermVertex) obj;
            return (v.getLemma().equals(this.lemma));
        }
        return false;
    }

    @Override
    public int hashCode() {
//        int hash = 7;
//        hash = 29 * hash + Objects.hashCode(this.lemma);
        return Objects.hashCode(this.lemma);
    }

    public String getUID() {
        return this.uid;
    }

    public List<String> getCategories() {
        return this.categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    void setAlternativeLables(List<String> altLables) {
        this.altLables = altLables;
    }

    public List<String> getAlternativeLables() {
        return this.altLables;
    }

    void setGlosses(List<String> glosses) {
        this.glosses = glosses;
    }

    public List<String> getGlosses() {
        return this.glosses;
    }

    void setBroaderUIDS(List<String> buids) {
        this.buids = buids;
    }

    List<String> getNarrowerUIDS() {
        return this.nuids;
    }

    void setNarrowerUIDS(List<String> nuids) {
        this.nuids = nuids;
    }

    List<String> getBroaderUIDS() {
        return this.buids;
    }

    void setForeignKey(String fKey) {
        this.fKey = fKey;
    }

    String getForeignKey() {
        return this.fKey;
    }

    @Override
    public String toString() {
        return this.lemma + "-" + uid;
    }
}
