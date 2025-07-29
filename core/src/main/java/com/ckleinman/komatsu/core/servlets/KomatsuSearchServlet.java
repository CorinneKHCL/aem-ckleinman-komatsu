package com.ckleinman.komatsu.core.servlets;

import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import com.day.cq.wcm.api.Page;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.adobe.granite.jmx.annotation.Description;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import org.osgi.service.component.annotations.Reference;

@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/ckleinman/pagesearch",
        "sling.servlet.methods=GET",
    }
)
@Description("Komatsu Search page Servlet")
public class KomatsuSearchServlet extends SlingAllMethodsServlet {

    @Reference
    private QueryBuilder queryBuilder;

    /**
     * Handles GET requests to search pages by title or description.
     *
     * @param request  the incoming Sling request
     * @param response the outgoing Sling response
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String searchText = request.getParameter("searchTerm");

        response.setContentType("application/json");

        if (searchText == null || searchText.isEmpty()) {
            response.getWriter().write("[]");
            return;
        }

        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);

        // create a map for query builder to search through page set up
        Map<String, String> params = new HashMap<>();
        params.put("path", "/content");
        params.put("type", "cq:Page");
        params.put("group.p.or", "true");
        params.put("group.1_fulltext", searchText);
        params.put("group.1_fulltext.relPath", "jcr:content/jcr:title");
        params.put("group.2_fulltext", searchText);
        params.put("group.2_fulltext.relPath", "jcr:content/jcr:description");

        Query query = queryBuilder.createQuery(PredicateGroup.create(params), session);

        if (query == null) {
            response.getWriter().write("[]");
            return;
        }
        SearchResult result = query.getResult();

        JsonArray jsonArr = new JsonArray(); // all pages found saved in json array

        for (Hit hit : result.getHits()) {
            try {
                Resource resource = hit.getResource();
                Page page = resource.adaptTo(Page.class);

                ValueMap properties = page.getProperties();
                JsonObject jsonObj = new JsonObject();

                jsonObj.addProperty("title", page.getTitle());
                jsonObj.addProperty("description", properties.get("jcr:description", ""));

                // I'm not sure what the correct property name is as I don't have access to AEM
                jsonObj.addProperty("image", properties.get("cq:featuredImage","")); // also this is only getting the image and not alt text
                jsonObj.addProperty("modifiedDate",  properties.get(JcrConstants.JCR_LASTMODIFIED, ""));

                jsonArr.add(jsonObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        response.getWriter().write(jsonArr.toString());
    }

}