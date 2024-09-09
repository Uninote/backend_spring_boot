package com.uninote.backend.dto;

public class QuestionTypeDTO {

    private Long typeId;
    private String typeName;

    public QuestionTypeDTO(Long typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }

    
    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
