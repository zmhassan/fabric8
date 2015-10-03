/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.forge.camel.commands.project.archetype;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.jboss.forge.addon.maven.archetype.ArchetypeCatalogFactory;

/**
 * The Apache Camel archetypes
 */
public class CamelArchetypeCatalogFactory implements ArchetypeCatalogFactory {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private static final String NAME = "camel";

    private ArchetypeCatalog cachedArchetypes;

    public String toString() {
        return NAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ArchetypeCatalog getArchetypeCatalog() {
        if (cachedArchetypes == null) {
            // use the camel catalog to load the archetypes
            String xml = new DefaultCamelCatalog().archetypeCatalogAsXml();
            if (xml != null) {
                try {
                    cachedArchetypes = new ArchetypeCatalogXpp3Reader().read(new StringReader(xml));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error while retrieving archetypes", e);
                }
            }
        }
        return cachedArchetypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CamelArchetypeCatalogFactory that = (CamelArchetypeCatalogFactory) o;

        if (!NAME.equals(that.NAME)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return NAME.hashCode();
    }
}
