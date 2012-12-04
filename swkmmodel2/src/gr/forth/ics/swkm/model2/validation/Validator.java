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


package gr.forth.ics.swkm.model2.validation;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gr.forth.ics.graph.Graph;
import gr.forth.ics.graph.path.Cycles;
import gr.forth.ics.graph.path.Path;
import gr.forth.ics.swkm.model2.GraphUtils;
import gr.forth.ics.swkm.model2.Literal;
import gr.forth.ics.swkm.model2.LiteralNode;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ObjectNode;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.views.Inheritable;
import gr.forth.ics.swkm.model2.Transitively;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.XmlSchema;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A validator has the task of verifying the validity of a {@link Model}. This class is abstract
 * since there are various notions of what constitutes validity, or what rules are checked.
 * 
 * <p>{@linkplain #defaultValidator()} creates a default validator (see the method specification
 * for details).
 * 
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public abstract class Validator {
    private Validator() { }
    
    /**
     * Performs validation checking against the specified model, and reports found errors
     * and warnings to the specified validation handler.
     * 
     * @param model the model to check its validity
     * @param handler the handler that will handle reported errors and warnings
     */
    public abstract void validate(Model model, ValidationHandler handler);
    
    /**
     * Performs validation checking against the specified model, and throws a
     * ValidationException if any error (but not warning) is found during the process.
     * 
     * @param model the model to check its validity
     * @throws ValidationException if any error is found during validation
     */
    public void validateAndFailOnFirstError(Model model) throws ValidationException {
        validate(model, new ValidationHandler() {
            public void handleError(ValidationProblem error) {
                throw new ValidationException(Collections.singleton(error), Collections.<ValidationProblem>emptySet());
            }

            public void handleWarning(ValidationProblem warning) { /* ignore warnings */ }
        });
    }

    public void validateAndFailOnFirstProblem(Model model) throws ValidationException {
        validate(model, new ValidationHandler() {
            public void handleError(ValidationProblem error) {
                throw new ValidationException(Collections.singleton(error), Collections.<ValidationProblem>emptySet());
            }

            public void handleWarning(ValidationProblem warning) {
                throw new ValidationException(Collections.<ValidationProblem>emptySet(), Collections.singleton(warning));
            }
        });
    }
    
    /**
     * Returns a default validator instance. Performs these validation checks:
     * <ul>
     * <li>{@linkplain #checkPropertiesHaveOneDomainAndRange(Model, ValidationHandler, boolean) 
     *    Properties must have one domain and one range, without use of inference}
     * <li>{@linkplain #checkNoIllegalDomains(Model, ValidationHandler)
     *      There are no illegal domains}
     * <li>{@linkplain #checkNoCycleInSubClassOf(Model, ValidationHandler)
     *      There is no cycle in the rdfs:subClassOf relation}
     * <li>{@linkplain #checkNoCycleInSubPropertyOf(Model, ValidationHandler)
     *      There is no cycle in the rdfs:subClassOf relation}
     * <li>{@linkplain #checkSubPropertiesHaveCompatibleDomainsAndRanges(Model, ValidationHandler)
     *      Each property has compatible domain and range with its super-properties}
     * <li>{@linkplain #checkPropertyInstancesHaveCompatibleTypes(Model, ValidationHandler)
     *      Each property instance has compatible subject and object with the domain and range, respectively,
     *      of the property}
     * </ul>
     * 
     * @return a default Validator instance
     */
    public static Validator defaultValidator() {
        return new DefaultValidator();
    }

    /**
     * Returns whether the specified resource belongs to {@linkplain Rdf#NAMESPACE RDF
     * namespace} or to {@linkplain RdfSchema#NAMESPACE RDF Schema namespace}
     * or to {@linkplain XmlSchema#NAMESPACE XML Schema namespace}
     *
     * @param resource the resource to check whether it belongs to the default namespaces
     * @return whether the specified resource belongs to RDF namespace or to
     * RDF Schema namespace XML Schema namespace
     */
    protected static boolean inDefaultNamespaces(Resource resource) {
        Uri uri = resource.getUri();
        return uri.hasEqualNamespace(Rdf.NAMESPACE)
                || uri.hasEqualNamespace(RdfSchema.NAMESPACE)
                || uri.hasEqualNamespace(XmlSchema.NAMESPACE);
    }

    /**
     * Checks that there is no triple of the form: {@code <MC rdf:type X>} where {@code MC} is of a metaclass, and X is <em>not</em>
     * {@code RdfSchema.CLASS}.
     *
     * @param model the model to validate
     * @param handler the handler that will receive a warning if this method's check is not satisfied
     */
    protected static void checkMetaclassesCannotBeTypesOfMetaclasses(Model model, ValidationHandler handler) {
        Resource rdfType = model.mapResource(Rdf.TYPE);
        for (RdfNode metaclass : model.findNodes(RdfType.METACLASS)) {
            for (Triple t : model.triples().s((Resource)metaclass).p(rdfType).fetch()) {
                if (!t.object().is(RdfSchema.CLASS)) {
                    ErrorCode.illegalTriple(t, "A metaclass can only be rdf:type of rdfs:Class, and of nothing else").handledBy(handler);
                }
            }
        }
    }

    /**
     * Checks that every property has a single domain and a single range, or in case either is missing, it inherits
     * a single property which does have the missing element (recursively).
     * 
     * <p>If a property has no domain/range, it inherits (by adding the necessary triples to the model,
     * in the {@linkplain Model#defaultNamedGraph() default} named graph) the domains/ranges of their
     * ancestors. If no domain or range is
     * found, {@linkplain RdfSchema#RESOURCE rdfs:Resource} is used, and warnings of {@linkplain ErrorCode#NO_DOMAIN}
     * and {@linkplain ErrorCode#NO_RANGE} are issued.
     * If a property and its ancestors have no domain or range, warnings are issued with {@linkplain ErrorCode#NO_DOMAIN}
     * and {@linkplain ErrorCode#NO_RANGE} codes respectively, and {@linkplain RdfSchema#RESOURCE rdfs:Resource} is
     * supplemented as the domain or range of the property.
     * 
     * <p>If this invocation does not report any validation errors, it is guaranteed that properties
     * will have unique domains and ranges afterwards.
     * 
     * <p>If a property has multiple domains or ranges, then errors are issued with
     * {@linkplain ErrorCode#NON_UNIQUE_DOMAIN}
     * and {@linkplain ErrorCode#NON_UNIQUE_RANGE} respectively.
     * </ul>
     * 
     * @param model the model to validate
     * @param handler the handler that will receive a warning if this method's check is not satisfied
     */
    protected static void checkPropertiesHaveOneDomainAndRange(Model model, ValidationHandler handler) {
        Set<Resource> alreadyValidated = Sets.newHashSet();
        for (RdfNode node : model.findNodes(RdfType.PROPERTY)) {
            Resource property = (Resource)node;
            if (inDefaultNamespaces(property)) {
                continue;
            }
            checkPropertiesHaveOneDomainAndRange(model, handler, property, alreadyValidated);
        }
    }
    
    private static void checkPropertiesHaveOneDomainAndRange(Model model, ValidationHandler handler,
            Resource property, Set<Resource> alreadyValidated) {
        alreadyValidated.add(property);
        for (Uri uri : new Uri[] { RdfSchema.DOMAIN, RdfSchema.RANGE }) {
            Resource mappedUri = model.mapResource(uri);
            
            Iterable<RdfNode> domainsOrRanges = model.triples().s(property).p(mappedUri)
                    .fetch().objects();
            int size = Iterables.size(domainsOrRanges);
            if (size == 0) {
                //collect all domains or ranges from direct ancestors
                Collection<RdfNode> parentDomainsOrRanges = Lists.newArrayList();
                for (Inheritable parentNode : property.asInheritable().ancestors(Transitively.NO)) {
                    Resource parent = (Resource)parentNode;
                    if (!alreadyValidated.contains(parent)) {
                        //recurse to make sure we read the correct domains and ranges of the parent
                        //this is only guaranteed if there is no cycle
                        checkPropertiesHaveOneDomainAndRange(model, handler, parent, alreadyValidated);
                    }
                    
                    Iterables.addAll(parentDomainsOrRanges, 
                            model.triples().s(parent).p(uri)
                                .fetch().objects());
                }
                //inherit domains or ranges in property
                for (RdfNode domainOrRange : parentDomainsOrRanges) {
                    model.add(null, property, mappedUri, domainOrRange);
                }
                
                //recalculate domain or ranges (will include the inherited ones)
                domainsOrRanges = model.triples().s(property).p(mappedUri)
                        .fetch().objects();
                size = Iterables.size(domainsOrRanges);
            }
            
            if (size == 1) { //ok
                continue;
            } else if (size == 0) { //defaults to Resource
                if (property.is(Rdf.PROPERTY)) {
                    continue;
                }
                model.add(null, property, mappedUri, model.mapResource(RdfSchema.RESOURCE));
                if (uri == RdfSchema.DOMAIN) {
                    ErrorCode.noDomain(property).handledBy(handler);
                } else {
                    ErrorCode.noRange(property).handledBy(handler);
                }
            } else {
                //were more than one. report error
                //if Model#leastCommonAncestor were implemented, a try should be attempted
                //because maybe all the ancestors have a common ancestor, which would
                //be nice to be used.

                //if more than one, report an error, and don't inherit
                ValidationProblem p;
                if (uri == RdfSchema.DOMAIN) {
                    p = ErrorCode.nonUniquePropertyDomain(property, domainsOrRanges);
                } else {
                    p = ErrorCode.nonUniquePropertyRange(property, domainsOrRanges);
                }
                p.handledBy(handler);
                continue;
            }
        }
    }
    
    
    /**
     * Checks that there is no {@linkplain XmlSchema XML type} or {@linkplain RdfSchema#LITERAL} is defined as the domain of a property.
     * 
     * <p>If an illegal domain is found, an error is issued with {@linkplain ErrorCode#ILLEGAL_DOMAIN} code.
     *
     * @param model the model to validate
     * @param handler the handler that will receive a warning if this method's check is not satisfied
     */
    protected void checkNoIllegalDomains(Model model, ValidationHandler handler) {
        for (RdfNode node : model.findNodes(RdfType.PROPERTY, RdfType.METAPROPERTY)) {
            Resource property = (Resource)node;
            Iterable<RdfNode> domains = model.triples().s(property).p(RdfSchema.DOMAIN)
                    .fetch().objects();
            for (RdfNode d : domains) {
                Resource domain = (Resource)d;
                if (domain.type().isXmlType() || domain.is(RdfSchema.LITERAL)) {
                    ErrorCode.illegalDomain(property, domain).handledBy(handler);
                }
            }
        }
    }

    /**
     * Checks that there is no cycle in the {@linkplain RdfSchema#SUBCLASSOF rdfs:subClassOf} relation.
     * 
     * <p>If such a cycle is found, an error is issued with {@linkplain ErrorCode#CYCLE_IN_SUBCLASSOF} code.
     * 
     * <p>Since some validation checks may depend on this hierarchy being acyclic, if this error is found, the rest of the validation
     * process may report redundant problems or not report real ones.
     *
     * @param model the model to validate
     * @param handler the handler that will receive a warning if this method's check is not satisfied
     */
    protected void checkNoCycleInSubClassOf(Model model, ValidationHandler handler) {
        Graph subClassOfGraph = GraphUtils.toGraph(model, RdfSchema.SUBCLASSOF).graph();
        Path cycle = Cycles.findCycle(subClassOfGraph);
        if (cycle != null) {
            ErrorCode.cycleInSubclassOf(cycle).handledBy(handler);
        }
    }
    
    /**
     * Checks that there is no cycle in the {@linkplain RdfSchema#SUBPROPERTYOF rdfs:subClassOf} relation.
     * 
     * <p>If such a cycle is found, an error is issued with {@linkplain ErrorCode#CYCLE_IN_SUBPROPERTYOF} code.
     * 
     * <p>Since some validation checks may depend on this hierarchy being acyclic, if this error is found, the rest of the validation
     * process may report redundant problems or not report real ones.
     *
     * @param model the model to validate
     * @param handler the handler that will receive a warning if this method's check is not satisfied
     */
    protected void checkNoCycleInSubPropertyOf(Model model, ValidationHandler handler) {
        Graph subClassOfGraph = GraphUtils.toGraph(model, RdfSchema.SUBPROPERTYOF).graph();
        Path cycle = Cycles.findCycle(subClassOfGraph);
        if (cycle != null) {
            ErrorCode.cycleInSubpropertyOf(cycle).handledBy(handler);
        }
    }
    
    /**
     * Checks that properties have compatible domains and ranges with their super-properties.
     * 
     * <p>If a property is found that has an incompatible domain or range with regards to a super-property of it,
     * an error is issued with {@linkplain ErrorCode#INCOMPATIBLE_DOMAIN} or 
     * {@linkplain ErrorCode#INCOMPATIBLE_RANGE} code respectively.
     * 
     * <p>Depends on properties having single domains and ranges.
     *
     * @param model the model to validate
     * @param handler the handler that will receive a warning if this method's check is not satisfied
     */
    protected void checkSubPropertiesHaveCompatibleDomainsAndRanges(Model model, ValidationHandler handler) {
        for (Triple subPropertyTriple : model.triples().p(RdfSchema.SUBPROPERTYOF).fetch()) {
            Resource subProperty = (Resource)subPropertyTriple.subject();
            Resource superProperty = (Resource)subPropertyTriple.object();
            
            {
                Resource subDomain = domainOf(subProperty);
                Resource superDomain = domainOf(superProperty);
                if (!subDomain.asInheritable().isDescendantOf(superDomain.asInheritable())) {
                    ErrorCode.incompatibleDomain(
                            subProperty, subDomain,
                            superProperty, superDomain).handledBy(handler);
                }
            }
            {
                Resource subRange = rangeOf(subProperty);
                Resource superRange = rangeOf(superProperty);
                if (!subRange.asInheritable().isDescendantOf(superRange.asInheritable())) {
                    ErrorCode.incompatibleRange(
                            subProperty, subRange,
                            superProperty, superRange).handledBy(handler);
                }
            }
        }
    }
    
    private static Resource domainOf(Resource property) {
        Model model = property.owner();
        Iterator<RdfNode> domains =
                model.triples().s(property).p(RdfSchema.DOMAIN).fetch().objects().iterator();
        if (!domains.hasNext()) {
            return null;
        }
        RdfNode domain = domains.next();
        return (Resource)domain;
    }
    
    private static Resource rangeOf(Resource property) {
        Model model = property.owner();
        Iterator<RdfNode> ranges =
                model.triples().s(property).p(RdfSchema.RANGE).fetch().objects().iterator();
        if (!ranges.hasNext()) {
            return null;
        }
        RdfNode range = ranges.next();
        return (Resource)range;
    }
    
    /**
     * Checks that property instances have subjects and objects that are {@linkplain Rdf#TYPE "type-of"} the actual
     * resources that the property definition declares as domain and range. It <em>ignores</em>
     * properties that are defined in {@code RDF(S), or XML Schema} namespaces.
     * 
     * <p>If the subject or an object of a property instance is not a type of the respective domain or range of the property,
     * then an error is issued with {@linkplain ErrorCode#WRONGLY_TYPED_NODE} code. There are two exceptions to
     * this rule:
     * <ul>
     * <li>if the object is a literal, then:
     *      <ul>
     *      <li>If the literal is {@linkplain Literal#hasType() typed}, then its {@linkplain Literal#getType() type} must exactly match
     * the range of the property, or else an error is issued with {@linkplain ErrorCode#WRONGLY_TYPED_LITERAL} code.
     *      <li>if the literal is untyped, then unless the range of the property is {@linkplain RdfSchema#LITERAL rdfs:Literal}
     * or {@link RdfType#isXmlType() an XML type}, then an error is issued with {@linkplain ErrorCode#WRONGLY_TYPED_LITERAL} code.
     *      </ul>
     * <li>if the domain or the range of the property if {@linkplain RdfSuite#CLASS rdfsuite:Class}, then exceptionally all metaclasses and
     * metaproperties are allowable as subject or object respectively. In other words, {@code rdfsuite:Class} denotes the union of metaclasses
     * and metaproperties.
     * </ul>
     * 
     * <p>This check depends on properties having single domains and ranges, and is only reliable when this condition holds.
     *
     * @param model the model to validate
     * @param handler the handler that will receive a warning if this method's check is not satisfied
     */
    protected static void checkPropertyInstancesHaveCompatibleTypes(Model model, ValidationHandler handler) {
        for (RdfNode n : model.findNodes(RdfType.PROPERTY)) {
            Resource property = (Resource)n;
            if (inDefaultNamespaces(property)) {
                continue;
            }
            Resource domainResource = domainOf(property);
            Resource rangeResource = rangeOf(property);
            if (!domainResource.type().isSchema() || !(rangeResource.type().isSchema() ||
                    rangeResource.type().isXmlType())) {
                //this is a more serious error, handled elsewhere
                continue;
            }
            Inheritable domain = domainResource.asInheritable();
            Inheritable range = rangeResource.asInheritable();
            
            final boolean domainIsRdfSuiteClass = domainResource.is(RdfSuite.CLASS);
            final boolean rangeIsRdfSuiteClass = rangeResource.is(RdfSuite.CLASS);
            
            for (Triple pi : model.triples().p(property).fetch()) {
                Resource rdfType = model.mapResource(Rdf.TYPE);
                do { //check that subject has at least one compatible type-of
                    ObjectNode subj = pi.subject();
                    if (domainIsRdfSuiteClass && EnumSet.of(RdfType.METACLASS,
                            RdfType.METAPROPERTY).contains(subj.type())) {
                        break; //RdfSuite#CLASS allows both metaclasses and metaproperties
                    }

                    //something might be a subclass/subproperty etc of something with the desired type
                    @SuppressWarnings("unchecked") //all inheritable are ObjectNodes
                    Iterable<ObjectNode> subjects = Iterables.concat(
                            Arrays.asList(subj),
                            !subj.type().isSchema() ? Collections.emptyList() :
                                (Iterable)subj.asInheritable().ancestors(Transitively.YES));

                    boolean found = false;
                    for (ObjectNode s : subjects) {
                        for (RdfNode type :  model.triples().s(s).p(rdfType).fetch().objects()) {
                            Inheritable inheritable = type.asInheritable();
                            if (domain.isAncestorOf(inheritable)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        ErrorCode.wronglyTypedSubject(pi, domainResource).handledBy(handler);
                    }
                } while (false); //this while is simply to allow an early break
                
                do { 
                    if (pi.object().isObjectNode()) {
                        //resource or blank node
                        ObjectNode obj = (ObjectNode)pi.object();
                        if (rangeIsRdfSuiteClass && EnumSet.of(RdfType.METACLASS,
                                RdfType.METAPROPERTY).contains(obj.type())) {
                            break; //RdfSuite#CLASS allows both metaclasses and metaproperties
                        }
                    //something might be a subclass/subproperty etc of something with the desired type
                    @SuppressWarnings("unchecked") //all inheritable are ObjectNodes
                    Iterable<ObjectNode> objects = Iterables.concat(
                            Arrays.asList(obj),
                            !obj.type().isSchema() ? Collections.emptyList() :
                                (Iterable)obj.asInheritable().ancestors(Transitively.YES));

                        boolean found = false;
                        for (ObjectNode o : objects) {
                            for (RdfNode type :  model.triples().s(o).p(rdfType).fetch().objects()) {
                                Inheritable inheritable = type.asInheritable();
                                if (range.isAncestorOf(inheritable)) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            ErrorCode.wronglyTypedObject(pi, rangeResource).handledBy(handler);
                        }
                    } else {
                        //literals
                        //check that properties with range rdfs:Literal have untyped literals
                        //and literals for other properties have type that matches (exactly; no inference in this implementation)
                        Literal literal = ((LiteralNode)pi.object()).getLiteral();
                        Uri literalType = literal.hasType() ? literal.getType() : RdfSchema.LITERAL;
                        if (!rangeResource.is(literalType) && !rangeResource.type().isXmlType()) {
                            ErrorCode.wronglyTypedLiteral(pi, rangeResource).handledBy(handler);
                        }
                    }
                } while (false); //this while is simple to allow an early break
            }
        }
    }

    /**
     *
     * @param model the model to validate
     * @param handler the handler that will receive a warning if this method's check is not satisfied
     */
    protected static void checkLiterals(Model model, ValidationHandler handler) {
        for (RdfNode node : model.findNodes(RdfType.LITERAL)) {
            Literal literal = ((LiteralNode)node).getLiteral();
            if (!literal.isValid()) {
                ErrorCode.illegalLiteral(literal).handledBy(handler);
            }
        }
    }
    
    /**
     * A default validator instance. Performs these validation checks:
     * <ul>
     * <li>{@linkplain #checkPropertiesHaveOneDomainAndRange(Model, ValidationHandler, boolean) 
     *    Properties must have one domain and one range, without use of inference}
     * <li>{@linkplain #checkNoIllegalDomains(Model, ValidationHandler)
     *      There are no illegal domains}
     * <li>{@linkplain #checkNoCycleInSubClassOf(Model, ValidationHandler)
     *      There is no cycle in the rdfs:subClassOf relation}
     * <li>{@linkplain #checkNoCycleInSubPropertyOf(Model, ValidationHandler)
     *      There is no cycle in the rdfs:subClassOf relation}
     * <li>{@linkplain #checkSubPropertiesHaveCompatibleDomainsAndRanges(Model, ValidationHandler)
     *      Each property has compatible domain and range with its super-properties}
     * <li>{@linkplain #checkPropertyInstancesHaveCompatibleTypes(Model, ValidationHandler)
     *      Each property instance has compatible subject and object with the domain and range, respectively,
     *      of the property}
     * <li>{@linkplain #checkLiterals(Model, ValidationHandler)
     *      Each literal conforms to the validity rules of its type (if it is typed)}
     * </ul>
     */
    protected static class DefaultValidator extends Validator {
        @Override
        public void validate(Model model, ValidationHandler handler) {
            checkMetaclassesCannotBeTypesOfMetaclasses(model, handler);
            checkPropertiesHaveOneDomainAndRange(model, handler);
            checkNoIllegalDomains(model, handler);
            checkNoCycleInSubClassOf(model, handler);
            checkNoCycleInSubPropertyOf(model, handler);
            checkSubPropertiesHaveCompatibleDomainsAndRanges(model, handler);
            checkPropertyInstancesHaveCompatibleTypes(model, handler);
            checkLiterals(model, handler);
        }
    }
}
