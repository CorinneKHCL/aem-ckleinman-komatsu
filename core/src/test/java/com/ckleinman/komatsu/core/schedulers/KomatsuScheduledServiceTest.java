// I used AI help create this class
package com.ckleinman.komatsu.core.schedulers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.jcr.Session;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.PageManager;

import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class KomatsuScheduledServiceTest {

    @InjectMocks
    private KomatsuScheduledService fixture;

    @Mock
    private Scheduler scheduler;

    @Mock
    private SlingSettingsService slingSettingsService;

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private QueryBuilder queryBuilder;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Session session;

    @Mock
    private Query query;

    @Mock
    private SearchResult searchResult;

    @Mock
    private Hit hit;

    @Mock
    private Resource page;

    @Mock
    private Resource child;

    @Mock
    private ModifiableValueMap modifiableValueMap;

    @Mock
    private KomatsuScheduledService.Config config;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRun_setsProcessedDateOnPublishedPages() throws Exception {
        activateMocks(true);
        fixture.run();

        verify(modifiableValueMap).put(eq("processedDate"), any(Calendar.class));
        verify(resourceResolver).commit();
    }

    @Test
    void testNotAuthorMode() throws Exception {
        activateMocks(false);

        fixture.run();

        verify(resourceResolverFactory, never()).getServiceResourceResolver(anyMap());
        verify(queryBuilder, never()).createQuery(any(), any());
    }

    private void activateMocks(boolean isAuthor) throws Exception {
        when(slingSettingsService.getRunModes()).thenReturn(Set.of(isAuthor ? "author" : "publish"));

        when(config.scheduler_expression()).thenReturn("0 0/2 * * * ?");
        when(config.scheduler_concurrent()).thenReturn(false);

        ScheduleOptions options = mock(ScheduleOptions.class);
        when(scheduler.EXPR(anyString())).thenReturn(options);
        when(options.name(anyString())).thenReturn(options);
        when(options.canRunConcurrently(anyBoolean())).thenReturn(options);

        fixture.activate(config);

        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenReturn(resourceResolver);
        when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        when(resourceResolver.adaptTo(PageManager.class)).thenReturn(mock(PageManager.class));

        when(queryBuilder.createQuery(any(PredicateGroup.class), eq(session))).thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
        when(searchResult.getHits()).thenReturn(List.of(hit));

        when(hit.getResource()).thenReturn(page);
        when(page.getChild("jcr:content")).thenReturn(child);
        when(child.getValueMap()).thenReturn(mock(ValueMap.class));
        when(child.adaptTo(ModifiableValueMap.class)).thenReturn(modifiableValueMap);
    }
}
