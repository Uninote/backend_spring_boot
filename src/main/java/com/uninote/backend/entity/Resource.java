package com.uninote.backend.entity;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "RESOURCES")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESOURCE_ID")
    private Long id;

    @Column(name = "TITLE")
    @Lob
    private String title;

    @Column(name = "CONTENT")
    @Basic(fetch = FetchType.LAZY)
    @Lob
    private String content;

    @Column(name = "QUIZ")
    @Lob
    private String quiz;

    @Column(name = "FLASHCARDS")
    @Lob
    private String flashcards;

    @Column(name = "SUMMARY")
    @Lob
    private String summary;

    @Column(name = "CHAPTERS")
    @Lob
    private String chapters;

    @Column(name = "GENERATED_CONTENT")
    @Lob
    private String generatedContent;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @OneToOne(mappedBy = "resource", fetch = FetchType.LAZY)
    private ResourceChat resourceChat;

    @OneToMany(mappedBy = "resource", fetch = FetchType.LAZY)
    private List<SpaceResource> spaceResources;

   

    public Resource() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuiz() {
        return quiz;
    }

    public void setQuiz(String quiz) {
        this.quiz = quiz;
    }

    public String getFlashcards() {
        return flashcards;
    }

    public void setFlashcards(String flashcards) {
        this.flashcards = flashcards;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getChapters() {
        return chapters;
    }

    public void setChapters(String chapters) {
        this.chapters = chapters;
    }

    public String getGeneratedContent() {
        return generatedContent;
    }

    public void setGeneratedContent(String generatedContent) {
        this.generatedContent = generatedContent;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public ResourceChat getResourceChat() {
        return resourceChat;
    }

    public void setResourceChat(ResourceChat resourceChat) {
        this.resourceChat = resourceChat;
    }

    public List<SpaceResource> getSpaceResources() {
        return spaceResources;
    }

    public void setSpaceResources(List<SpaceResource> spaceResources) {
        this.spaceResources = spaceResources;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
