package com.smartsourcing.charitycommission.rsi.model;

import jakarta.validation.constraints.NotBlank;

public class QuestionForm {
    @NotBlank(message = "This field is required")
    private String answer;

    private String questionId;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

