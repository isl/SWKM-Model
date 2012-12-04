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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.RdfNode;
import gr.forth.ics.swkm.model2.RdfType;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.Triple;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.swkm.model2.event.RdfNodeListener;
import gr.forth.ics.swkm.model2.event.TripleListener;
import gr.forth.ics.swkm.model2.Transitively;
import gr.forth.ics.swkm.model2.event.TypeChange;
import java.util.Map;
import java.util.Set;

/**
 * An index capable of tracking namespace dependencies on a {@link Model}.
 *
 * <p>A namespace dependency is defined as follows:<BR>
 * Namespace {@code nsA} depends on {@code nsB} iff all of the following holds:
 * <ul>
 * <li>There exists a triple of the form: {@code <nsA#x, p, nsB#y>}, for arbitrary local names {@code x} and {@code y}
 * <li>{@code nsA#x} and {@code nsB#y} have a <em>schema</em> type (see {@linkplain RdfType#isSchema()}
 * <li>{@code nsA != nsB}, i.e. self dependencies are ignored
 * </ul>
 *
 * By default, a NamespaceDependenciesIndex instance is <em>attached</em> (via listeners) to a given {@code Model}
 * and it tracks modifications to it so it is always kept consistent with regards to the model. In this mode, it can't be garbage
 * collected if the model itself is not garbage collected. If this is not desirable, the NamespaceDependenciesIndex can be explicitly
 * detached from the model, and remains immutable from that point on, regardless of modifications to the model,
 * and it can also be garbage-collected before the model does.
 *
 * <p>All methods, unless otherwise specified, throw {@code NullPointerException} in case of {@code null}
 * arguments.
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class NamespaceDependenciesIndex {
    private final Model model;
    private MyListener listener;

    private enum Direction {
        FORWARD, BACKWARD;
    }

    private final Map<Uri, Multiset<Uri>> forwardDeps = Maps.newHashMap();
    private final Map<Uri, Multiset<Uri>> backwardDeps = Maps.newHashMap();

    private final Map<Direction, Function<Uri, Iterable<Uri>>> exploringFunctions = Maps.newEnumMap(Direction.class);
    {
        class ExploringFunction implements Function<Uri, Iterable<Uri>> {
            private final Map<Uri, Multiset<Uri>> deps;

            ExploringFunction(Map<Uri, Multiset<Uri>> deps) {
                this.deps = deps;
            }

            public Iterable<Uri> apply(Uri uri) {
                return deps.get(uri);
            }
        }
        exploringFunctions.put(Direction.FORWARD, new ExploringFunction(forwardDeps));
        exploringFunctions.put(Direction.BACKWARD, new ExploringFunction(backwardDeps));
    }

    /**
     * Creates a NamespaceDependenciesIndex and attaches it to the specified model. It also reads the existing triples from the model
     * to initialize its state.
     *
     * <p>The created NamespaceDependenciesIndex will track modifications to the specified model and update itself, unless
     * {@linkplain #dettach()} is called.
     * 
     * @param model the model for which to create a NamespaceDependenciesIndex, must be non-null
     */
    public NamespaceDependenciesIndex(Model model) {
        this.model = Preconditions.checkNotNull(model);
        this.listener = new MyListener();
        model.addRdfNodeListener(listener);
        model.addTripleListener(listener);
        for (Triple t : model.triples().fetch()) {
            tripleAdded(t);
        }
    }

    /**
     * Dettaches this NamespaceDependenciesIndex from its model, that is, it will stop updating its state in accordance to modifications to the model.
     * This operation is a no-op if this NamespaceDependenciesIndex is already dettached.
     */
    public void dettach() {
        if (listener != null) {
            model.removeRdfNodeListener(listener);
            model.removeTripleListener(listener);
            listener = null;
        }
    }

    /**
     * Returns whether this NamespaceDependenciesIndex has been dettached from its model.
     *
     * @return whether this NamespaceDependenciesIndex has been dettached from its model
     * @see #dettach()
     */
    public boolean isDettached() {
        return listener == null;
    }

    /**
     * Finds the namespaces that the specified namespace depends on, transitively or not. See the {@link NamespaceDependenciesIndex
     * class javadoc} for the definition of a dependency.
     *
     * @param namespace the namespace for which to find the namespaces that it depends on
     * @param transitively if {@code Transitively.NO}, then only the direct dependencies are
     * returned, else if {@code Transitively.YES} direct and indirect dependencies are returned
     * @return the namespaces that the specified namespace depend on, transitively or not
     * @see NamespaceDependenciesIndex for the definition of a namespace dependency
     */
    public Set<Uri> getDependencies(Uri namespace, Transitively transitively) {
        return transitively.collect(namespace, exploringFunctions.get(Direction.FORWARD));
    }

    /**
     * Finds the namespaces that depend on a specific namespace, transitively or not. This is
     * the inverse relation of {@link #getDependencies(Uri, Transitively)}.
     * See the {@link NamespaceDependenciesIndex
     * class javadoc} for the definition of a dependency.
     *
     * @param namespace the namespace for which to find the namespaces that depend on it
     * @param transitively if {@code Transitively.NO}, then only the direct dependants are
     * returned, else if {@code Transitively.YES} direct and indirect dependants are returned
     * @return the namespaces that depend on the specified namespace, transitively or not
     * @see NamespaceDependenciesIndex for the definition of a namespace dependency
     */
    public Set<Uri> getDependants(Uri namespace, Transitively transitively) {
        return transitively.collect(namespace, exploringFunctions.get(Direction.BACKWARD));
    }

    private Map<Uri, Multiset<Uri>> deps(Direction direction) {
        switch (direction) {
            case FORWARD: return forwardDeps;
            case BACKWARD: return backwardDeps;
            default: throw new AssertionError();
        }
    }

    private Multiset<Uri> depsPerNamespace(Map<Uri, Multiset<Uri>> deps, Uri namespace) {
        Multiset<Uri> value = deps.get(namespace);
        if (value == null) {

            deps.put(namespace, value = HashMultiset.create());
        }
        return value;
    }

    private void tripleAdded(Triple triple) {
        if (triple.subject().type().isSchema() && triple.object().type().isSchema()) {
            addDependency(
                    ((Resource)triple.subject()).getUri().getNamespaceUri(),
                    ((Resource)triple.object()).getUri().getNamespaceUri());
        }
    }

    private void tripleDeleted(Triple triple) {
        if (triple.subject().type().isSchema() && triple.object().type().isSchema()) {
            removeDependency(
                    ((Resource)triple.subject()).getUri().getNamespaceUri(),
                    ((Resource)triple.object()).getUri().getNamespaceUri());
        }
    }

    private void addDependency(Uri from, Uri to) {
        if (from.equals(to)) {
            //ignore self-dependencies
            return;
        }
        addDependency(from, to, Direction.FORWARD);
        addDependency(to, from, Direction.BACKWARD);
    }

    private void addDependency(Uri uri1, Uri uri2, Direction direction) {
        Map<Uri, Multiset<Uri>> deps = deps(direction);
        Multiset<Uri> depsOfNamespace = depsPerNamespace(deps, uri1);
        depsOfNamespace.add(uri2);
    }

    private void removeDependency(Uri from, Uri to) {
        removeDependency(from, to, Direction.FORWARD);
        removeDependency(to, from, Direction.BACKWARD);
    }

    private void removeDependency(Uri uri1, Uri uri2, Direction direction) {
        Map<Uri, Multiset<Uri>> deps = deps(direction);
        Multiset<Uri> depsOfNamespace = depsPerNamespace(deps, uri1);
        depsOfNamespace.remove(uri2);

        if (depsOfNamespace.isEmpty()) {
            deps.remove(uri1);
        }
    }
        
    private class MyListener implements RdfNodeListener, TripleListener {
        public void onNodeAddition(RdfNode node) { /* ignore */ }

        public void onNodeDeletion(RdfNode node) { /* ignore */ }

        public void onTypeChange(TypeChange change) {
            RdfNode node = change.node();
            if (node.type().isSchema() && !change.oldType().isSchema()) {
                Resource resource = (Resource)node; //only a Resource can be schema
                for (Triple t : model.triples().s(resource).fetch()) {
                    tripleAdded(t);
                }
                for (Triple t : model.triples().o(resource).fetch()) {
                    tripleAdded(t);
                }
            }
        }

        public void onTripleAddition(Resource namedGraph, Triple triple) {
            if (triple.graphs().size() == 1) {
                tripleAdded(triple);
            }
        }

        public void onTripleDeletion(Resource namedGraph, Triple triple) {
            if (triple.graphs().isEmpty()) {
                tripleDeleted(triple);
            }
        }
    }

    /**
     * {@inheritDoc }
     * @return {@inheritDoc }
     */
    @Override
    public String toString() {
        final Map<Uri, Multiset<Uri>> deps = deps(Direction.FORWARD);
        return "{" + Joiner.on(",").join(Iterables.transform(deps.keySet(), new Function<Uri, String>() {
            public String apply(final Uri uri) {
                return Joiner.on(",").join(Iterables.transform(deps.get(uri).elementSet(), new Function<Uri, String>() {
                    public String apply(Uri uri2) {
                        return uri + "<->" + uri2;
                    }
                }));
            }
        })) + "}";
    }
}
