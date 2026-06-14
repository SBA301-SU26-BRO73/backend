package com.sba301.backend.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sba301.backend.common.enums.UserRole;
import com.sba301.backend.common.enums.UserStatus;
import com.sba301.backend.entity.CourtType;
import com.sba301.backend.entity.PlanFeature;
import com.sba301.backend.entity.SubscriptionPlan;
import com.sba301.backend.entity.User;
import com.sba301.backend.repository.CourtTypeRepository;
import com.sba301.backend.repository.SubscriptionPlanRepository;
import com.sba301.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CourtTypeRepository courtTypeRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedSuperAdmin();
        seedCourtTypes();
        seedSubscriptionPlans();
        seedPendingUsers();
    }

    private void seedSuperAdmin() {
        String email = "admin@demo.com";
        if (userRepository.existsByEmail(email)) return;

        userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("admindemo"))
                .fullName("Super Admin")
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
    }

    private void seedCourtTypes() {
        if (courtTypeRepository.count() > 0) return;

        record CT(String name, String nameEn, String description, String icon, String color, boolean active) {}
        List<CT> types = List.of(
            new CT("Cầu lông",     "Badminton",    "Sân cầu lông trong nhà, mặt sân thảm hoặc gỗ", "feather",    "#15803D", true),
            new CT("Pickleball",   "Pickleball",   "Sân pickleball tiêu chuẩn 6.1m × 13.4m",        "circle-dot", "#0EA5E9", true),
            new CT("Tennis",       "Tennis",       "Sân tennis ngoài trời / trong nhà",              "zap",        "#D97706", true),
            new CT("Bóng đá mini", "Mini Football","Sân cỏ nhân tạo 5–7 người",                      "trophy",     "#65A30D", true),
            new CT("Bóng rổ",     "Basketball",   "Sân bóng rổ nửa sân / nguyên sân",              "star",       "#7C3AED", true),
            new CT("Bóng chuyền", "Volleyball",   "Sân bóng chuyền trong nhà",                      "layers",     "#DB2777", false)
        );

        for (CT ct : types) {
            CourtType entity = new CourtType();
            entity.setName(ct.name());
            entity.setNameEn(ct.nameEn());
            entity.setDescription(ct.description());
            entity.setIcon(ct.icon());
            entity.setColor(ct.color());
            entity.setActive(ct.active());
            courtTypeRepository.save(entity);
        }
    }

    private void seedSubscriptionPlans() {
        if (subscriptionPlanRepository.count() > 0) return;

        record PlanDef(
            String name, String tagline, long monthlyPrice, long yearlyPrice,
            int maxBranches, int maxCourts, String color, boolean popular,
            List<String> features
        ) {}

        List<PlanDef> plans = List.of(
            new PlanDef(
                "Cơ bản", "Cho chủ sân mới bắt đầu",
                299_000L, 2_990_000L, 1, 5, "#5B6B66", false,
                List.of(
                    "1 cơ sở",
                    "Tối đa 5 sân",
                    "Quản lý đặt sân cơ bản",
                    "Báo cáo doanh thu theo ngày",
                    "Hỗ trợ qua email"
                )
            ),
            new PlanDef(
                "Tiêu chuẩn", "Phổ biến cho chuỗi vừa",
                699_000L, 6_990_000L, 3, 15, "#15803D", true,
                List.of(
                    "Tối đa 3 cơ sở",
                    "Tối đa 15 sân",
                    "Quản lý đặt sân nâng cao",
                    "Báo cáo & biểu đồ chi tiết",
                    "Khuyến mãi & mã giảm giá",
                    "Hỗ trợ ưu tiên"
                )
            ),
            new PlanDef(
                "Chuyên nghiệp", "Cho chuỗi sân quy mô lớn",
                1_499_000L, 14_990_000L, 99, 99, "#0F6B4A", false,
                List.of(
                    "Không giới hạn cơ sở",
                    "Không giới hạn sân",
                    "Phân quyền nhân viên",
                    "API & tích hợp",
                    "Quản lý khách hàng (CRM)",
                    "Hỗ trợ 24/7 riêng"
                )
            )
        );

        for (PlanDef pd : plans) {
            SubscriptionPlan plan = new SubscriptionPlan();
            plan.setName(pd.name());
            plan.setTagline(pd.tagline());
            plan.setMonthlyPrice(pd.monthlyPrice());
            plan.setYearlyPrice(pd.yearlyPrice());
            plan.setMaxBranches(pd.maxBranches());
            plan.setMaxCourts(pd.maxCourts());
            plan.setColor(pd.color());
            plan.setPopular(pd.popular());
            plan.setActive(true);

            for (String feat : pd.features()) {
                PlanFeature pf = new PlanFeature();
                pf.setPlan(plan);
                pf.setFeature(feat);
                plan.getFeatures().add(pf);
            }

            subscriptionPlanRepository.save(plan);
        }
    }

    private void seedPendingUsers() {
        String marker = "pending01@demo.com";
        if (userRepository.existsByEmail(marker)) return;

        record PU(String email, String fullName, String phone) {}
        List<PU> pending = List.of(
            new PU("pending01@demo.com", "Nguyễn Văn An",   "0901234501"),
            new PU("pending02@demo.com", "Trần Thị Bích",   "0912888702"),
            new PU("pending03@demo.com", "Lê Hoàng Minh",   "0987555103"),
            new PU("pending04@demo.com", "Phạm Quốc Huy",   "0938222404"),
            new PU("pending05@demo.com", "Võ Thị Hằng",     "0909765405"),
            new PU("pending06@demo.com", "Đặng Thanh Tùng", "0901111206"),
            new PU("pending07@demo.com", "Hoàng Mỹ Linh",   "0935444507"),
            new PU("pending08@demo.com", "Ngô Bảo Châu",    "0918246808"),
            new PU("pending09@demo.com", "Vũ Thị Mai",      "0903909909"),
            new PU("pending10@demo.com", "Đỗ Minh Khoa",    "0944121210")
        );

        String hash = passwordEncoder.encode("demo1234");
        for (PU pu : pending) {
            userRepository.save(User.builder()
                    .email(pu.email())
                    .passwordHash(hash)
                    .fullName(pu.fullName())
                    .phone(pu.phone())
                    .role(UserRole.ADMIN)
                    .status(UserStatus.PENDING_APPROVAL)
                    .build());
        }
    }
}
