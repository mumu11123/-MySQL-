package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dto.merchant.ApplyMerchantDTO;
import com.campus.dto.LoginMerchantDTO;
import com.campus.dto.merchant.UpdateMerchantDTO;
import com.campus.entity.Merchant;
import com.campus.entity.MerchantApply;
import com.campus.mapper.MerchantApplyMapper;
import com.campus.mapper.MerchantMapper;
import com.campus.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    private final MerchantApplyMapper merchantApplyMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Merchant login(LoginMerchantDTO dto) {
        // 根据用户名查询商家
        Merchant merchant = lambdaQuery()
                .eq(Merchant::getUsername, dto.getUsername())
                .one();
        // 校验账号是否存在
        if (merchant == null) throw new RuntimeException("账号不存在");
        // 校验密码（bcrypt 比对）
        if (!passwordEncoder.matches(dto.getPassword(), merchant.getPassword())) throw new RuntimeException("密码错误");
        return merchant;
    }

    @Override
    public void updateInfo(UpdateMerchantDTO dto, Long merchantId) {
        // 查询商家信息
        Merchant merchant = getById(merchantId);
        if (merchant == null) throw new RuntimeException("商家不存在");
        if (Integer.valueOf(3).equals(merchant.getStatus())) {
            throw new RuntimeException("商家已被管理员禁止营业，暂不能修改店铺信息");
        }
        // 只更新非空字段
        if (StringUtils.hasText(dto.getShopName())) merchant.setShopName(dto.getShopName());
        if (StringUtils.hasText(dto.getPhone())) merchant.setPhone(dto.getPhone());
        if (StringUtils.hasText(dto.getAddress())) merchant.setAddress(dto.getAddress());
        if (StringUtils.hasText(dto.getLogo())) merchant.setLogo(dto.getLogo());
        if (dto.getDescription() != null) merchant.setDescription(dto.getDescription());
        merchant.setUpdatedAt(LocalDateTime.now());
        // 保存修改
        updateById(merchant);
    }

    @Override
    public void applyMerchant(ApplyMerchantDTO dto) {
        MerchantApply oldApply = getApplyByPhone(dto.getPhone());
        if (oldApply != null && (Integer.valueOf(0).equals(oldApply.getStatus()) || Integer.valueOf(1).equals(oldApply.getStatus()))) {
            throw new RuntimeException("该手机号已存在待审核或已通过的入驻申请");
        }
        MerchantApply apply = new MerchantApply();
        apply.setShopName(dto.getShopName());
        apply.setCategory(dto.getCategory());
        apply.setContactPerson(dto.getContactPerson());
        apply.setPhone(dto.getPhone());
        apply.setAddress(dto.getAddress());
        apply.setBusinessLicense(dto.getBusinessLicense());
        apply.setStatus(0); // 0待审核
        apply.setAppliedAt(LocalDateTime.now());
        merchantApplyMapper.insert(apply);
    }

    @Override
    public MerchantApply getApplyByPhone(String phone) {
        // 查询申请记录
        MerchantApply apply = merchantApplyMapper.selectOne(
                Wrappers.lambdaQuery(MerchantApply.class)
                        .eq(MerchantApply::getPhone, phone)
                        .orderByDesc(MerchantApply::getAppliedAt)
                        .last("LIMIT 1")
        );
        // 如果申请存在 && 已经通过
        if (apply != null && apply.getStatus() == 1) {
            // 查询对应的商家
            Merchant merchant = this.getById(apply.getMerchantId());
            if (merchant != null) {
                // 把商家账号塞进 apply 里
                apply.setMerchantUsername(merchant.getUsername());
            }
        }
        return apply;
    }

    @Override
    public void updateMerchantStatus(Integer status, Long merchantId) {
        if (status == null || (status != 1 && status != 2)) {
            throw new RuntimeException("商家只能自主切换营业中或暂停营业");
        }
        Merchant merchant = getById(merchantId);
        if (merchant == null) throw new RuntimeException("商家不存在");
        if (Integer.valueOf(3).equals(merchant.getStatus())) {
            throw new RuntimeException("商家已被管理员禁止营业，不能自主恢复");
        }
        merchant.setStatus(status);
        merchant.setUpdatedAt(LocalDateTime.now());
        updateById(merchant);
    }

    @Override
    public void ensureCanHandleOrder(Long merchantId) {
        Merchant merchant = getById(merchantId);
        if (merchant == null) throw new RuntimeException("商家不存在");
        if (!Integer.valueOf(1).equals(merchant.getStatus())) {
            throw new RuntimeException("店铺当前非营业状态，不能处理订单");
        }
    }

    @Override
    public void ensureCanManageDish(Long merchantId) {
        Merchant merchant = getById(merchantId);
        if (merchant == null) throw new RuntimeException("商家不存在");
        if (Integer.valueOf(3).equals(merchant.getStatus())) {
            throw new RuntimeException("商家已被管理员禁止营业，不能维护菜品");
        }
    }
}
