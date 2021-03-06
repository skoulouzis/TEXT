/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    private Set<String> nuids;
    private String fKey;
    private List<TermVertex> narrower;

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
            return (v.getUID().equals(this.uid));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.uid);
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

    Set<String> getNarrowerUIDS() {
        return this.nuids;
    }

    void setNarrowerUIDS(Set<String> nuids) {
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

    void addNarrowerUID(String uid) {
        if (this.nuids == null) {
            nuids = new HashSet<>();
        }
        nuids.add(uid);
    }

    List<TermVertex> getNarrower() {
        return this.narrower;
    }

    void addNarrower(TermVertex tv) {
        if (narrower == null) {
            this.narrower = new ArrayList<>();
        }
        narrower.add(tv);
    }

    void setNarrower(ArrayList<TermVertex> narrower) {
        this.narrower = narrower;
    }

    void addBroaderUID(String buid) {
        if (this.buids == null) {
            this.buids = new ArrayList<>();
        }
        this.buids.add(buid);
    }

    void addBroader(TermVertex tv) {
        if (this.broader == null) {
            this.broader = new ArrayList<>();
        }
        this.broader.add(tv);
    }
}
