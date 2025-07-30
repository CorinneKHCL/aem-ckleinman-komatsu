package com.ckleinman.komatsu.core.models.impl;

import com.ckleinman.komatsu.core.models.Komatsu;
import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.wcm.core.components.util.AbstractComponentImpl;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(
    adaptables = Resource.class,
    adapters = {Komatsu.class, ComponentExporter.class},
    resourceType = KomatsuImpl.RESOURCE_TYPE
)
public class KomatsuImpl extends AbstractComponentImpl implements Komatsu {
    public static final String RESOURCE_TYPE = "ckleinman/components/komatsu";

    @ValueMapValue
    private String text;

    @ValueMapValue
    private String button;

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public String getButton() {
        return this.button;
    }

    @Override
    public String getClassName() {
        return "cmp-komatsu-form";
    }
}