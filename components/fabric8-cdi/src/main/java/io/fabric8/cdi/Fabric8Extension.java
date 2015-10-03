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
package io.fabric8.cdi;

import io.fabric8.annotations.Alias;
import io.fabric8.annotations.Configuration;
import io.fabric8.annotations.Endpoint;
import io.fabric8.annotations.External;
import io.fabric8.annotations.Factory;
import io.fabric8.annotations.PortName;
import io.fabric8.annotations.Protocol;
import io.fabric8.annotations.ServiceName;
import io.fabric8.cdi.bean.ConfigurationBean;
import io.fabric8.cdi.bean.KubernetesClientBean;
import io.fabric8.cdi.bean.ServiceBean;
import io.fabric8.cdi.bean.ServiceUrlBean;
import io.fabric8.cdi.bean.ServiceUrlCollectionBean;
import io.fabric8.cdi.producers.FactoryMethodProducer;
import io.fabric8.cdi.qualifiers.ExternalQualifier;
import io.fabric8.cdi.qualifiers.PortQualifier;
import io.fabric8.cdi.qualifiers.ProtocolQualifier;
import io.fabric8.kubernetes.client.KubernetesClient;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessManagedBean;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Fabric8Extension implements Extension {

    private static final Set<FactoryMethodContext> factories = new LinkedHashSet<>();

    public void afterDiscovery(final @Observes AfterBeanDiscovery event, BeanManager beanManager) {

        //Only add the bean if no other bean is found.
        if (beanManager.getBeans(KubernetesClient.class).isEmpty()) {
            event.addBean(new KubernetesClientBean());
        }

        //We need to process factories in reverse order so that we make feasible forwarding for service id etc.
        List<FactoryMethodContext> reverseFactories = new ArrayList<>(FactoryMethodContext.sort(factories));
        Collections.reverse(reverseFactories);

        for (final FactoryMethodContext factoryMethodContext : reverseFactories) {
            ServiceBean.doWith(factoryMethodContext.getReturnType(), new ServiceBean.Callback() {
                @Override
                public ServiceBean apply(ServiceBean bean) {
                    String serviceId = bean.getServiceName();
                    String serviceProtocol = bean.getServiceProtocol();
                    String servicePort = bean.getServicePort();
                    Boolean serviceExternal = bean.getServiceExternal();

                    //Ensure that there is a factory String -> sourceType before adding producer.
                    if (!String.class.equals(factoryMethodContext.getSourceType())) {
                        ServiceBean.getBean(serviceId, serviceProtocol, null, servicePort, serviceExternal, (Class<Object>) factoryMethodContext.getSourceType());
                    }
                    return bean.withProducer(new FactoryMethodProducer(factoryMethodContext.getBean(), factoryMethodContext.getFactoryMethod(), serviceId, serviceProtocol, servicePort, serviceExternal));
                }
            });
        }

        for (ServiceUrlBean bean : ServiceUrlBean.getBeans()) {
            event.addBean(bean);
        }

        for (ServiceUrlCollectionBean bean : ServiceUrlCollectionBean.getBeans()) {
            event.addBean(bean);
        }
        
        for (ServiceBean bean : ServiceBean.getBeans()) {
            if (bean.getProducer() != null) {
                event.addBean(bean);
            }
        }
        for (ConfigurationBean b : ConfigurationBean.getBeans()) {
            event.addBean(b);
        }
    }

    public <R> void processAnnotatedType(@Observes ProcessAnnotatedType<R> pat,
                              BeanManager beanManager) {
     AnnotatedType type = pat.getAnnotatedType();
    }


    public <T, X> void onInjectionPoint(@Observes ProcessInjectionPoint<T, X> event, BeanManager beanManager) {
        final InjectionPoint injectionPoint = event.getInjectionPoint();
        if (isServiceInjectionPoint(injectionPoint)) {
            Annotated annotated = injectionPoint.getAnnotated();
            Alias alias = annotated.getAnnotation(Alias.class);
            ServiceName name = annotated.getAnnotation(ServiceName.class);
            PortName port = annotated.getAnnotation(PortName.class);
            Protocol protocol = annotated.getAnnotation(Protocol.class);
            External external = annotated.getAnnotation(External.class);
            
            Boolean serviceEndpoint = annotated.getAnnotation(Endpoint.class) != null;

            String serviceName = name.value();
            String serviceProtocol = protocol != null ? protocol.value() : "tcp";
            String serviceAlias = alias != null ? alias.value() : null;
            String servicePort = port != null ? port.value() : null;
            Boolean serviceExternal = external != null && external.value();
            
            Type type = annotated.getBaseType();
            if (type instanceof ParameterizedType && Instance.class.equals(((ParameterizedType)type).getRawType())) {
                type =  ((ParameterizedType) type).getActualTypeArguments()[0];
            }

            if (type.equals(String.class)) {
                ServiceUrlBean.getBean(serviceName, serviceProtocol, serviceAlias, servicePort, serviceExternal);
            } else if (serviceEndpoint && isGenericOf(type, List.class, String.class)) {
                ServiceUrlCollectionBean.getBean(serviceName, serviceProtocol, serviceAlias, servicePort, serviceEndpoint, Types.LIST_OF_STRINGS);
            } else if (serviceEndpoint && isGenericOf(type, List.class, null)) {
                //TODO: Integrate with Factories(?)
            } else if (serviceEndpoint && isGenericOf(type, Set.class, String.class)) {
                ServiceUrlCollectionBean.getBean(serviceName, serviceProtocol, serviceAlias, servicePort, serviceEndpoint, Types.SET_OF_STRINGS);
            } else if (serviceEndpoint && isGenericOf(type, Set.class, null)) {
                //TODO: Integrate with Factories(?)
            } else {
                ServiceBean.getBean(serviceName, serviceProtocol, serviceAlias, servicePort, serviceExternal, (Class) type);
            }

            if (protocol == null) {
                setDefaultProtocol(event);
            }
            if (port == null) {
                setDefaultPort(event);
            }
            if (external == null) {
                setExternalFalse(event);
            }
        } else if (isConfigurationInjectionPoint(injectionPoint)) {
            Annotated annotated = injectionPoint.getAnnotated();
            Configuration configuration = annotated.getAnnotation(Configuration.class);
            Type type = injectionPoint.getType();
            String configurationId = configuration.value();
            ConfigurationBean.getBean(configurationId, (Class) type);
        }
    }

    public <X> void onManagedBean(final @Observes ProcessManagedBean<X> event) {
        for (final AnnotatedMethod<?> method : event.getAnnotatedBeanClass().getMethods()) {
            final Factory factory = method.getAnnotation(Factory.class);
            if (factory != null) {
                final Type sourceType = getSourceType(method);
                final Type returnType = method.getJavaMember().getReturnType();
                factories.add(new FactoryMethodContext(event.getBean(), (Class) sourceType, (Class) returnType, method));
            }
        }
    }

    private static <T> Type getSourceType(AnnotatedMethod<T> method) {
        for (AnnotatedParameter<T> parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(ServiceName.class)) {
                return parameter.getBaseType();
            }
        }
        return String.class;
    }

    /**
     * Checks if the InjectionPoint is annotated with the @Configuration qualifier.
     *
     * @param injectionPoint The injection point.
     * @return
     */
    private static boolean isConfigurationInjectionPoint(InjectionPoint injectionPoint) {
        Set<Annotation> qualifiers = injectionPoint.getQualifiers();
        for (Annotation annotation : qualifiers) {
            if (annotation.annotationType().isAssignableFrom(Configuration.class)) {
                return true;
            }
        }
        return false;
    }
    
    
    private <T, X> void setDefaultProtocol(ProcessInjectionPoint<T, X> event) {
        //if protocol is not specified decorate injection point with "default" protocol.
        event.setInjectionPoint(new DelegatingInjectionPoint(event.getInjectionPoint()) {
            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new LinkedHashSet<>(super.getQualifiers());
                qualifiers.add(new ProtocolQualifier("tcp"));
                return Collections.unmodifiableSet(qualifiers);
            }
        });
    }

    private <T, X> void setDefaultPort(ProcessInjectionPoint<T, X> event) {
        //if portname is not specified decorate injection point with "default" port.
        event.setInjectionPoint(new DelegatingInjectionPoint(event.getInjectionPoint()) {
            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new LinkedHashSet<>(super.getQualifiers());
                qualifiers.add(new PortQualifier(""));
                return Collections.unmodifiableSet(qualifiers);
            }
        });
    }

    private <T, X> void setExternalFalse(ProcessInjectionPoint<T, X> event) {
        //if protocol is not specified decorate injection point with "default" protocol.
        event.setInjectionPoint(new DelegatingInjectionPoint(event.getInjectionPoint()) {
            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new LinkedHashSet<>(super.getQualifiers());
                qualifiers.add(new ExternalQualifier(false));
                return Collections.unmodifiableSet(qualifiers);
            }
        });
    }

    private static boolean isGenericOf(Type type, Type raw, Type argument) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return p.getRawType() != null
                    && p.getRawType().equals(raw)
                    && p.getActualTypeArguments().length == 1
                    && (p.getActualTypeArguments()[0].equals(argument) || argument == null);
        } else if (type instanceof Class) {
            return argument == null && type.equals(raw);
        } else {
            return false;
        }
    }
    

    /**
     * Checks if the InjectionPoint is annotated with the @Service qualifier.
     *
     * @param injectionPoint The injection point.
     * @return
     */
    public static boolean isServiceInjectionPoint(InjectionPoint injectionPoint) {
        Set<Annotation> qualifiers = injectionPoint.getQualifiers();
        for (Annotation annotation : qualifiers) {
            if (annotation.annotationType().isAssignableFrom(ServiceName.class)) {
                return true;
            }
        }
        return false;
    }
}
