/* 
 *  COPYRIGHT (c) 2008-2009 by Institute of Computer Science, 
 *  Foundation for Research and Technology - Hellas
 *  Contact: 
 *      POBox 1385, Heraklio Crete, GR-700 13 GREECE
 *      Tel:+30-2810-391632
 *      Fax: +30-2810-391638
 *      E-mail: isl@ics.forth.gr
 *      http://www.ics.forth.gr/isl
 *
 *   Authors  :  Dimitris Andreou, Nelly Vouzoukidou.
 *
 *   This file is part of SWKM model APIs (see also http://athena.ics.forth.gr:9090/SWKM/).
 *
 *    SWKM model APIs is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *   SWKM model APIs is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with SWKM model APIs.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *   SWKM has been partially supported by EU project KP-Lab (IP IST-27490) kp-lab.org
 */


package gr.forth.ics.swkm.model2.util;

import gr.forth.ics.swkm.model2.*;
import com.google.common.collect.Lists;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.util.RandomChooser;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.util.Assert;


/**
 * A randomized triple generator.
 * Example:
 *
 * <pre>{@code
 *Model model = ...;
 *Iterator<Triple> triples = TripleGenerator.newEmpty()
 *    .setProb(Event.NEW_CLASS, 1.0)
 *    .setProb(Event.TURN_CLASS_TO_METACLASS, 1.0)
 *    .triplesFor(model);
 *for (int i = 0; i < 10000; i++) {
 *    System.out.println(triples.next());
 *}
 *}
 * </pre>
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class RandomTripleGenerator {
    private final Map<Event, Double> probabilities =
            new EnumMap<Event, Double>(Event.class);

    private RandomTripleGenerator() {
    }

    public static RandomTripleGenerator newEmpty() {
        return new RandomTripleGenerator();
    }

    public static RandomTripleGenerator newDefault() {
        RandomTripleGenerator tg = new RandomTripleGenerator();
        for (Event event : Event.values()) {
            tg.setProb(event, 1.0);
        }
        tg.setProb(Event.NEW_INDIVIDUAL, 0.5);
        tg.setProb(Event.NEW_BLANK_INDIVIDUAL, 0.5);
        
        tg.setProb(Event.NEW_STATEMENT, 0.5);
        tg.setProb(Event.NEW_BLANK_STATEMENT, 0.5);
        
        tg.setProb(Event.NEW_ALT, 0.5);
        tg.setProb(Event.NEW_BLANK_ALT, 0.5);
        
        tg.setProb(Event.NEW_SEQ, 0.5);
        tg.setProb(Event.NEW_BLANK_SEQ, 0.5);
        
        tg.setProb(Event.NEW_BAG, 0.5);
        tg.setProb(Event.NEW_BLANK_BAG, 0.5);
        return tg;
    }

    public enum Event {
        NEW_NAMED_GRAPH,
        NEW_INDIVIDUAL,
        NEW_BLANK_INDIVIDUAL,
        NEW_CLASS,
        NEW_PROPERTY,
        NEW_METACLASS,
        NEW_METAPROPERTY,
        NEW_STATEMENT,
        NEW_BLANK_STATEMENT,
        NEW_ALT,
        NEW_BLANK_ALT,
        NEW_BAG,
        NEW_BLANK_BAG,
        NEW_SEQ,
        NEW_BLANK_SEQ,
        TURN_CLASS_TO_METACLASS,
        TURN_PROPERTY_TO_METAPROPERTY
    }

    public RandomTripleGenerator setProb(Event event, Double probability) {
        probabilities.put(event, probability);
        return this;
    }

    public Double getProb(Event event) {
        Double prob = probabilities.get(event);
        if (prob == null) {
            return 0.0;
        }
        return prob;
    }

    public Iterator<Triple> triplesFor(Model model) {
        Assert.notNull(model);
        return new TripleStream(model, this);
    }

    private static class TripleStream implements Iterator<Triple> {
        private final Model model;
        private final Random random = new Random(0);

        private final UniqueNames names = new UniqueNames();

        private final Uri dummy = Uri.parse("http://ns#dummy");
        private final List<Resource> namedGraphs = Lists.newArrayList((Resource)null);

        private final RandomChooser<ModelAction> actionChooser;

        TripleStream(Model model, RandomTripleGenerator tg) {
            this.model = model;
            actionChooser = RandomChooser.<ModelAction>newInstance()
                    .setRandom(random)
                    .choice(new NewNamedGraph(), tg.getProb(Event.NEW_NAMED_GRAPH))
                    .choice(new NewIndividual(), tg.getProb(Event.NEW_INDIVIDUAL))
                    .choice(new NewBlankIndividual(), tg.getProb(Event.NEW_BLANK_INDIVIDUAL))
                    .choice(new NewClass(), tg.getProb(Event.NEW_CLASS))
                    .choice(new NewProperty(), tg.getProb(Event.NEW_PROPERTY))
                    .choice(new NewMetaclass(), tg.getProb(Event.NEW_METACLASS))
                    .choice(new NewMetaproperty(), tg.getProb(Event.NEW_METAPROPERTY))
                    .choice(new NewStatement(), tg.getProb(Event.NEW_STATEMENT))
                    .choice(new NewBlankStatement(), tg.getProb(Event.NEW_BLANK_STATEMENT))
                    .choice(new NewAlt(), tg.getProb(Event.NEW_ALT))
                    .choice(new NewBlankAlt(), tg.getProb(Event.NEW_BLANK_ALT))
                    .choice(new NewBag(), tg.getProb(Event.NEW_BAG))
                    .choice(new NewBlankBag(), tg.getProb(Event.NEW_BLANK_BAG))
                    .choice(new NewSeq(), tg.getProb(Event.NEW_SEQ))
                    .choice(new NewBlankSeq(), tg.getProb(Event.NEW_BLANK_SEQ))
                    .choice(new ClassToMetaclass(), tg.getProb(Event.TURN_CLASS_TO_METACLASS))
                    .choice(new PropertyToMetaproperty(), tg.getProb(Event.TURN_PROPERTY_TO_METAPROPERTY))
                    .build();
        }

        public boolean hasNext() {
            return true;
        }

        public Triple next() {
            while (true) {
                ModelAction action = actionChooser.choose();
                Triple triple = action.call();
                if (triple != null) {
                    return triple;
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        private Resource randomGraph() {
            return namedGraphs.get(random.nextInt(namedGraphs.size()));
        }

        private class NewNamedGraph implements ModelAction {
            public Triple call() {
                Resource newNamedGraph = model.mapResource(names.nextUri("graph"));
                namedGraphs.add(newNamedGraph);
                return model.add(
                        randomGraph(),
                        newNamedGraph,
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(RdfSuite.GRAPH));
            }
        }
        
        private class NewIndividual implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(names.nextUri("individual")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(RdfSchema.RESOURCE));
            }
        }

        private class NewBlankIndividual implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapBlankNode(names.nextBlankNode("individual")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(RdfSchema.RESOURCE));
            }
        }

        private class NewClass implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(names.nextUri("class")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(RdfSchema.CLASS));
            }
        }

        private class NewProperty implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(dummy),
                        model.mapResource(names.nextUri("property")),
                        model.mapResource(dummy));
            }
        }

        private class NewMetaclass implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(names.nextUri("metaclass")),
                        model.mapResource(RdfSchema.SUBCLASSOF),
                        model.mapResource(RdfSchema.CLASS));
            }
        }

        private class NewMetaproperty implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(names.nextUri("metaproperty")),
                        model.mapResource(RdfSchema.SUBCLASSOF),
                        model.mapResource(Rdf.PROPERTY));
            }
        }

        private class NewStatement implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(names.nextUri("statement")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(Rdf.STATEMENT));
            }
        }

        private class NewBlankStatement implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapBlankNode(names.nextBlankNode("statement")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(Rdf.STATEMENT));
            }
        }

        private class NewAlt implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(names.nextUri("alt")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(Rdf.ALT));
            }
        }

        private class NewBlankAlt implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapBlankNode(names.nextBlankNode("alt")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(Rdf.ALT));
            }
        }

        private class NewBag implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(names.nextUri("bag")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(Rdf.BAG));
            }
        }

        private class NewBlankBag implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapBlankNode(names.nextBlankNode("bag")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(Rdf.BAG));
            }
        }

        private class NewSeq implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapResource(names.nextUri("seq")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(Rdf.SEQ));
            }
        }

        private class NewBlankSeq implements ModelAction {
            public Triple call() {
                return model.add(
                        randomGraph(),
                        model.mapBlankNode(names.nextBlankNode("seq")),
                        model.mapResource(Rdf.TYPE),
                        model.mapResource(Rdf.SEQ));
            }
        }

        private class ClassToMetaclass implements ModelAction {
            public Triple call() {
                List<RdfNode> classes = Lists.newArrayList(model.findNodes(RdfType.CLASS));
                if (classes.isEmpty()) {
                    return null;
                }
                RdfNode clazz = classes.get(random.nextInt(classes.size()));
                Uri uri = ((Resource)clazz).getUri();
                if (uri.hasEqualNamespace(Rdf.NAMESPACE) ||
                        uri.hasEqualNamespace(RdfSchema.NAMESPACE) ||
                        uri.hasEqualNamespace(RdfSuite.NAMESPACE)) {
                    return null;
                }
                return model.add(
                        randomGraph(),
                        (Resource)clazz,
                        model.mapResource(RdfSchema.SUBCLASSOF),
                        model.mapResource(RdfSchema.CLASS));
            }
        }

        private class PropertyToMetaproperty implements ModelAction {
            public Triple call() {
                List<RdfNode> properties = Lists.newArrayList(model.findNodes(RdfType.PROPERTY));
                if (properties.isEmpty()) {
                    return null;
                }
                RdfNode property = properties.get(random.nextInt(properties.size()));
                Uri uri = ((Resource)property).getUri();
                if (uri.getNamespace() == Rdf.NAMESPACE.getNamespace() ||
                        uri.getNamespace() == RdfSchema.NAMESPACE.getNamespace()) {
                    return null;
                }
                if (property.predicateTriples().iterator().hasNext()) {
                    return null; //or else it would case typing error
                }
                return model.add(
                        randomGraph(),
                        (Resource)property,
                        model.mapResource(RdfSchema.SUBCLASSOF),
                        model.mapResource(Rdf.PROPERTY));
            }
        }
    }

    interface ModelAction {
        Triple call();
    }
}
class UniqueNames {
    private int uriCounter = 0;
    private int literalCounter = 0;
    private int blankNodeCounter = 0;

    Uri nextUri(String prefix) {
        return Uri.parse("http://ns#" + prefix + "" + (uriCounter++));
    }

    Literal nextLiteral() {
        return Literal.create("\"literal" + (literalCounter++) + "\")");
    }

    String nextBlankNode(String prefix) {
        return "bNode" + prefix + "" + (blankNodeCounter++);
    }
}