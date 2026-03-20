package org.example.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String conversationId;
    private String content;
    private String type;
}
