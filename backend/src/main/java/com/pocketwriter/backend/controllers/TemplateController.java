package com.pocketwriter.backend.controllers;

import com.pocketwriter.backend.models.Template;
import com.pocketwriter.backend.services.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping
    public List<Template> getAllTemplates() {
        return templateService.getAllTemplates();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplateById(@PathVariable Long id) {
        Optional<Template> template = templateService.getTemplateById(id);
        return template.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Template createTemplate(@RequestBody Template template) {
        return templateService.createTemplate(template);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
public ResponseEntity<Template> updateTemplate(@PathVariable Long id, @RequestBody Template template) {
    Optional<Template> updated = templateService.updateTemplate(id, template);
    return updated
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}

}
