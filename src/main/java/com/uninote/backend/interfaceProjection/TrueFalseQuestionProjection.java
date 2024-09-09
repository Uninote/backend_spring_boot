package com.uninote.backend.interfaceProjection;

import java.sql.Clob;
import java.sql.SQLException;
import java.io.Reader;
import java.math.BigDecimal;
import java.io.BufferedReader;
import java.io.IOException;

public interface TrueFalseQuestionProjection {
    Long getId();
    Long getCourseId();
    Long getQuestionTypeId();
    BigDecimal getIsDifficultRaw();
    String getQuestionText();  
    BigDecimal getCorrectAnswerRaw();
    String getImageUrl();

    
    default Boolean getIsDifficult() {
        return getIsDifficultRaw() != null && getIsDifficultRaw().intValue() == 1;
    }

    
    default Boolean getCorrectAnswer() {
        return getCorrectAnswerRaw() != null && getCorrectAnswerRaw().intValue() == 1;
    }
}
