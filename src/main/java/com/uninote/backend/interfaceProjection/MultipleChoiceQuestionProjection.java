package com.uninote.backend.interfaceProjection;

import java.math.BigDecimal;

public interface MultipleChoiceQuestionProjection {
    Long getId();
    Long getCourseId();
    Long getQuestionTypeId();
    String getQuestionText();  
    BigDecimal getIsDifficultRaw();
    String getCorrectChoice();  
    String getImageUrl();

    
    default Boolean getIsDifficult() {
        return getIsDifficultRaw() != null && getIsDifficultRaw().intValue() == 1;
    }
}
