package net.lab1024.sa.admin.module.system.bid.common.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.system.bid.common.domain.entity.BidWorkflowHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 招投标流程历史 Dao
 *
 * @author Codex
 * @date 2026-06-07
 */
@Mapper
public interface BidWorkflowHistoryDao extends BaseMapper<BidWorkflowHistoryEntity> {
}
