package com.pocketwriter.backend.controllers;

import com.pocketwriter.backend.models.Article;
import com.pocketwriter.backend.models.Template;
import com.pocketwriter.backend.services.ArticleService;
import com.pocketwriter.backend.services.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private TemplateService templateService;

    @GetMapping
    public List<Article> getAllArticles() {
        return articleService.getAllArticles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        Optional<Article> article = articleService.getArticleById(id);
        return article.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        // Associate the article with a template if template info is provided
        if (article.getTemplate() != null && article.getTemplate().getId() != null) {
            Optional<Template> templateOpt = templateService.getTemplateById(article.getTemplate().getId());
            if (templateOpt.isPresent()) {
                article.setTemplate(templateOpt.get());
            } else {
                return ResponseEntity.badRequest().build(); // Template id not found
            }
        } else {
            article.setTemplate(null); // No template provided
        }
        Article savedArticle = articleService.createArticle(article);
        return ResponseEntity.ok(savedArticle);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        // Associate the article with a template if template info is provided
        if (article.getTemplate() != null && article.getTemplate().getId() != null) {
            Optional<Template> templateOpt = templateService.getTemplateById(article.getTemplate().getId());
            if (templateOpt.isPresent()) {
                article.setTemplate(templateOpt.get());
            } else {
                return ResponseEntity.badRequest().build(); // Template id not found
            }
        } else {
            article.setTemplate(null); // No template provided
        }
        Optional<Article> updated = articleService.updateArticle(id, article);
        return updated
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}
