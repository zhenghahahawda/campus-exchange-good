package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.ConversationParticipant;

@Mapper
public interface ConversationParticipantMapper extends BaseMapper<ConversationParticipant> {
}
