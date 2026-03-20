package org.example.service;

import org.example.common.Result;
import org.example.entity.Good;

import java.util.List;

public interface FavoriteService {

    Result<List<Good>> getUserFavorites(Integer userId);

    Result<String> addFavorite(Integer userId, Integer goodsId);

    Result<String> removeFavorite(Integer userId, Integer goodsId);
}
