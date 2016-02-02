/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org._3pq.jgrapht.edge.DirectedWeightedEdge;
import org.json.simple.parser.ParseException;
import org.semanticweb.skos.AddAssertion;
import org.semanticweb.skos.SKOSAnnotation;
import org.semanticweb.skos.SKOSAssertion;
import org.semanticweb.skos.SKOSChange;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSCreationException;
import org.semanticweb.skos.SKOSDataFactory;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSEntity;
import org.semanticweb.skos.SKOSLiteral;
import org.semanticweb.skos.SKOSObjectRelationAssertion;
import org.semanticweb.skos.SKOSUntypedLiteral;
import org.semanticweb.skosapibinding.SKOSManager;

/**
 *
 * @author S. Koulouzis
 */
class SkosUtils {

    public static final String SKOS_URI = "http://uva.sne.nl/";
    private static SKOSDataFactory skosDF;
    private static SKOSDataset dataset;
    private static SKOSManager manager;

    public static SKOSDataFactory getSKOSDataFactory() throws SKOSCreationException {
        if (skosDF == null) {
            skosDF = getSKOSManager().getSKOSDataFactory();
        }
        return skosDF;
    }

    public static SKOSManager getSKOSManager() throws SKOSCreationException {
        if (manager == null) {
            manager = new SKOSManager();
        }
        return manager;
    }

    public static SKOSDataset getSKOSDataset() throws SKOSCreationException {
        if (dataset == null) {
            dataset = getSKOSManager().createSKOSDataset(URI.create(SKOS_URI));
        }
        return dataset;
    }

    public static List<SKOSChange> create(DirectedWeightedEdge edge, String lang) throws ParseException, SKOSCreationException {

        TermVertex target = (TermVertex) edge.getTarget();
        TermVertex source = (TermVertex) edge.getSource();

        List<SKOSChange> addAssertions = new ArrayList<>();
        SKOSConcept targetConcept = getSKOSDataFactory().getSKOSConcept(URI.create(SKOS_URI + "#" + target.getUID()));

        addAssertions.add(new AddAssertion(getSKOSDataset(), getPrefAssertion(targetConcept, target.getLemma(), lang)));
        List<String> alt = target.getAlternativeLables();
        if (alt != null) {
            for (String s : alt) {
                if (!s.equals(target.getLemma())) {
                    addAssertions.add(new AddAssertion(getSKOSDataset(), getAltAssertion(targetConcept, s, lang)));
                }
            }
        }

        List<String> cats = target.getCategories();
        if (cats != null) {
            for (String cat : cats) {
                addAssertions.add(new AddAssertion(getSKOSDataset(), getCategoryAssertion(targetConcept, cat, lang)));
            }
        }
        List<String> glosses = target.getGlosses();
        if (glosses != null) {
            for (String gloss : glosses) {
                addAssertions.add(new AddAssertion(getSKOSDataset(), getDefinitionAassertion(targetConcept, gloss, lang)));
            }
        }


        SKOSConcept sourceConcept = getSKOSDataFactory().getSKOSConcept(URI.create(SKOS_URI + "#" + source.getUID()));
        addAssertions.add(new AddAssertion(getSKOSDataset(), getPrefAssertion(sourceConcept, source.getLemma(), lang)));

        alt = source.getAlternativeLables();
        if (alt != null) {
            for (String s : alt) {
                if (!s.equals(source.getLemma())) {
                    addAssertions.add(new AddAssertion(getSKOSDataset(), getAltAssertion(sourceConcept, s, lang)));
                }
            }
        }
        cats = source.getCategories();
        if (cats != null) {
            for (String cat : cats) {
                addAssertions.add(new AddAssertion(getSKOSDataset(), getCategoryAssertion(sourceConcept, cat, lang)));
            }
        }
        glosses = source.getGlosses();
        if (glosses != null) {
            for (String gloss : glosses) {
                addAssertions.add(new AddAssertion(getSKOSDataset(), getDefinitionAassertion(sourceConcept, gloss, lang)));
            }
        }


        SKOSObjectRelationAssertion broaderPropertyRelationAssertion = getSKOSDataFactory().
                getSKOSObjectRelationAssertion(sourceConcept, getSKOSDataFactory().getSKOSBroaderProperty(), targetConcept);
        addAssertions.add(new AddAssertion(getSKOSDataset(), broaderPropertyRelationAssertion));


        SKOSObjectRelationAssertion narrowerPropertyRelationAssertion = getSKOSDataFactory().
                getSKOSObjectRelationAssertion(targetConcept, getSKOSDataFactory().getSKOSNarrowerProperty(), sourceConcept);
        addAssertions.add(new AddAssertion(getSKOSDataset(), narrowerPropertyRelationAssertion));


        return addAssertions;
    }

    private static SKOSAssertion getCategoryAssertion(SKOSConcept concept, String category, String lang) throws SKOSCreationException {
        SKOSAnnotation annotation = getSKOSDataFactory().getSKOSAnnotation(getSKOSDataFactory().
                getSKOSNotationProperty().getURI(), category, lang);
        return getSKOSDataFactory().getSKOSAnnotationAssertion(concept, annotation);
    }

    private static SKOSAssertion getDefinitionAassertion(SKOSConcept concept, String gloss, String lang) throws SKOSCreationException {
        SKOSAnnotation annotation = getSKOSDataFactory().getSKOSAnnotation(getSKOSDataFactory().getSKOSDefinitionObjectProperty().getURI(), gloss, lang);
        return getSKOSDataFactory().getSKOSAnnotationAssertion(concept, annotation);
    }

    private static SKOSAssertion getAltAssertion(SKOSConcept concept, String s, String lang) throws SKOSCreationException {
        SKOSAnnotation altLabel = getSKOSDataFactory().getSKOSAnnotation(getSKOSDataFactory().
                getSKOSAltLabelProperty().getURI(), s, lang);
        return getSKOSDataFactory().getSKOSAnnotationAssertion(concept, altLabel);
    }

    private static SKOSAssertion getPrefAssertion(SKOSConcept concept, String lemma, String lang) throws SKOSCreationException {
        SKOSAnnotation prefLabel = getSKOSDataFactory().getSKOSAnnotation(getSKOSDataFactory().getSKOSPrefLabelProperty().getURI(), lemma, lang);
        return getSKOSDataFactory().getSKOSAnnotationAssertion(concept, prefLabel);
    }

    static String getPrefLabelValue(SKOSDataset dataset, SKOSConcept concept, String language) throws SKOSCreationException {

        Set<SKOSAnnotation> prefLabel = dataset.getSKOSAnnotationsByURI(concept, getSKOSDataFactory().getSKOSPrefLabelProperty().getURI());
        SKOSAnnotation ann = prefLabel.iterator().next();
        // if the annotation is a literal annotation?

        String value, lang = null;

        if (ann.isAnnotationByConstant()) {
            SKOSLiteral literal = ann.getAnnotationValueAsConstant();
            value = literal.getLiteral();
            if (!literal.isTyped()) {
                // if it has  language
                SKOSUntypedLiteral untypedLiteral = literal.getAsSKOSUntypedLiteral();
                if (untypedLiteral.hasLang()) {
                    lang = untypedLiteral.getLang();
                }
            }
        } else {
            // annotation is some resource
            SKOSEntity entity = ann.getAnnotationValue();
            value = entity.getURI().getFragment();
        }
        if (lang != null && lang.equals(language)) {
            return value;
        }
        return null;
    }

    static String getUID(SKOSConcept concept, File taxonomyFile) {
        return concept.getURI().toString().replace(taxonomyFile.toURI().toString() + "#", "");
    }

    static List<String> getAltLabelValues(SKOSDataset dataset, SKOSConcept concept, String language) throws SKOSCreationException {
        List<String> altLabels = new ArrayList<>();
        Set<SKOSAnnotation> altLabel = dataset.getSKOSAnnotationsByURI(concept, getSKOSDataFactory().getSKOSAltLabelProperty().getURI());
        Iterator<SKOSAnnotation> iter = altLabel.iterator();

        String value, lang = null;
        while (iter.hasNext()) {
            SKOSAnnotation ann = iter.next();
            if (ann.isAnnotationByConstant()) {
                SKOSLiteral literal = ann.getAnnotationValueAsConstant();
                value = literal.getLiteral();
                if (!literal.isTyped()) {
                    // if it has  language
                    SKOSUntypedLiteral untypedLiteral = literal.getAsSKOSUntypedLiteral();
                    if (untypedLiteral.hasLang()) {
                        lang = untypedLiteral.getLang();
                    }
                }
            } else {
                // annotation is some resource
                SKOSEntity entity = ann.getAnnotationValue();
                value = entity.getURI().getFragment();
            }
            if (lang != null && lang.equals(language)) {
                altLabels.add(value);
            }
        }
        return altLabels;
    }

    static List<String> getBroaderUIDs(SKOSDataset dataset, SKOSConcept concept) throws SKOSCreationException {
        List<String> broaderUIDs = new ArrayList<>();
        Set<SKOSAnnotation> broaderLabel = dataset.getSKOSAnnotationsByURI(concept, getSKOSDataFactory().getSKOSBroaderProperty().getURI());
        Iterator<SKOSAnnotation> iter = broaderLabel.iterator();

        String value = null;
        while (iter.hasNext()) {
            SKOSAnnotation ann = iter.next();
            if (ann.isAnnotationByConstant()) {
                SKOSLiteral literal = ann.getAnnotationValueAsConstant();
                value = literal.getLiteral();
            } else {
                // annotation is some resource
                SKOSEntity entity = ann.getAnnotationValue();
                value = entity.getURI().getFragment();
            }
//            if (lang != null && lang.equals(language)) {
            broaderUIDs.add(value);
//            }
        }
        return broaderUIDs;
    }

    static List<String> getNarrowerUIDs(SKOSDataset dataset, SKOSConcept concept) throws SKOSCreationException {
        List<String> narrowerUIDs = new ArrayList<>();
        Set<SKOSAnnotation> narrowerLabel = dataset.getSKOSAnnotationsByURI(concept, getSKOSDataFactory().getSKOSNarrowerProperty().getURI());
        Iterator<SKOSAnnotation> iter = narrowerLabel.iterator();

        String value = null;
        while (iter.hasNext()) {
            SKOSAnnotation ann = iter.next();
            if (ann.isAnnotationByConstant()) {
                SKOSLiteral literal = ann.getAnnotationValueAsConstant();
                value = literal.getLiteral();
            } else {
                // annotation is some resource
                SKOSEntity entity = ann.getAnnotationValue();
                value = entity.getURI().getFragment();
            }
            narrowerUIDs.add(value);
        }
        return narrowerUIDs;
    }
}
