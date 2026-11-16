package com.csnote.domain;

import java.util.ArrayList;
import java.util.List;

public class NotePage {

    private final String slug;
    private final String label;
    private final String summary;
    private final List<Part> parts = new ArrayList<>();

    public NotePage(String slug, String label, String summary) {
        this.slug = slug;
        this.label = label;
        this.summary = summary;
    }

    public String getSlug() {
        return slug;
    }

    public String getLabel() {
        return label;
    }

    public String getSummary() {
        return summary;
    }

    public List<Part> getParts() {
        return parts;
    }

    public String getTemplate() {
        return "notes/" + slug;
    }

    public String getPath() {
        return "home".equals(slug) ? "/" : "/notes/" + slug;
    }
}
