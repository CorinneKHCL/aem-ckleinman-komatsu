package com.ckleinman.komatsu.core.servlets;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

import com.day.cq.wcm.api.Page;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/ckleinman/modifiedpage",
        "sling.servlet.methods=GET",
    }
)
public class KomatsuEditServlet extends SlingAllMethodsServlet {
    /**
     * Handles GET requests to search page and child pages that were modified by the same user
     *
     * @param request  the incoming Sling request
     * @param response the outgoing Sling response
     * @throws IOException if an input or output error occurs
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String pageUrl = request.getParameter("path");

        if (pageUrl == null || pageUrl.isEmpty()) {
            response.getWriter().write("There is no url");
            return;
        }

        ResourceResolver resolver = request.getResourceResolver();
        Resource resource = resolver.getResource(pageUrl);

        if (resource == null) {
            response.setStatus(404);
            response.getWriter().write("Page does not exist");
            return;
        }        

        Page page = resource.adaptTo(Page.class);

        if (page == null) {
            response.setStatus(404);
            response.getWriter().write("Page does not exist");
            return;
        }

        JsonArray jsonArray = new JsonArray();
        String name = page.getLastModifiedBy();
        JsonObject obj = new JsonObject();

        obj.addProperty("modifiedBy", name);
        jsonArray.add(obj);

        Iterator<Page> childPages = page.listChildren(subpage -> {
            return name.equals(subpage.getLastModifiedBy());
        }, true);

        while (childPages.hasNext()) {
            Page child = childPages.next();
            JsonObject object = new JsonObject();
            object.addProperty("title", child.getTitle());
            object.addProperty("path", child.getPath());
            jsonArray.add(object);
        }

        response.getWriter().write(jsonArray.toString());
    }
}
