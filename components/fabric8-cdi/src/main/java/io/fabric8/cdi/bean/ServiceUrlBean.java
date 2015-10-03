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
package io.fabric8.cdi.bean;


import io.fabric8.cdi.producers.ServiceUrlProducer;
import io.fabric8.cdi.qualifiers.Qualifiers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServiceUrlBean extends ProducerBean<String> {

    private static final String SUFFIX = "-url";
    private static final Map<Key, ServiceUrlBean> BEANS = new HashMap<>();

    public static ServiceUrlBean getBean(String name, String protocol, String alias, String port, Boolean external) {
        String serviceAlias = alias != null ? alias :
                (external ? "external-" : "") + name + "-" + protocol + "-" + port + SUFFIX;
        Key key = new Key(name, protocol, serviceAlias, port, external);
        if (BEANS.containsKey(key)) {
            return BEANS.get(key);
        }
        ServiceUrlBean bean = new ServiceUrlBean(name, protocol, serviceAlias, port, external);
        BEANS.put(key, bean);
        return bean;
    }

    public static ServiceUrlBean anyBean(String id, String protocol, String port, Boolean external) {
        for (Map.Entry<Key, ServiceUrlBean> entry : BEANS.entrySet()) {
           Key key = entry.getKey();
           if (key.serviceName.equals(id) && key.serviceProtocol.equals(protocol)) {
               return entry.getValue();
           }
        }
        return getBean(id, protocol, null, port, external);
    }

    public static Collection<ServiceUrlBean> getBeans() {
        return BEANS.values();
    }
    private final String serviceName;
    private final String serviceProtocol;
    private final String serviceAlias;
    private final String servicePort;
    private final Boolean serviceExternal;

    private ServiceUrlBean(String serviceName, String serviceProtocol, String serviceAlias, String servicePort, Boolean serviceExternal) {
        super(serviceAlias, String.class, new ServiceUrlProducer(serviceName, serviceProtocol, servicePort, serviceExternal), Qualifiers.create(serviceName, serviceProtocol, servicePort, false, serviceExternal));
        this.serviceName = serviceName;
        this.serviceProtocol = serviceProtocol;
        this.serviceAlias = serviceAlias;
        this.servicePort = servicePort;
        this.serviceExternal = serviceExternal;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceProtocol() {
        return serviceProtocol;
    }

    public String getServiceAlias() {
        return serviceAlias;
    }

    @Override
    public String toString() {
        return "ServiceUrlBean[" +
                "serviceName='" + serviceName + '\'' +
                ", serviceProtocol='" + serviceProtocol + '\'' +
                ", servicePort='" + servicePort + '\'' +
                ']';
    }

    private static final class Key {
        private final String serviceName;
        private final String serviceProtocol;
        private final String serviceAlias;
        private final String servicePort;
        private final Boolean serviceExternal;

        private Key(String serviceName, String serviceProtocol, String serviceAlias, String servicePort, Boolean serviceExternal) {
            this.serviceName = serviceName;
            this.serviceProtocol = serviceProtocol;
            this.serviceAlias = serviceAlias;
            this.servicePort = servicePort;
            this.serviceExternal = serviceExternal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (serviceName != null ? !serviceName.equals(key.serviceName) : key.serviceName != null) return false;
            if (serviceProtocol != null ? !serviceProtocol.equals(key.serviceProtocol) : key.serviceProtocol != null) return false;
            if (serviceAlias != null ? !serviceAlias.equals(key.serviceAlias) : key.serviceAlias != null) return false;
            if (servicePort != null ? !servicePort.equals(key.servicePort) : key.servicePort != null) return false;
            if (serviceExternal != null ? !serviceExternal.equals(key.serviceExternal) : key.serviceExternal != null) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = serviceName != null ? serviceName.hashCode() : 0;
            result = 31 * result + (serviceProtocol != null ? serviceProtocol.hashCode() : 0);
            result = 31 * result + (serviceAlias != null ? serviceAlias.hashCode() : 0);
            result = 31 * result + (servicePort != null ? servicePort.hashCode() : 0);
            result = 31 * result + (serviceExternal != null ? serviceExternal.hashCode() : 0);
            return result;
        }
    }
}

