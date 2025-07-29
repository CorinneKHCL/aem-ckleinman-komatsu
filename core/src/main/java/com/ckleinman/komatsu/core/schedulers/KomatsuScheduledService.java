
package com.ckleinman.komatsu.core.schedulers;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.PageManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@Component(service = Runnable.class)
public class KomatsuScheduledService implements Runnable {
    private static final String JOB_NAME = "com.ckleinman.komatsu.core.schedulers.KomatsuScheduledService";

    @Reference
    private Scheduler scheduler;

    @Reference
    private SlingSettingsService slingSettingsService;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    private boolean isAuthor;

    private Config config;

    @ObjectClassDefinition(name = "A scheduled task", description = "Runs every two minutes and updates processed date with current times for published pages")
    public static @interface Config {

        @AttributeDefinition(name = "Cron-job expression")
        String scheduler_expression() default "0 0/2 * * * ?";

        @AttributeDefinition(name = "Concurrent task", description = "Whether or not to schedule this task concurrently")
        boolean scheduler_concurrent() default false;
    }

    @Activate
    protected void activate(Config config) {
        this.config = config;
        isAuthor = slingSettingsService.getRunModes().contains("author");

        scheduler.unschedule(JOB_NAME);

        if (isAuthor) {
            // I used AI with some help with the scheduler as that is something I'm less
            // familiar with and the resources I found were a little thin -- would
            // definitely love to discuss and get some pointers
            ScheduleOptions scheduleOptions = scheduler.EXPR(config.scheduler_expression());
            scheduleOptions.name(JOB_NAME).canRunConcurrently(config.scheduler_concurrent());
            scheduler.schedule(this, scheduleOptions);
        } else {
            return;
        }
    }

    @Override
    public void run() {
        if (!isAuthor) {
            return;
        }

        Map<String, Object> params = Map.of(ResourceResolverFactory.SUBSERVICE, "writeService");

        try {
            ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(params);

            if (resourceResolver == null) {
                return;
            }
            Session session = resourceResolver.adaptTo(Session.class);
            PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

            if (pageManager == null) {
                return;
            }

            Map<String, String> queryMap = new HashMap<>();
            queryMap.put("path", "/content");
            queryMap.put("type", "cq:Page");
            queryMap.put("1_property", "cq:lastReplicationAction");
            queryMap.put("1_property.value", "Activate");

            Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
            SearchResult result = query.getResult();

            for (Hit hit : result.getHits()) {
                Resource resource = hit.getResource();
                Resource child = resource.getChild("jcr:content");

                if (child != null && child.getValueMap() != null) {
                    child.adaptTo(ModifiableValueMap.class)
                    .put("processedDate", Calendar.getInstance());

                    resourceResolver.commit();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
