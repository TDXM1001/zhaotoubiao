package net.lab1024.sa.admin.module.system.bid.project.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 招标项目实体
 *
 * @author Codex
 * @date 2026-06-07
 */
@Data
@TableName("t_bid_project")
public class BidProjectEntity {

    @TableId(type = IdType.AUTO)
    private Long projectId;

    private String projectCode;

    private String projectName;

    private String projectType;

    private String procurementMode;

    private Long ownerOrgId;

    private Long agentOrgId;

    private Long managerEmployeeId;

    private String status;

    private BigDecimal budgetAmount;

    private LocalDateTime publishTime;

    private LocalDateTime awardTime;

    private LocalDateTime archiveTime;

    private String remark;

    private Boolean deletedFlag;

    private Integer version;

    private Long createUserId;

    private LocalDateTime createTime;

    private Long updateUserId;

    private LocalDateTime updateTime;
}
