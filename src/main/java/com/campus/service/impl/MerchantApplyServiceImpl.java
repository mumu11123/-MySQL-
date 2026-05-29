package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.Merchant;
import com.campus.entity.MerchantApply;
import com.campus.entity.MerchantBlacklist;
import com.campus.mapper.MerchantApplyMapper;
import com.campus.mapper.MerchantBlacklistMapper;
import com.campus.mapper.MerchantMapper;
import com.campus.service.MerchantApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MerchantApplyServiceImpl extends ServiceImpl<MerchantApplyMapper, MerchantApply>
        implements MerchantApplyService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final MerchantMapper merchantMapper;
    private final MerchantBlacklistMapper merchantBlacklistMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<MerchantApply> listApplications() {
        return list(Wrappers.lambdaQuery(MerchantApply.class)
                .orderByDesc(MerchantApply::getAppliedAt)
                .orderByDesc(MerchantApply::getId));
    }

    @Override
    public List<MerchantApply> listApplicationsByStatus(Integer status) {
        return list(Wrappers.lambdaQuery(MerchantApply.class)
                .eq(MerchantApply::getStatus, status)
                .orderByDesc(MerchantApply::getAppliedAt)
                .orderByDesc(MerchantApply::getId));
    }

    @Override
    @Transactional
    public Map<String, String> approve(Long id, Long reviewerId) {
        MerchantApply apply = getById(id);
        if (apply == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!Integer.valueOf(0).equals(apply.getStatus())) {
            throw new RuntimeException("申请已审核，不能重复处理");
        }

        String username = buildUniqueUsername(apply);
        Merchant merchant = new Merchant();
        merchant.setUsername(username);
        merchant.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        merchant.setShopName(apply.getShopName());
        merchant.setCategory(apply.getCategory());
        merchant.setContactPerson(apply.getContactPerson());
        merchant.setPhone(apply.getPhone());
        merchant.setAddress(apply.getAddress());
        merchant.setBusinessLicense(apply.getBusinessLicense());
        merchant.setStatus(1);
        merchant.setIsDeleted(0);
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.insert(merchant);

        apply.setStatus(1);
        apply.setMerchantId(merchant.getId());
        apply.setReviewerId(reviewerId);
        apply.setReviewedAt(LocalDateTime.now());
        updateById(apply);

        Map<String, String> result = new HashMap<>();
        result.put("username", username);
        result.put("password", DEFAULT_PASSWORD);
        return result;
    }

    @Override
    @Transactional
    public void reject(Long id, String rejectReason, Long reviewerId) {
        MerchantApply apply = getById(id);
        if (apply == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!Integer.valueOf(0).equals(apply.getStatus())) {
            throw new RuntimeException("申请已审核，不能重复处理");
        }

        apply.setStatus(2);
        apply.setReviewerId(reviewerId);
        apply.setReviewComment(rejectReason);
        apply.setReviewedAt(LocalDateTime.now());
        updateById(apply);
    }

    @Override
    @Transactional
    public void addToBlacklist(Long id, String reason, Long adminId) {
        MerchantApply apply = getById(id);
        if (apply == null) {
            throw new RuntimeException("申请记录不存在");
        }
        Long merchantId = apply.getMerchantId();

        MerchantBlacklist blacklist = new MerchantBlacklist();
        blacklist.setMerchantId(merchantId != null ? merchantId : 0L);
        blacklist.setReason(reason);
        blacklist.setCreatedAt(LocalDateTime.now());
        merchantBlacklistMapper.insert(blacklist);

        if (merchantId != null && merchantId > 0) {
            Merchant merchant = merchantMapper.selectById(merchantId);
            if (merchant != null) {
                merchant.setStatus(3);
                merchant.setUpdatedAt(LocalDateTime.now());
                merchantMapper.updateById(merchant);
            }
        }

        apply.setStatus(2);
        apply.setReviewerId(adminId);
        apply.setReviewComment(reason);
        apply.setReviewedAt(LocalDateTime.now());
        updateById(apply);
    }

    private String buildUniqueUsername(MerchantApply apply) {
        String base = sanitizeUsername(apply.getShopName());
        if (base.isBlank()) {
            base = "merchant";
        }

        String username = base;
        int suffix = 1;
        while (merchantMapper.selectCount(Wrappers.lambdaQuery(Merchant.class)
                .eq(Merchant::getUsername, username)) > 0) {
            username = base + suffix;
            suffix++;
        }
        return username;
    }

    private String sanitizeUsername(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toLowerCase().replaceAll("\\s+", "_");
        return normalized.replaceAll("[^a-z0-9_\\u4e00-\\u9fa5]", "");
    }
}
