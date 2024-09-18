package com.uninote.backend.dto;

public class QuestionDTO {
    private Long id;
    private Long courseId;
    private Long questionTypeId;
    private String questionText;
    private Boolean isDifficult;


    public QuestionDTO(Long id, Long courseId, Long questionTypeId, String questionText, Boolean isDifficult) {
        this.id = id;
        this.courseId = courseId;
        this.questionTypeId = questionTypeId;
        this.questionText = questionText;
        this.isDifficult = isDifficult;
    }

    public QuestionDTO(Long questionId, Long courseId, Long questionTypeId, String questionText, Integer isDifficult) {
        this.id = questionId;
        this.courseId = courseId;
        this.questionTypeId = questionTypeId;
        this.questionText = questionText;
        this.isDifficult = (isDifficult != null && isDifficult == 1);
    }

    public QuestionDTO(){}

   public Boolean getIsDifficult(){
    return isDifficult;
   }

   public void setIsDifficult(Boolean isDifficult){
    this.isDifficult = isDifficult;
   }

   public void setId(Long id){
    this.id = id;
   }

   public Long getId(){
    return id;
   }
    

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getQuestionTypeId() {
        return questionTypeId;
    }

    public void setQuestionTypeId(Long questionTypeId) {
        this.questionTypeId = questionTypeId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
}
