package com.campus.controller.auth;

import com.campus.common.result.Result;
import com.campus.dto.LoginMerchantDTO;
import com.campus.dto.student.LoginDTO;
import com.campus.dto.student.RegisterStudentDTO;
import com.campus.entity.Merchant;
import com.campus.entity.User;
import com.campus.service.MerchantService;
import com.campus.service.UserService;
import com.campus.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "认证模块", description = "登录、注册、Token 管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MerchantService merchantService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Operation(summary = "项目启动测试", description = "返回成功消息，验证工程是否可正常运行")
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("项目启动成功");
    }

    @Operation(summary = "商家登录", description = "商家账号登录，返回 JWT Token")
    @PostMapping("/login/merchant")
    public Result<String> loginMerchant(@Valid @RequestBody LoginMerchantDTO dto) {
        Merchant merchant = merchantService.login(dto);
        String token = jwtUtil.generateToken(merchant.getId(), "merchant");
        return Result.success("登录成功", token);
    }

    @Operation(summary = "学生注册", description = "学生账号注册")
    @PostMapping("/register/student")
    public Result<Void> registerStudent(@Valid @RequestBody RegisterStudentDTO dto) {
        long exists = userService.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .count();
        if (exists > 0) {
            return Result.error(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userService.save(user);
        return Result.success();
    }

    @Operation(summary = "学生登录", description = "学生账号登录，返回 JWT Token")
    @PostMapping("/login/student")
    public Result<String> loginStudent(@Valid @RequestBody LoginDTO dto) {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error(400, "用户名或密码错误");
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            return Result.error(403, "账号已禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), "student");
        return Result.success("登录成功", token);
    }
}
