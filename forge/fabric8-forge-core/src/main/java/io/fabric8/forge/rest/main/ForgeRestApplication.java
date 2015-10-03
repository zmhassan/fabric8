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
package io.fabric8.forge.rest.main;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.fabric8.forge.rest.RootResource;
import org.apache.cxf.feature.LoggingFeature;
import io.fabric8.forge.rest.CommandsResource;
import io.fabric8.forge.rest.RepositoriesResource;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class ForgeRestApplication extends Application {
    @Inject
    ForgeInitialiser forgeInitialiser;

    @Inject
    RootResource rootResource;

    @Inject
    CommandsResource commandsResource;

    @Inject
    RepositoriesResource repositoriesResource;

    private boolean preloaded = false;

    @Override
    public Set<Object> getSingletons() {
        if (!preloaded) {
            preloaded = true;
            forgeInitialiser.preloadCommands(commandsResource);
        }

        return new HashSet<Object>(
                Arrays.asList(
                        rootResource,
                        commandsResource,
                        repositoriesResource,
                        new JacksonJsonProvider(),
/*
                        new SwaggerFeature(),
                        new EnableJMXFeature(),
*/
                        new LoggingFeature()
                )
        );
    }
}
