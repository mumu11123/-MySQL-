package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("merchant_blacklist")
public class MerchantBlacklist {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;
    private Long userId;
    private String reason;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String shopName;
}