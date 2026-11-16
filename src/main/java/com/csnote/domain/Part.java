package com.csnote.domain;

import java.util.ArrayList;
import java.util.List;

public class Part {

    private final String id;
    private final String title;
    private final List<Section> sections = new ArrayList<>();

    public Part(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Section> getSections() {
        return sections;
    }
}
