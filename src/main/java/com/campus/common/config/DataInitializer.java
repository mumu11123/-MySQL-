package com.campus.common.config;

import com.campus.entity.Admin;
import com.campus.mapper.AdminMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminMapper adminMapper;
    private final DishMapper dishMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, "admin")
        );
        if (admin == null) {
            admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole("SUPER_ADMIN");
            admin.setRealName("系统管理员");
            admin.setStatus(1);
            adminMapper.insert(admin);
            log.info("默认管理员账号已创建: admin / 123456");
        } else if (!admin.getPassword().startsWith("$2a$")) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            adminMapper.updateById(admin);
            log.info("管理员密码已自动加密");
        }

        rebuildCurrentMonthDishSales();
        repairMerchantBlacklistSchema();
    }

    private void rebuildCurrentMonthDishSales() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);
        dishMapper.rebuildMonthlySales(start, end);
        log.info("菜品月销量已按本月已完成订单重新计算");
    }

    private void repairMerchantBlacklistSchema() {
        Integer userIdNotNull = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = 'merchant_blacklist' " +
                        "AND column_name = 'user_id' AND is_nullable = 'NO'",
                Integer.class
        );
        if (userIdNotNull == null || userIdNotNull == 0) {
            return;
        }

        dropForeignKeyIfExists("merchant_blacklist", "fk_blacklist_user");
        jdbcTemplate.execute("ALTER TABLE merchant_blacklist MODIFY user_id BIGINT NULL COMMENT '学生ID'");
        log.info("merchant_blacklist.user_id 已调整为允许为空，支持管理员拉黑整家商家");
    }

    private void dropForeignKeyIfExists(String tableName, String constraintName) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.table_constraints " +
                        "WHERE table_schema = DATABASE() AND table_name = ? " +
                        "AND constraint_name = ? AND constraint_type = 'FOREIGN KEY'",
                Integer.class,
                tableName,
                constraintName
        );
        if (exists != null && exists > 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + constraintName);
        }
    }

}
