package com.pocketwriter.backend.services;

import com.pocketwriter.backend.models.Template;
import com.pocketwriter.backend.repositories.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Optional<Template> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    public Template createTemplate(Template template) {
        return templateRepository.save(template);
    }

    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    public Optional<Template> updateTemplate(Long id, Template updatedTemplate) {
    return templateRepository.findById(id).map(existing -> {
        existing.setName(updatedTemplate.getName());
        existing.setLayoutJson(updatedTemplate.getLayoutJson());
        return templateRepository.save(existing);
    });
}

}
