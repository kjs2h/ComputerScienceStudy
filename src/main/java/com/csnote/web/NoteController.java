package com.csnote.web;

import com.csnote.domain.NoteCatalog;
import com.csnote.domain.NotePage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
public class NoteController {

    private final NoteCatalog catalog;

    public NoteController(NoteCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/")
    public String home(Model model) {
        return page("home", model);
    }

    @GetMapping("/notes/{slug}")
    public String page(@PathVariable String slug, Model model) {
        NotePage page = catalog.find(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("page", page);
        model.addAttribute("prev", catalog.previous(page).orElse(null));
        model.addAttribute("next", catalog.next(page).orElse(null));
        return "layout";
    }

    @GetMapping("/go/{anchorId}")
    public String go(@PathVariable String anchorId) {
        return catalog.findByAnchor(anchorId)
                .map(page -> "redirect:" + page.getPath() + "#" + UriUtils.encodeFragment(anchorId, StandardCharsets.UTF_8))
                .orElse("redirect:/");
    }
}
