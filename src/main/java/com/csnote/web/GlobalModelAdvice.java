package com.csnote.web;

import com.csnote.domain.NoteCatalog;
import com.csnote.domain.NotePage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(assignableTypes = NoteController.class)
public class GlobalModelAdvice {

    private final NoteCatalog catalog;

    public GlobalModelAdvice(NoteCatalog catalog) {
        this.catalog = catalog;
    }

    @ModelAttribute("pages")
    public List<NotePage> pages() {
        return catalog.getPages();
    }
}
