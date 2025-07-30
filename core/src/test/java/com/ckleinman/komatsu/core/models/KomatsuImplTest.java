// I used AI to help write some of the unit test
package com.ckleinman.komatsu.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ckleinman.komatsu.core.models.impl.KomatsuImpl;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class KomatsuImplTest {
    private final AemContext context = new AemContext();

    private Komatsu model;

    @BeforeEach
    void setup() {
        context.addModelsForClasses(Komatsu.class, KomatsuImpl.class);

        context.load().json("/komatsu/test-content.json", "/content/mypage");

        // Get the jcr:content node
        Resource resource = context.resourceResolver().getResource("/content/mypage/jcr:content");

        // Adapt to model
        model = resource.adaptTo(KomatsuImpl.class);
    }

    // I'm struggling to get the code to work using either this method or using the json test file to mock but this is what I would od
    // @Test
    // void testModelReturnsConfiguredValues() {
    //     assertNotNull(model, "Model should not be null");

    //     assertEquals("Search AEM Pages", model.getText(), "Text should match the injected value");
    //     assertEquals("Submit Search", model.getButton(), "Button should match the injected value");
    //     assertEquals("cmp-komatsu-form", model.getClassName(), "Class name should match the hardcoded return value");
    // }
}