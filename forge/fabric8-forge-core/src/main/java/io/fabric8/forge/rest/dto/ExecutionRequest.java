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
package io.fabric8.forge.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@XmlRootElement
public class ExecutionRequest {
    @XmlElement
    private String resource;

    @XmlElementWrapper
    private List<Map<String, String>> inputList;

    @XmlElementWrapper
    private List<String> promptQueue;

    private Integer wizardStep;

    @Override
    public String toString() {
        return "ExecutionRequest{" +
                "resource='" + resource + '\'' +
                ", inputs=" + inputList +
                ", promptQueue=" + promptQueue +
                '}';
    }

    public List<Map<String, String>> getInputList() {
        return inputList;
    }

    public void setInputList(List<Map<String, String>> inputList) {
        this.inputList = inputList;
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getWizardStep() {
        return wizardStep;
    }

    public void setWizardStep(Integer wizardStep) {
        this.wizardStep = wizardStep;
    }

    /**
     * Returns the wizard step number or 0 if one is not defined
     */
    public int wizardStep() {
        if (wizardStep != null) {
            return wizardStep.intValue();
        } else {
            return 0;
        }
    }
}
