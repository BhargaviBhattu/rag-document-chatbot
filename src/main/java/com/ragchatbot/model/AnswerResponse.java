package com.ragchatbot.model;

public class AnswerResponse {
   


    private String answer;
    private String status;

    public AnswerResponse(String answer, String status) {
        this.answer = answer;
        this.status = status;
    }

    public String getAnswer() { return answer; }
    public String getStatus() { return status; }
}

