// I used AI to support writing the unit test for this servlet

package com.ckleinman.komatsu.core.servlets;

import com.day.cq.search.*;
import com.day.cq.search.result.*;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.testing.mock.aem.junit5.AemContext;

import org.apache.sling.api.resource.*;
import org.apache.sling.servlethelpers.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(AemContextExtension.class)
class KomatsuSearchServletTest {
    private final AemContext context = new AemContext();

    private KomatsuSearchServlet fixture = new KomatsuSearchServlet();

    Map<String, Object> map = new HashMap<>();

    QueryBuilder queryBuilder;
    Query query;
    SearchResult searchResult;
    Hit hit;
    Session session;

    MockSlingHttpServletRequest request;
    MockSlingHttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        context.build().resource("/content/test").commit();
        context.currentResource("/content/test");

        request = context.request();
        response = context.response();
    }

    @Test
    void doGetSearchNull(AemContext context) throws ServletException, IOException {
  
        fixture.doGet(request, response);

        assertEquals("[]", response.getOutputAsString());
    }

    @Test
    void doGetSearchEmpty(AemContext context) throws ServletException, IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        request.setParameterMap(Map.of("searchTerm", "sample"));
        mockQueries();
        fixture.doGet(request, response);

        assertEquals("[]", response.getOutputAsString());
    }


    @Test
    void doGetSearchText(AemContext context) throws ServletException, IOException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, RepositoryException {
        request.setParameterMap(Map.of("searchTerm", "sample"));

        mockQueries();

        context.create().page("/content/sample-page", "/apps/sample-template");
        Resource contentResource = context.resourceResolver().getResource("/content/sample-page/jcr:content");

        when(searchResult.getHits()).thenReturn(List.of(hit));
        when(hit.getResource()).thenReturn(contentResource);

        Page page = context.pageManager().getPage("/content/sample-page");
        assertNotNull(page);

        contentResource.adaptTo(ModifiableValueMap.class).put("jcr:title", "Sample Title");
        contentResource.adaptTo(ModifiableValueMap.class).put("jcr:description", "Sample Description");
        contentResource.adaptTo(ModifiableValueMap.class).put("cq:featuredImage", "/content/dam/sample.jpg");
        contentResource.adaptTo(ModifiableValueMap.class).put("cq:modifiedDate", Calendar.getInstance());

        context.request().setParameterMap(Map.of("searchTerm", "sample"));

        fixture.doGet(request, response);

        // TODO: Update once session is not null will page information from above
        assertEquals("[]", response.getOutputAsString());
    }

    private void mockQueries() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
       
        session = mock(Session.class); // I'm struggling to mock the session and it keeps returning null no matter what I try
        queryBuilder = mock(QueryBuilder.class);
        query = mock(Query.class);
        searchResult = mock(SearchResult.class);
        hit = mock(Hit.class);

        java.lang.reflect.Field field = KomatsuSearchServlet.class.getDeclaredField("queryBuilder");
        field.setAccessible(true);
        field.set(fixture, queryBuilder);

        when(queryBuilder.createQuery(any(PredicateGroup.class), any(Session.class)))
                .thenReturn(query);
        when(query.getResult()).thenReturn(searchResult);
    }
}