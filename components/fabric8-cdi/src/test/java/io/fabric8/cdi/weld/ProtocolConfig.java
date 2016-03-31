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
package io.fabric8.cdi.weld;

import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.inject.Inject;

public class ProtocolConfig {
    
    @Inject
    @ConfigProperty(name = "SOURCE_PROTOCOL")
    private String sourceProtocol;

    @Inject
    @ConfigProperty(name = "TARGET_PROTOCOL")
    private String targetProtocol;

    public String getSourceProtocol() {
        return sourceProtocol;
    }

    public String getTargetProtocol() {
        return targetProtocol;
    }
}