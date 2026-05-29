package com.campus.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.merchant.ApplyMerchantDTO;
import com.campus.dto.LoginMerchantDTO;
import com.campus.dto.merchant.UpdateMerchantDTO;
import com.campus.entity.Merchant;
import com.campus.entity.MerchantApply;

public interface MerchantService extends IService<Merchant> {

    /**
     * 商家登录校验
     * @param dto 登录参数
     * @return 商家信息
     */
    Merchant login(LoginMerchantDTO dto);

    /**
     * 修改商家信息
     * @param dto 修改参数
     * @param merchantId 商家ID，从Token中获取
     */
    void updateInfo(UpdateMerchantDTO dto, Long merchantId);

    /**
     * 提交入驻申请
     * @param dto 入驻申请信息
     */
    void applyMerchant(ApplyMerchantDTO dto);

    /**
     * 根据手机号查询入驻申请
     * @param phone 手机号
     * @return 入驻申请信息
     */
    MerchantApply getApplyByPhone(String phone);

    /**
     * 修改营业状态
     * @param status 营业状态（0-休息中，1-营业中）
     * @param merchantId 商家ID，从Token中获取
     */
    void updateMerchantStatus(Integer status, Long merchantId);

    /**
     * 校验商家是否允许处理订单。
     * @param merchantId 商家ID
     */
    void ensureCanHandleOrder(Long merchantId);

    /**
     * 校验商家是否允许维护菜品。
     * @param merchantId 商家ID
     */
    void ensureCanManageDish(Long merchantId);
}
