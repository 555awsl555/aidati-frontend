package com.aw.aidati.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 应用
 * @TableName app
 */
@TableName(value ="app")
@Data
public class App implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 应用名
     */
    private String appname;

    /**
     * 应用描述
     */
    private String appdesc;

    /**
     * 应用图标
     */
    private String appicon;

    /**
     * 应用类型（0-得分类，1-测评类）
     */
    private Integer apptype;

    /**
     * 评分策略（0-自定义，1-AI）
     */
    private Integer scoringstrategy;

    /**
     * 审核状态：0-待审核, 1-通过, 2-拒绝
     */
    private Integer reviewstatus;

    /**
     * 审核信息
     */
    private String reviewmessage;

    /**
     * 审核人 id
     */
    private Long reviewerid;

    /**
     * 审核时间
     */
    private Date reviewtime;

    /**
     * 创建用户 id
     */
    private Long userid;

    /**
     * 创建时间
     */
    private Date createtime;

    /**
     * 更新时间
     */
    private Date updatetime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isdelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}