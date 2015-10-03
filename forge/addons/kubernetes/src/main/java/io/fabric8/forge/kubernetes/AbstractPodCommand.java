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
package io.fabric8.forge.kubernetes;

import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.UICompleter;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for working with a pod
 */
public abstract class AbstractPodCommand extends AbstractKubernetesCommand {
    @Inject
    @WithAttributes(label = "Pod ID", description = "The ID of the pod.", required = true)
    UIInput<String> podId;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        super.initializeUI(builder);

        // populate autocompletion options
        podId.setCompleter(new UICompleter<String>() {
            @Override
            public Iterable<String> getCompletionProposals(UIContext context, InputComponent<?, String> input, String value) {
                List<String> list = new ArrayList<String>();
                PodList pods = getKubernetes().pods().list();
                if (pods != null) {
                    List<Pod> items = pods.getItems();
                    if (items != null) {
                        for (Pod item : items) {
                            String id = KubernetesHelper.getName(item);
                            list.add(id);
                        }
                    }
                }
                Collections.sort(list);
                System.out.println("Completion list is " + list);
                return list;
            }
        });

        builder.add(podId);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        KubernetesClient kubernetes = getKubernetes();

        String podIdText = podId.getValue();
        Pod podInfo = getKubernetes().pods().inNamespace(getNamespace()).withName(podIdText).get();
        if (podInfo == null) {
            System.out.println("No pod for id: " + podIdText);
        } else {
            executePod(podInfo, podIdText);
        }
        return null;
    }

    protected abstract void executePod(Pod pod, String podId) throws Exception;
}

