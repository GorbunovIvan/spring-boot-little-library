package com.example.controller;

import com.example.model.Visitor;
import com.example.service.VisitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/visitors")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("visitors", visitorService.getAll());
        return "visitors/visitors";
    }

    @GetMapping("/{id}")
    public String getById(@PathVariable int id, Model model) {
        var visitor = visitorService.getById(id);
        if (visitor == null) {
            throw new RuntimeException("Visitor with id '" + id + "' is not found");
        }
        model.addAttribute("visitor", visitor);
        return "visitors/visitor";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        var visitor = new Visitor();
        model.addAttribute("visitor", visitor);
        return "visitors/new";
    }

    @PostMapping
    public String create(Model model,
                         @ModelAttribute @Valid Visitor visitor, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("visitor", visitor);
            return "visitors/new";
        }
        visitorService.create(visitor);
        return "redirect:/visitors";
    }

    @GetMapping("/{id}/edit")
    public String updateForm(@PathVariable int id, Model model) {
        var visitor = visitorService.getById(id);
        if (visitor == null) {
            throw new RuntimeException("Visitor with id '" + id + "' is not found");
        }
        model.addAttribute("visitor", visitor);
        return "visitors/edit";
    }

    @PostMapping("/{id}")
    public String update(Model model, @PathVariable int id,
                         @ModelAttribute @Valid Visitor visitor, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("visitor", visitor);
            return "visitors/edit";
        }
        visitorService.update(id, visitor);
        return "redirect:/visitors/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable int id) {
        visitorService.deleteById(id);
        return "redirect:/visitors";
    }
}
