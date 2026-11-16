package com.csnote.service;

import com.csnote.domain.NoteCatalog;
import com.csnote.domain.NotePage;
import com.csnote.domain.Part;
import com.csnote.domain.SearchEntry;
import com.csnote.domain.Section;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class NoteScanner {

    private static final Set<String> HEADINGS = Set.of("h1", "h2", "h3", "h4");
    private static final int BODY_LIMIT = 600;

    public NoteCatalog scan(List<NotePage> pages) {
        Map<String, String> anchors = new HashMap<>();
        List<SearchEntry> entries = new ArrayList<>();
        for (NotePage page : pages) {
            scanPage(page, anchors, entries);
        }
        return new NoteCatalog(pages, anchors, entries);
    }

    private void scanPage(NotePage page, Map<String, String> anchors, List<SearchEntry> entries) {
        Document doc = load(page);
        doc.select("[id]").forEach(el -> anchors.putIfAbsent(el.id(), page.getSlug()));

        Part current = null;
        for (Element heading : doc.select("h1, h2, h3, h4")) {
            if (heading.id().isEmpty()) {
                continue;
            }
            String title = heading.text();
            if (heading.normalName().equals("h1")) {
                current = new Part(heading.id(), title);
                page.getParts().add(current);
                continue;
            }
            if (heading.normalName().equals("h2") && current != null) {
                current.getSections().add(new Section(heading.id(), title));
            }
            String partTitle = current != null ? current.getTitle() : page.getLabel();
            entries.add(new SearchEntry(heading.id(), page.getSlug(), partTitle, title, bodyOf(heading)));
        }
    }

    private String bodyOf(Element heading) {
        StringBuilder body = new StringBuilder();
        Element next = heading.nextElementSibling();
        while (next != null && !HEADINGS.contains(next.normalName()) && body.length() < BODY_LIMIT) {
            body.append(next.text()).append(' ');
            next = next.nextElementSibling();
        }
        return body.length() > BODY_LIMIT ? body.substring(0, BODY_LIMIT) : body.toString().trim();
    }

    private Document load(NotePage page) {
        ClassPathResource resource = new ClassPathResource("templates/" + page.getTemplate() + ".html");
        try (InputStream in = resource.getInputStream()) {
            return Jsoup.parse(in, "UTF-8", "");
        } catch (IOException e) {
            throw new UncheckedIOException("노트 템플릿을 읽을 수 없습니다: " + page.getSlug(), e);
        }
    }
}
