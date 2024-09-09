package com.uninote.backend.interfaceProjection;

import java.math.BigDecimal;

public interface FlashcardProjection {
    Long getId();
    Long getCourseId();
    Long getQuestionTypeId();
    String getQuestionText();  
    BigDecimal getIsDifficultRaw();
    String getAnswer();  
    

    
    default Boolean getIsDifficult() {
        return getIsDifficultRaw() != null && getIsDifficultRaw().intValue() == 1;
    }
}
