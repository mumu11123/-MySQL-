package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("merchant_apply")
public class MerchantApply {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;// 申请通过后自动关联merchant.id
    private String shopName;
    private String category;
    private String contactPerson;
    private String phone;
    private String address;
    private String businessLicense;
    private Integer status; // 0待审核 1已通过 2已驳回 3退回补充
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;

    @TableField(exist = false)
    private String merchantUsername;
}