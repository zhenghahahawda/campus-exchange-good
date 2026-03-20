package org.example.service;

import org.example.common.Result;
import org.example.entity.GoodsCategory;

import java.util.List;

public interface GoodsCategoryService {
    
    Result<List<GoodsCategory>> getAllCategories();
    
    Result<List<GoodsCategory>> getActiveCategories();
}
