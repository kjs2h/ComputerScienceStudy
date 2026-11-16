package com.csnote.service;

import com.csnote.domain.NoteCatalog;
import com.csnote.domain.SearchEntry;
import com.csnote.domain.SearchHit;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class SearchService {

    private static final int MAX_HITS = 40;
    private static final int SNIPPET_BEFORE = 25;
    private static final int SNIPPET_LENGTH = 90;

    private final NoteCatalog catalog;

    public SearchService(NoteCatalog catalog) {
        this.catalog = catalog;
    }

    public List<SearchHit> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            return List.of();
        }
        return catalog.getEntries().stream()
                .map(entry -> match(entry, q))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing((SearchHit hit) -> !hit.snippet().isEmpty()))
                .limit(MAX_HITS)
                .toList();
    }

    private Optional<SearchHit> match(SearchEntry entry, String q) {
        String url = "/go/" + entry.id();
        if (entry.title().toLowerCase(Locale.ROOT).contains(q)) {
            return Optional.of(new SearchHit(url, entry.part(), entry.title(), ""));
        }
        int index = entry.body().toLowerCase(Locale.ROOT).indexOf(q);
        if (index < 0) {
            return Optional.empty();
        }
        int start = Math.max(0, index - SNIPPET_BEFORE);
        int end = Math.min(entry.body().length(), start + SNIPPET_LENGTH);
        String snippet = (start > 0 ? "…" : "") + entry.body().substring(start, end) + "…";
        return Optional.of(new SearchHit(url, entry.part(), entry.title(), snippet));
    }
}
