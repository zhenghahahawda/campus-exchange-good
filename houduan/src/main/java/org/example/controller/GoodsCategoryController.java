package org.example.controller;

import org.example.common.Result;
import org.example.entity.GoodsCategory;
import org.example.service.GoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    /**
     * 获取启用的商品分类
     * 普通用户使用（发布商品时选择分类）
     */
    @GetMapping
    public Result<List<GoodsCategory>> getActiveCategories() {
        System.out.println("=== 调用获取启用分类接口 ===");
        return goodsCategoryService.getActiveCategories();
    }

    /**
     * 获取所有商品分类（包括禁用的）
     * 管理员使用
     */
    @GetMapping("/all")
    public Result<List<GoodsCategory>> getAllCategories() {
        System.out.println("=== 调用获取所有分类接口 ===");
        return goodsCategoryService.getAllCategories();
    }
}
