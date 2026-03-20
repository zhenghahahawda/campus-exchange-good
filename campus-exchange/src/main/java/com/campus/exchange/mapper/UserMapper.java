package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.exchange.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 调用存储过程生成用户ID
     * @param params 参数Map，包含输出参数userId
     */
    void generateUserId(Map<String, Object> params);

    /**
     * 同步更新user_id_sequence表的序列值
     * @param newId 新的序列值
     */
    @Update("UPDATE user_id_sequence SET id = #{newId} WHERE stub = 'a'")
    void updateUserIdSequence(Integer newId);

    /**
     * 原子性递增user_id_sequence并返回新ID
     * 使用 LAST_INSERT_ID 保证并发安全
     */
    @Update("UPDATE user_id_sequence SET id = LAST_INSERT_ID(id + 1) WHERE stub = 'a'")
    void incrementUserIdSequence();

    /**
     * 获取 LAST_INSERT_ID 的值（配合 incrementUserIdSequence 使用）
     */
    @Select("SELECT LAST_INSERT_ID()")
    Integer getLastInsertId();
}
