package com.uninote.backend.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "note_collection_items" )
public class NoteCollectionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_collection_items_seq")
    @SequenceGenerator(name = "note_collection_items_seq", sequenceName = "note_collection_items_seq", allocationSize = 1)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }
}
