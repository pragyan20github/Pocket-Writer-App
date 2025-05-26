package com.pocketwriter.backend.services;

import com.pocketwriter.backend.models.Article;
import com.pocketwriter.backend.repositories.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    public Optional<Article> updateArticle(Long id, Article updatedArticle) {
        return articleRepository.findById(id).map(existing -> {
            existing.setTitle(updatedArticle.getTitle());
            existing.setContent(updatedArticle.getContent());
            existing.setTemplate(updatedArticle.getTemplate());
            return articleRepository.save(existing);
        });
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }
}
