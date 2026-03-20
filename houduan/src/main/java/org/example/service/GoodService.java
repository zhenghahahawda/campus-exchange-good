package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.common.Result;
import org.example.dto.GoodsListResponse;
import org.example.entity.Good;
import java.util.List;

public interface GoodService extends IService<Good> {

    Result<GoodsListResponse> getGoodsList(Integer page, Integer limit, String category, String search, String sort, String exchangeType, Integer currentUserId);

    Result<Good> getGoodDetail(Integer id, Integer userId);

    Result<String> publishGood(Good good);

    Result<String> updateGoodStatus(Integer id, String status, Integer userId);

    Result<GoodsListResponse> getMyGoods(Integer userId, Integer page, Integer limit, Integer status, Integer shelfStatus);

    Result<String> likeGood(Integer goodId, Integer userId);

    Result<String> unlikeGood(Integer goodId, Integer userId);

    Result<List<Good>> getSimilarGoods(Integer goodId, Integer currentUserId, Integer conditionLevel, String sortBy, Integer limit);
}
