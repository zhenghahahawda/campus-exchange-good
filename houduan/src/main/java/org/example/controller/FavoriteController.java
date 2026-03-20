package org.example.controller;

import org.example.common.Result;
import org.example.entity.Good;
import org.example.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public Result<List<Good>> getFavorites(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return favoriteService.getUserFavorites(Integer.valueOf(userId));
    }

    @PostMapping
    public Result<String> addFavorite(@RequestBody Map<String, Integer> body, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        Integer goodsId = body.get("goodsId");
        return favoriteService.addFavorite(Integer.valueOf(userId), goodsId);
    }

    @DeleteMapping("/{goodsId}")
    public Result<String> removeFavorite(@PathVariable Integer goodsId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return favoriteService.removeFavorite(Integer.valueOf(userId), goodsId);
    }
}
