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


package gr.forth.ics.swkm.model2;

import com.google.common.base.Preconditions;
import gr.forth.ics.swkm.model2.ModelBuilder.DefaultUriSettings;
import gr.forth.ics.swkm.model2.ModelBuilder.UriValidationSettings;
import gr.forth.ics.swkm.model2.index.ModelIndexer;
import gr.forth.ics.swkm.model2.index.ModelIndexers;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;

/**
 * A builder of a model instance offering various configuration options.
 *
 * <p>Use the static factories to get a ModelBuilder, then optionally configure other
 * attributes (they have reasonable defaults) and call {@link #build()} to obtain
 * a reference to a new {@link Model} instance.</p>
 *
 * All available options are the following:
 * <pre>
 * ModelBuilder.
 *      [newSparse(). | newFull(). | newTrees().]
 *      [withTypeInference(). | withoutTypeInference().]?   //Default value: with
 *      [withUriValidation(). | withoutUriValidation().]?   //Default value: with
 *      [withDefaultNamedGraphUri("someUri").]?             //Default value: RdfSuite.DEFAULT_GRAPH_URI
 *      build();
 *</pre>
 * <p>Except for the above three index implementations there is also another one called
 * Horizontal. This one does not allow typing inference. All other options are available
 * for this implementation, too.</p>
 * 
 *<p>This is a very simple example of creating a default sparse model:
 *{@code
 *<pre>
 *Model model = ModelBuilder.newSparse().build();
 *</pre>}
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 * @author Vouzoukidou Nelly, email: vuzukid (at) csd (dot) uoc (dot) gr
 */
public class ModelBuilder {

    /**
     * Creates a builder that will build a {@link Model} efficient for sparse RDF graphs (i.e., the
     * vast majority of RDF graphs).
     *
     * @return a builder that will build a {@link Model} efficient for sparse RDF graphs (i.e., the
     * vast majority of RDF graphs)
     */
    public static TypeInferenceSettings newSparse() {
        return new ModelBuilderImpl(ModelIndexers.createNodeListsModelIndexer());
    }

    /**
     * Creates a builder that will build a {@link Model} with full storing of triples.
     *
     * <p>It has high memory requirements, but is very efficient in answering triple
     * queries.</p>
     *
     * @return a builder that will build a {@link Model} with full storing of triples
     */
    public static TypeInferenceSettings newFull() {
        return new ModelBuilderImpl(ModelIndexers.createMultimapsModelIndexer());
    }

    /**
     * Creates a builder that will build a new {@link Model}.
     * @return a builder that will build a new {@link Model}
     */
    public static TypeInferenceSettings newTrees() {
        return new ModelBuilderImpl(ModelIndexers.createTreeMapModelIndexer());
    }

    /**
     * Creates a builder that will build a {@link Model} with minimal memory footprint
     * but very slow in triple queries.
     *
     * <p>Use this type of storage only in case when a bag of triples only needs
     * to be stored. (For instance when a transorfmation from one type of rdf document
     * to another is needed).</p>
     *
     * <p>Since type inference of nodes depends on triples queries, use of this
     * feature would require too much time and thus, is not allowed.</p>
     *
     * @return a builder that will build a {@link Model} with minimal memory footprint
     * but very slow in triple queries
     */
    public static DefaultUriSettings newHorizontal() {
        return new ModelBuilderHorizontal(ModelIndexers.createHorizontalModelIndexer());
    }

    /**
     * An assisting interface for {@link ModelBuilder}, allowing definition of whether
     * type inferncing will be used or not.
     */
    public interface TypeInferenceSettings extends UriValidationSettings {

        /**
         * Declares that the model to be created will inference types of the rdf nodes each time
         * a triple is added in the model.
         * @return an object to handle the rest of the procedure of the model creation
         */
        UriValidationSettings withTypeInference();

        /**
         * Declares that the model to be created will not inference types of the rdf nodes
         * while triples are added to the model.
         *
         * <p>Namespace dependencies will not be found without inference of nodes' types.</p>
         *
         * <p>Method {@linkplain Model#retypeNodes()} can still be called at any time
         * in the model and will have the same effect, as it would have if typing inference
         * was used. However, any triples added after the call of method {@linkplain Model#retypeNodes()}
         * will not update the types of the affected triples.</p>
         *
         * @return an object to handle the rest of the procedure of the model creation
         * @see {@linkplain Model#retypeNodes()}
         */
        UriValidationSettings withoutTypeInference();
    }

    public interface UriValidationSettings extends DefaultUriSettings {

        /**
         * Declares that the model to be created will validate URIs given as input according to
         * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC&nbsp;2396</a>.
         * This is the default option.
         *
         * @return an object to handle the rest of the procedure of the model creation
         */
        DefaultUriSettings withUriValidation();

        /**
         * Declares that the model to be created will not validate URIs. This does not
         * turn off validation during parsing, but only validation during creation of the model
         * through the API (methods Model.mapResource()).
         *
         * @return an object to handle the rest of the procedure of the model creation
         */
        DefaultUriSettings withoutUriValidation();
    }

    /**
     * An assisting interface for {@link ModelBuilder}, allowing definition of the
     * default uri of the named graph triples will be put in.
     */
    public interface DefaultUriSettings extends Builder {

        /**
         * Defines the URI of the default named graph of the to-be-built {@link Model} instance.
         * The default named graph can later be queried by {@linkplain Model#defaultNamedGraph()} method.
         *
         * <p>Triples that are added without specifying any named graph, are added to the default named graph.
         *
         * @param defaultUri the URI of the default named graph; must be non-null
         * @return an object to handle the rest of the procedure of the model creation
         */
        Builder withDefaultNamedGraphUri(Uri defaultUri);
    }

    /**
     * An assisting interface for {@link ModelBuilder}, offering the final method of the procedure of
     * creating a new {@link Model}.
     */
    public interface Builder {

        /**
         * Builds a {@link Model} instance, using the settings specified on this builder instance.
         *
         * @return a {@link Model} instance, using the settings specified on this builder instance.
         */
        public Model build();
    }
}

class ModelBuilderImpl extends ModelBuilderHorizontal implements ModelBuilder.TypeInferenceSettings {

    private ModelImpl.TypeInferenceStrategy typeInferenceStrategy;

    ModelBuilderImpl(ModelIndexer index) {
        super(index);
        this.typeInferenceStrategy = ModelImpl.TypeInferenceStrategy.WITH_TYPING;
    }


    public ModelBuilder.UriValidationSettings withTypeInference() {
        typeInferenceStrategy = ModelImpl.TypeInferenceStrategy.WITH_TYPING;
        return this;
    }

    public ModelBuilder.UriValidationSettings withoutTypeInference() {
        typeInferenceStrategy = ModelImpl.TypeInferenceStrategy.NO_TYPING;
        return this;
    }

    @Override
    public Model build() {
        return new ModelImpl(index, typeInferenceStrategy, uriValidationStrategy, defaultUri);
    }
}

class ModelBuilderHorizontal implements ModelBuilder.DefaultUriSettings, ModelBuilder.Builder {

    protected final ModelIndexer index;
    protected Uri defaultUri = RdfSuite.DEFAULT_GRAPH_URI;
    protected ModelImpl.UriValidationStrategy uriValidationStrategy;

    ModelBuilderHorizontal(ModelIndexer index) {
        this.index = index;
        this.uriValidationStrategy = ModelImpl.UriValidationStrategy.WITH_VALIDATION;
    }

    public ModelBuilder.Builder withDefaultNamedGraphUri(Uri defaultUri) {
        this.defaultUri = Preconditions.checkNotNull(defaultUri);
        return this;
    }

    public DefaultUriSettings withUriValidation() {
        uriValidationStrategy = ModelImpl.UriValidationStrategy.WITH_VALIDATION;
        return this;
    }

    public DefaultUriSettings withoutUriValidation() {
        uriValidationStrategy = ModelImpl.UriValidationStrategy.WITHOUT_VALIDATION;
        return this;
    }

    public Model build() {
        return new ModelImpl(index, ModelImpl.TypeInferenceStrategy.NO_TYPING, uriValidationStrategy, defaultUri);
    }
}
