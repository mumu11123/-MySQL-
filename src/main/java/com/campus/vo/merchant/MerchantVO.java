package com.campus.vo.merchant;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MerchantVO {
    private Long id;
    private String username;
    private String shopName;
    private String category;
    private String contactPerson;
    private String phone;
    private String address;
    private String logo;
    private String description;
    private String businessLicense;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}