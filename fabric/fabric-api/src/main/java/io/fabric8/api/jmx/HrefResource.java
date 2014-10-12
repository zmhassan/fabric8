package io.fabric8.api.jmx;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HrefResource {
    public String name;
    public List<HashMap<String, String>> links= new ArrayList<HashMap<String,String>>();
    public Map<String, String> attribute= new HashMap<String,String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HrefResource(final String name, final String rel, final String href, String method){
        setName(name);
        links.add(new HashMap<String, String>() {
            {
                put("rel", rel);
                put("href", href);
            }});

        attribute.put("method",method);
    }
    public Map<String, String> getAttribute() {
        return attribute;
    }

    public void setAttribute(Map<String, String> attribute) {
        this.attribute = attribute;
    }




}