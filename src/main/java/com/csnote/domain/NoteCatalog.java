package com.csnote.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NoteCatalog {

    private final List<NotePage> pages;
    private final Map<String, String> anchors;
    private final List<SearchEntry> entries;

    public NoteCatalog(List<NotePage> pages, Map<String, String> anchors, List<SearchEntry> entries) {
        this.pages = List.copyOf(pages);
        this.anchors = Map.copyOf(anchors);
        this.entries = List.copyOf(entries);
    }

    public List<NotePage> getPages() {
        return pages;
    }

    public List<SearchEntry> getEntries() {
        return entries;
    }

    public Optional<NotePage> find(String slug) {
        return pages.stream().filter(p -> p.getSlug().equals(slug)).findFirst();
    }

    public Optional<NotePage> findByAnchor(String anchorId) {
        return Optional.ofNullable(anchors.get(anchorId)).flatMap(this::find);
    }

    public Optional<NotePage> previous(NotePage page) {
        int i = pages.indexOf(page);
        return i > 0 ? Optional.of(pages.get(i - 1)) : Optional.empty();
    }

    public Optional<NotePage> next(NotePage page) {
        int i = pages.indexOf(page);
        return i >= 0 && i < pages.size() - 1 ? Optional.of(pages.get(i + 1)) : Optional.empty();
    }
}
