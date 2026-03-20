package org.example.dto;

import lombok.Data;
import org.example.entity.Good;

import java.util.List;

@Data
public class GoodsListResponse {
    private List<Good> list;
    private Long total;
    private Integer page;
    private Integer limit;
}
