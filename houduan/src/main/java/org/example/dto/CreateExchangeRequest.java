package org.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateExchangeRequest {
    private Integer goodsId;
    private String message;
    private List<String> offerItems;
}
