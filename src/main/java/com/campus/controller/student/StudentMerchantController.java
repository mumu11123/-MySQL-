package com.campus.controller.student;

import com.campus.common.result.Result;
import com.campus.entity.Merchant;
import com.campus.service.MerchantService;
import com.campus.vo.merchant.MerchantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "学生端-商家浏览")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentMerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "获取营业中商家列表")
    @GetMapping("/merchants")
    public Result<List<MerchantVO>> getMerchants() {
        List<MerchantVO> merchants = merchantService.lambdaQuery()
                .eq(Merchant::getStatus, 1)
                .orderByDesc(Merchant::getUpdatedAt)
                .orderByDesc(Merchant::getCreatedAt)
                .list()
                .stream()
                .map(this::toVO)
                .toList();
        return Result.success(merchants);
    }

    private MerchantVO toVO(Merchant merchant) {
        MerchantVO vo = new MerchantVO();
        BeanUtils.copyProperties(merchant, vo);
        vo.setUsername(null);
        vo.setBusinessLicense(null);
        return vo;
    }
}
