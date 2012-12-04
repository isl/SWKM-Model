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


package gr.forth.ics.swkm.model2.diff;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gr.forth.ics.swkm.model2.Uri;
import gr.forth.ics.rdfsuite.services.VersionedUri;
import gr.forth.ics.swkm.model2.Model;
import gr.forth.ics.swkm.model2.ModelBuilder;
import gr.forth.ics.swkm.model2.Resource;
import gr.forth.ics.swkm.model2.io.Format;
import gr.forth.ics.swkm.model2.io.RdfIO;
import gr.forth.ics.swkm.model2.vocabulary.Rdf;
import gr.forth.ics.swkm.model2.vocabulary.RdfSchema;
import gr.forth.ics.swkm.model2.vocabulary.RdfSuite;
import gr.forth.ics.swkm.model2.vocabulary.Xml;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Papavasileiou Vicky
 */
class NamespaceMapping {

    private BiMap<String,String> namespaceMap;

    public NamespaceMapping() {
        this.namespaceMap = HashBiMap.create();
        addDefaults(namespaceMap);
    }

    public NamespaceMapping(BiMap<String, String> namespaceMap) {
        this.namespaceMap = namespaceMap;
    }

    public BiMap<String, String> getNamespaceMap() {
        return namespaceMap;
    }

    public void populateMapping(Model model1, Model model2){
        Iterator<Uri> n1 = model1.namespaces().iterator();
        while(n1.hasNext()){ //the namespace URIs of model 1
            Uri next1 = n1.next();
            if(next1.getNamespaceUri().equals(RdfSchema.NAMESPACE.getNamespaceUri()) ||
                    next1.getNamespaceUri().equals(Rdf.NAMESPACE.getNamespaceUri()) ||
                    next1.getNamespaceUri().equals(Xml.NAMESPACE.getNamespaceUri()) ||
                    next1.getNamespaceUri().equals(RdfSuite.NAMESPACE.getNamespaceUri())){
                continue;
            }
            VersionedUri  vURI1=VersionedUri.parse(next1.getNamespace());
            String plain1=vURI1.stripVersion().getFullUri(); //the first uri without the version
            Iterator<Uri> n2 = model2.namespaces().iterator();
            while(n2.hasNext()){ //the namespace URIs of model 2
                Uri next2 = n2.next();
                if(next2.getNamespaceUri().equals(RdfSchema.NAMESPACE.getNamespaceUri()) ||
                        next2.getNamespaceUri().equals(Rdf.NAMESPACE.getNamespaceUri()) ||
                        next2.getNamespaceUri().equals(Xml.NAMESPACE.getNamespaceUri()) ||
                        next2.getNamespaceUri().equals(RdfSuite.NAMESPACE.getNamespaceUri())){
                    continue;
                }
                if(next1.toString().endsWith("_data#") == next2.toString().endsWith("_data#")){
                    VersionedUri  vURI2=VersionedUri.parse(next2.getNamespace());
                    String plain2=vURI2.stripVersion().getFullUri(); //the second uri without the version
                        if(plain1.equals(plain2)){ //if the two URIs are the same except the version
                            this.namespaceMap.forcePut(next1.toString(), next2.toString());
                            break; //only 1-1 mapping is permited
                        }else{
                        //check if the "namespace part" until the last '/' match
                            String ns1 = next1.getNamespace();
                            String ns2 = next2.getNamespace();
                            if(ns1.equals(ns2)){
                                this.namespaceMap.forcePut(Uri.parse(ns1).toString(),Uri.parse(ns2).toString());
                            }
                        }
                }
            }
        }
        Iterator<Resource> g1= model1.namedGraphs().iterator(); //the Graphspaces of the first model
        while(g1.hasNext()){
            Resource next1 = g1.next();
            VersionedUri  vURI1 = VersionedUri.parse(next1.getUri().getNamespace());
            String plain1=vURI1.stripVersion().getFullUri(); //the first uri without the version
            Iterator<Resource> g2 = model2.namedGraphs().iterator();
            while(g2.hasNext()){
                Resource next2 = g2.next();
                VersionedUri  vURI2=VersionedUri.parse(next2.getUri().getNamespace());
                String plain2=vURI2.stripVersion().getFullUri(); // the second uri without the version
                if(plain1.equals(plain2)){
                    this.namespaceMap.put(next1.getUri().getNamespaceUri().toString(),
                            next2.getUri().getNamespaceUri().toString());
                    break;
                }
            }
        }
    }



    private void addDefaults(Map<String,String> map){
        map.put(RdfSchema.NAMESPACE.toString(), RdfSchema.NAMESPACE.toString());
        map.put(Rdf.NAMESPACE.toString(), Rdf.NAMESPACE.toString());
        map.put(Xml.NAMESPACE.toString(), Xml.NAMESPACE.toString());
        map.put(RdfSuite.NAMESPACE.toString(), RdfSuite.NAMESPACE.toString());
    }


    public static void main(String[] args) throws FileNotFoundException{
        Model model1 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("test/gr/forth/ics/rdfsuite/diff2/data/Namespaces1.trig"), Format.TRIG).
                withBase("http://test~_~v1#");

        Model model2 = ModelBuilder.newSparse().build();
        RdfIO.read(new File("test/gr/forth/ics/rdfsuite/diff2/data/Namespaces2.trig"), Format.TRIG).
                withBase("http://test~_~v2#");

        NamespaceMapping map = new NamespaceMapping();
        map.populateMapping(model1, model2);
        System.out.println(map.getNamespaceMap());
    }

}
