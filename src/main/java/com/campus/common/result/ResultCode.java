package com.campus.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一返回状态码枚举
 *
 * @author Campus Team
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 系统错误 */
    ERROR(500, "系统错误"),

    /** 未授权 / Token 校验失败 */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /** 权限不足 */
    FORBIDDEN(403, "权限不足，拒绝访问"),

    /** 请求参数校验失败 */
    BAD_REQUEST(400, "请求参数不正确"),

    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在");

    /** 状态码 */
    private final int code;

    /** 状态消息 */
    private final String message;
}
