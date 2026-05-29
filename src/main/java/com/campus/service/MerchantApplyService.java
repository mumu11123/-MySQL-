package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.MerchantApply;

import java.util.List;
import java.util.Map;

public interface MerchantApplyService extends IService<MerchantApply> {

    List<MerchantApply> listApplications();

    List<MerchantApply> listApplicationsByStatus(Integer status);

    Map<String, String> approve(Long id, Long reviewerId);

    void reject(Long id, String rejectReason, Long reviewerId);

    void addToBlacklist(Long id, String reason, Long adminId);
}
