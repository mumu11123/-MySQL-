package com.campus.vo.admin;

import lombok.Data;

@Data
public class AdminVO {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private Integer status;
}
