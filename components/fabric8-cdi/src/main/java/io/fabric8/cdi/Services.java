/*
 * Copyright 2005-2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.fabric8.cdi;


import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.utils.Strings;
import io.fabric8.utils.Systems;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static io.fabric8.cdi.KubernetesHolder.KUBERNETES;

public class Services {

    private static final String KUBERNETES_NAMESPACE = "KUBERNETES_NAMESPACE";
    private static final String HOST_SUFFIX = "_SERVICE_HOST";
    private static final String PORT_SUFFIX = "_SERVICE_PORT";
    private static final String PROTO_SUFFIX = "_TCP_PROTO";
    public static final String DEFAULT_PROTO = "tcp";
    public static final String DEFAULT_NAMESPACE = "default";

    public static String toServiceUrl(String serviceId, String serviceProtocol, boolean serviceExternal) {
        String serviceNamespace = Systems.getEnvVarOrSystemProperty(KUBERNETES_NAMESPACE, DEFAULT_NAMESPACE);
        return KUBERNETES.getServiceURL(serviceId, serviceNamespace, serviceProtocol, serviceExternal);
    }

    public static List<String> toServiceEndpointUrl(String serviceId, String serviceProtocol) {
        List<String> endpoints = new ArrayList<>();
        String namespace = Systems.getEnvVarOrSystemProperty(KUBERNETES_NAMESPACE, DEFAULT_NAMESPACE);
        String serviceProto = serviceProtocol != null ? serviceProtocol : DEFAULT_PROTO;

        try {
            for (String endpoint : KubernetesHelper.lookupServiceInDns(serviceId)) {
                endpoints.add(serviceProto + "://" + endpoint);
            }
        } catch (UnknownHostException e) {
            //ignore and fallback to the api.
        }
        
        if (!endpoints.isEmpty()) {
            return endpoints;
        }
        
        for (io.fabric8.kubernetes.api.model.Endpoints item : KUBERNETES.getEndpoints(namespace).getItems()) {
            if (item.getId().equals(serviceId) && (namespace == null || namespace.equals(item.getNamespace()))) {
                for (String endpoint : item.getEndpoints()) {
                    endpoints.add(serviceProto + "://" + endpoint);
                }
                break;
            }
        }
        return endpoints;
    }

    public static String serviceToHost(String id) {
        return Systems.getEnvVarOrSystemProperty(toEnvVariable(id + HOST_SUFFIX), "");
    }

    public static String serviceToPort(String id) {
        return Systems.getEnvVarOrSystemProperty(toEnvVariable(id + PORT_SUFFIX), "");
    }

    public static String serviceToProtocol(String id, String servicePort) {
        return Systems.getEnvVarOrSystemProperty(toEnvVariable(id + PORT_SUFFIX + "_" + servicePort + PROTO_SUFFIX), DEFAULT_PROTO);
    }


    public static String toEnvVariable(String str) {
        return str.toUpperCase().replaceAll("-", "_");
    }
}
