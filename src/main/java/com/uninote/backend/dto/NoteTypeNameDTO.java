package com.uninote.backend.dto;


public class NoteTypeNameDTO {

    private String typeName;
    private Long typeId;

    public NoteTypeNameDTO() {
    }

    public NoteTypeNameDTO(String typeName, Long typeId) {
        this.typeName = typeName;
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

}
