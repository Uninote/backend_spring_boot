package com.uninote.backend.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


@Entity
@Table(name = "MESSAGES")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MESSAGE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CHAT_ID")
    private Chat chat;

    @Lob
    @Column(name = "USER_MESSAGE")
    private String userMessage;

    @Lob
    @Column(name = "SERVICE_RESPONSE")
    private String serviceResponse;

    @Column(name = "INPUT_TOKENS_USED")
    private Integer inputTokensUsed;

    @Column(name = "OUTPUT_TOKENS_USED")
    private Integer outputTokensUsed;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;

    @Lob
    @Column(name = "RESOURCE_SOURCES")
    private String resourceSources;

    @Lob
    @Column(name = "ANNOTATIONS")
    private String annotations;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageMedia> media = new ArrayList<>();

    public Message() {}

    // Parameterized constructor
    public Message(Chat chat, String userMessage, String serviceResponse, Integer inputTokensUsed, 
                   Integer outputTokensUsed, Timestamp createdAt, String resourceSources, String annotations) {
        this.chat = chat;
        this.userMessage = userMessage;
        this.serviceResponse = serviceResponse;
        this.inputTokensUsed = inputTokensUsed;
        this.outputTokensUsed = outputTokensUsed;
        this.createdAt = createdAt;
        this.resourceSources = resourceSources;
        this.annotations = annotations;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getServiceResponse() {
        return serviceResponse;
    }

    public void setServiceResponse(String serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

    public Integer getInputTokensUsed() {
        return inputTokensUsed;
    }

    public void setInputTokensUsed(Integer inputTokensUsed) {
        this.inputTokensUsed = inputTokensUsed;
    }

    public Integer getOutputTokensUsed() {
        return outputTokensUsed;
    }

    public void setOutputTokensUsed(Integer outputTokensUsed) {
        this.outputTokensUsed = outputTokensUsed;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getResourceSources() {
        return resourceSources;
    }

    public void setResourceSources(String resourceSources) {
        this.resourceSources = resourceSources;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public List<MessageMedia> getMedia() {
        return media;
    }
    
    public void setMedia(List<MessageMedia> media) {
        this.media = media;
    }
}