-- =====================================================================
-- SEED DATA MIGRATION FOR TESTING (V7)
-- =====================================================================

-- 1. Seed Court Types (if not present)
INSERT INTO court_types (name, name_en, description, icon, color, active)
SELECT name, name_en, description, icon, color, active
FROM (VALUES
    ('Cầu lông',     'Badminton',    'Sân cầu lông trong nhà, mặt sân thảm hoặc gỗ', 'feather',    '#15803D', true),
    ('Pickleball',   'Pickleball',   'Sân pickleball tiêu chuẩn 6.1m × 13.4m',        'circle-dot', '#0EA5E9', true),
    ('Tennis',       'Tennis',       'Sân tennis ngoài trời / trong nhà',              'zap',        '#D97706', true),
    ('Bóng đá mini', 'Mini Football','Sân cỏ nhân tạo 5–7 người',                      'trophy',     '#65A30D', true),
    ('Bóng rổ',     'Basketball',   'Sân bóng rổ nửa sân / nguyên sân',              'star',       '#7C3AED', true),
    ('Bóng chuyền', 'Volleyball',   'Sân bóng chuyền trong nhà',                      'layers',     '#DB2777', false)
) AS v(name, name_en, description, icon, color, active)
WHERE NOT EXISTS (SELECT 1 FROM court_types WHERE name = v.name);

-- 2. Seed Subscription Plans (if not present)
INSERT INTO subscription_plans (name, tagline, max_courts, max_branches, is_active, monthly_price, yearly_price, color, popular)
SELECT name, tagline, max_courts, max_branches, is_active, monthly_price, yearly_price, color, popular
FROM (VALUES
    ('Cơ bản', 'Cho chủ sân mới bắt đầu', 5, 1, true, 299000, 2990000, '#5B6B66', false),
    ('Tiêu chuẩn', 'Phổ biến cho chuỗi vừa', 15, 3, true, 699000, 6990000, '#15803D', true),
    ('Chuyên nghiệp', 'Cho chuỗi sân quy mô lớn', 99, 99, true, 1499000, 14990000, '#0F6B4A', false)
) AS v(name, tagline, max_courts, max_branches, is_active, monthly_price, yearly_price, color, popular)
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = v.name);

-- 3. Seed Plan Features (if not present)
INSERT INTO plan_features (plan_id, feature)
SELECT sp.id, f.feature
FROM subscription_plans sp
CROSS JOIN (
    VALUES 
        ('Cơ bản', '1 cơ sở'),
        ('Cơ bản', 'Tối đa 5 sân'),
        ('Cơ bản', 'Quản lý đặt sân cơ bản'),
        ('Cơ bản', 'Báo cáo doanh thu theo ngày'),
        ('Cơ bản', 'Hỗ trợ qua email'),
        ('Tiêu chuẩn', 'Tối đa 3 cơ sở'),
        ('Tiêu chuẩn', 'Tối đa 15 sân'),
        ('Tiêu chuẩn', 'Quản lý đặt sân nâng cao'),
        ('Tiêu chuẩn', 'Báo cáo & biểu đồ chi tiết'),
        ('Tiêu chuẩn', 'Khuyến mãi & mã giảm giá'),
        ('Tiêu chuẩn', 'Hỗ trợ ưu tiên'),
        ('Chuyên nghiệp', 'Không giới hạn cơ sở'),
        ('Chuyên nghiệp', 'Không giới hạn sân'),
        ('Chuyên nghiệp', 'Phân quyền nhân viên'),
        ('Chuyên nghiệp', 'API & tích hợp'),
        ('Chuyên nghiệp', 'Quản lý khách hàng (CRM)'),
        ('Chuyên nghiệp', 'Hỗ trợ 24/7 riêng')
) AS f(plan_name, feature)
WHERE sp.name = f.plan_name
  AND NOT EXISTS (
      SELECT 1 FROM plan_features pf 
      WHERE pf.plan_id = sp.id AND pf.feature = f.feature
  );

-- 4. Seed Active Admin Users
INSERT INTO users (email, password_hash, phone, role, status, full_name)
SELECT email, password_hash, phone, role, status, full_name
FROM (VALUES
    ('owner01@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0909000001', 'ADMIN', 'ACTIVE', 'Admin Một'),
    ('owner02@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0909000002', 'ADMIN', 'ACTIVE', 'Admin Hai'),
    ('owner03@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0909000003', 'ADMIN', 'ACTIVE', 'Admin Ba'),
    ('owner04@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0909000004', 'ADMIN', 'ACTIVE', 'Admin Bốn'),
    ('owner05@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0909000005', 'ADMIN', 'ACTIVE', 'Admin Năm')
) AS v(email, password_hash, phone, role, status, full_name)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = v.email);

-- 5. Seed Active Customer Users
INSERT INTO users (email, password_hash, phone, role, status, full_name)
SELECT email, password_hash, phone, role, status, full_name
FROM (VALUES
    ('customer01@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0919000001', 'CUSTOMER', 'ACTIVE', 'Khách Một'),
    ('customer02@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0919000002', 'CUSTOMER', 'ACTIVE', 'Khách Hai'),
    ('customer03@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0919000003', 'CUSTOMER', 'ACTIVE', 'Khách Ba'),
    ('customer04@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0919000004', 'CUSTOMER', 'ACTIVE', 'Khách Bốn'),
    ('customer05@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0919000005', 'CUSTOMER', 'ACTIVE', 'Khách Năm')
) AS v(email, password_hash, phone, role, status, full_name)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = v.email);

-- 6. Seed Active Staff Users
INSERT INTO users (email, password_hash, phone, role, status, full_name)
SELECT email, password_hash, phone, role, status, full_name
FROM (VALUES
    ('staff01@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0929000001', 'STAFF', 'ACTIVE', 'Nhân Viên Một'),
    ('staff02@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0929000002', 'STAFF', 'ACTIVE', 'Nhân Viên Hai'),
    ('staff03@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0929000003', 'STAFF', 'ACTIVE', 'Nhân Viên Ba'),
    ('staff04@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0929000004', 'STAFF', 'ACTIVE', 'Nhân Viên Bốn'),
    ('staff05@demo.com', '$2a$10$BQCthYGJ5LA3C3PaK9WqC.Hg14/ABgM.nVwWmQH4AyIq4FPz/nBAC', '0929000005', 'STAFF', 'ACTIVE', 'Nhân Viên Năm')
) AS v(email, password_hash, phone, role, status, full_name)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = v.email);

-- 7. Seed Subscriptions for Admins
INSERT INTO subscriptions (admin_id, plan_id, max_courts, max_branches, start_date, end_date)
SELECT u.id, sp.id, sp.max_courts, sp.max_branches, '2026-01-01'::date, '2027-01-01'::date
FROM users u
CROSS JOIN (
    SELECT id, max_courts, max_branches FROM subscription_plans WHERE name = 'Chuyên nghiệp' LIMIT 1
) sp
WHERE u.role = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM subscriptions WHERE admin_id = u.id);

-- 8. Seed 25 Branches
INSERT INTO branches (admin_id, name, address, ward, city, phone, open_time, close_time, bank_account_number, bank_account_name, bank_name, bank_qr_image_url, status)
SELECT 
    (SELECT id FROM users WHERE email = v.admin_email LIMIT 1),
    v.name, v.address, v.ward, v.city, v.phone, 
    v.open_time::time, v.close_time::time, 
    v.bank_account_number, v.bank_account_name, v.bank_name, v.bank_qr_image_url, 
    'ACTIVE'
FROM (VALUES
    -- Hanoi Branches
    ('owner01@demo.com', 'Sân Cầu Lông Ba Đình', 'Số 5 Hoàng Diệu', 'Quán Thánh', 'Hà Nội', '0901234001', '06:00', '22:00', '10123456789', 'NGUYEN VAN ONE', 'Vietcombank', 'https://example.com/qr1.jpg'),
    ('owner02@demo.com', 'Sân Pickleball Cầu Giấy', 'Số 12 Dịch Vọng Hậu', 'Dịch Vọng Hậu', 'Hà Nội', '0901234002', '06:00', '22:00', '20123456789', 'NGUYEN VAN TWO', 'Techcombank', 'https://example.com/qr2.jpg'),
    ('owner03@demo.com', 'Tennis Hoàn Kiếm', 'Số 2 Lý Thái Tổ', 'Tràng Tiền', 'Hà Nội', '0901234003', '06:00', '22:00', '30123456789', 'NGUYEN VAN THREE', 'BIDV', 'https://example.com/qr3.jpg'),
    ('owner04@demo.com', 'Sân Bóng Đá Đống Đa', 'Số 101 Đặng Văn Ngữ', 'Trung Tự', 'Hà Nội', '0901234004', '06:00', '22:00', '40123456789', 'NGUYEN VAN FOUR', 'MB Bank', 'https://example.com/qr4.jpg'),
    ('owner05@demo.com', 'CLB Cầu Lông Tây Hồ', 'Số 45 Xuân Diệu', 'Quảng An', 'Hà Nội', '0901234005', '06:00', '22:00', '50123456789', 'NGUYEN VAN FIVE', 'ACB', 'https://example.com/qr5.jpg'),
    ('owner01@demo.com', 'Sân Pickleball Hà Đông', 'Số 8 Quang Trung', 'Nguyễn Trãi', 'Hà Nội', '0901234006', '06:00', '22:00', '10123456789', 'NGUYEN VAN ONE', 'Vietcombank', 'https://example.com/qr1.jpg'),
    ('owner02@demo.com', 'Sân Cầu Lông Thanh Xuân', 'Số 19 Lê Văn Lương', 'Nhân Chính', 'Hà Nội', '0901234007', '06:00', '22:00', '20123456789', 'NGUYEN VAN TWO', 'Techcombank', 'https://example.com/qr2.jpg'),
    ('owner03@demo.com', 'Sân Bóng Rổ Hai Bà Trưng', 'Số 9 Tạ Quang Bửu', 'Bách Khoa', 'Hà Nội', '0901234008', '06:00', '22:00', '30123456789', 'NGUYEN VAN THREE', 'BIDV', 'https://example.com/qr3.jpg'),

    -- Da Nang Branches
    ('owner04@demo.com', 'Pickleball Hải Châu', 'Số 50 Bạch Đằng', 'Hải Châu I', 'Đà Nẵng', '0901234009', '06:00', '22:00', '40123456789', 'NGUYEN VAN FOUR', 'MB Bank', 'https://example.com/qr4.jpg'),
    ('owner05@demo.com', 'Sân Cầu Lông Thanh Khê', 'Số 120 Điện Biên Phủ', 'Chính Gián', 'Đà Nẵng', '0901234010', '06:00', '22:00', '50123456789', 'NGUYEN VAN FIVE', 'ACB', 'https://example.com/qr5.jpg'),
    ('owner01@demo.com', 'CLB Tennis Sơn Trà', 'Số 85 Võ Nguyên Giáp', 'Phước Mỹ', 'Đà Nẵng', '0901234011', '06:00', '22:00', '10123456789', 'NGUYEN VAN ONE', 'Vietcombank', 'https://example.com/qr1.jpg'),
    ('owner02@demo.com', 'Mini Football Ngũ Hành Sơn', 'Số 20 Ngô Quyền', 'An Hải Đông', 'Đà Nẵng', '0901234012', '06:00', '22:00', '20123456789', 'NGUYEN VAN TWO', 'Techcombank', 'https://example.com/qr2.jpg'),
    ('owner03@demo.com', 'Sân Bóng Rổ Liên Chiểu', 'Số 55 Tôn Đức Thắng', 'Hòa Khánh Nam', 'Đà Nẵng', '0901234013', '06:00', '22:00', '30123456789', 'NGUYEN VAN THREE', 'BIDV', 'https://example.com/qr3.jpg'),
    ('owner04@demo.com', 'Sân Cầu Lông Cẩm Lệ', 'Số 90 Cách Mạng Tháng 8', 'Khuê Trung', 'Đà Nẵng', '0901234014', '06:00', '22:00', '40123456789', 'NGUYEN VAN FOUR', 'MB Bank', 'https://example.com/qr4.jpg'),

    -- Ho Chi Minh Branches
    ('owner05@demo.com', 'Sân Pickleball Quận 1', 'Số 10 Nguyễn Huệ', 'Bến Nghé', 'Hồ Chí Minh', '0901234015', '06:00', '22:00', '50123456789', 'NGUYEN VAN FIVE', 'ACB', 'https://example.com/qr5.jpg'),
    ('owner01@demo.com', 'CLB Cầu Lông Quận 3', 'Số 200 Nguyễn Đình Chiểu', 'Võ Thị Sáu', 'Hồ Chí Minh', '0901234016', '06:00', '22:00', '10123456789', 'NGUYEN VAN ONE', 'Vietcombank', 'https://example.com/qr1.jpg'),
    ('owner02@demo.com', 'Sân Tennis Bình Thạnh', 'Số 35 Điện Biên Phủ', 'Phường 15', 'Hồ Chí Minh', '0901234017', '06:00', '22:00', '20123456789', 'NGUYEN VAN TWO', 'Techcombank', 'https://example.com/qr2.jpg'),
    ('owner03@demo.com', 'Mini Football Quận 7', 'Số 105 Nguyễn Văn Linh', 'Tân Phong', 'Hồ Chí Minh', '0901234018', '06:00', '22:00', '30123456789', 'NGUYEN VAN THREE', 'BIDV', 'https://example.com/qr3.jpg'),
    ('owner04@demo.com', 'Sân Bóng Rổ Thủ Đức', 'Số 50 Võ Văn Ngân', 'Linh Chiểu', 'Hồ Chí Minh', '0901234019', '06:00', '22:00', '40123456789', 'NGUYEN VAN FOUR', 'MB Bank', 'https://example.com/qr4.jpg'),
    ('owner05@demo.com', 'CLB Cầu Lông Tân Bình', 'Số 80 Trường Chinh', 'Phường 12', 'Hồ Chí Minh', '0901234020', '06:00', '22:00', '50123456789', 'NGUYEN VAN FIVE', 'ACB', 'https://example.com/qr5.jpg'),
    ('owner01@demo.com', 'Sân Pickleball Gò Vấp', 'Số 15 Quang Trung', 'Phường 10', 'Hồ Chí Minh', '0901234021', '06:00', '22:00', '10123456789', 'NGUYEN VAN ONE', 'Vietcombank', 'https://example.com/qr1.jpg'),
    ('owner02@demo.com', 'Sân Cầu Lông Quận 10', 'Số 30 Thành Thái', 'Phường 14', 'Hồ Chí Minh', '0901234022', '06:00', '22:00', '20123456789', 'NGUYEN VAN TWO', 'Techcombank', 'https://example.com/qr2.jpg'),
    ('owner03@demo.com', 'Sân Pickleball Bình Tân', 'Số 210 Tên Lửa', 'Bình Trị Đông B', 'Hồ Chí Minh', '0901234023', '06:00', '22:00', '30123456789', 'NGUYEN VAN THREE', 'BIDV', 'https://example.com/qr3.jpg'),
    ('owner04@demo.com', 'CLB Cầu Lông Phú Nhuận', 'Số 40 Phùng Văn Cung', 'Phường 7', 'Hồ Chí Minh', '0901234024', '06:00', '22:00', '40123456789', 'NGUYEN VAN FOUR', 'MB Bank', 'https://example.com/qr4.jpg'),
    ('owner05@demo.com', 'Sân Pickleball Nhà Bè', 'Số 95 Nguyễn Hữu Thọ', 'Phước Kiển', 'Hồ Chí Minh', '0901234025', '06:00', '22:00', '50123456789', 'NGUYEN VAN FIVE', 'ACB', 'https://example.com/qr5.jpg')
) AS v(admin_email, name, address, ward, city, phone, open_time, close_time, bank_account_number, bank_account_name, bank_name, bank_qr_image_url)
WHERE NOT EXISTS (SELECT 1 FROM branches WHERE name = v.name);

-- 9. Seed Staff Mappings
INSERT INTO staff (user_id, branch_id)
SELECT u.id, b.id
FROM users u
JOIN branches b ON b.name = CASE 
    WHEN u.email = 'staff01@demo.com' THEN 'Sân Cầu Lông Ba Đình'
    WHEN u.email = 'staff02@demo.com' THEN 'Sân Pickleball Cầu Giấy'
    WHEN u.email = 'staff03@demo.com' THEN 'Pickleball Hải Châu'
    WHEN u.email = 'staff04@demo.com' THEN 'Sân Pickleball Quận 1'
    WHEN u.email = 'staff05@demo.com' THEN 'CLB Cầu Lông Quận 3'
END
WHERE u.role = 'STAFF'
  AND NOT EXISTS (SELECT 1 FROM staff WHERE user_id = u.id);

-- 10. Seed 27 Courts under Branches
INSERT INTO courts (branch_id, name, court_type_id, description, image_url, status)
SELECT 
    b.id,
    c.court_name,
    ct.id,
    c.description,
    c.image_url,
    'ACTIVE'
FROM branches b
CROSS JOIN (
    VALUES
        ('Sân Cầu Lông Ba Đình', 'Sân số 1', 'Cầu lông', 'Thảm chuyên dụng Yonex', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Cầu Lông Ba Đình', 'Sân số 2', 'Cầu lông', 'Thảm chuyên dụng Yonex', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Pickleball Cầu Giấy', 'Sân số 1', 'Pickleball', 'Sân tiêu chuẩn ngoài trời', 'https://images.unsplash.com/photo-1601647998802-984400c40683?w=500&auto=format&fit=crop'),
        ('Sân Pickleball Cầu Giấy', 'Sân số 2', 'Pickleball', 'Sân tiêu chuẩn ngoài trời', 'https://images.unsplash.com/photo-1601647998802-984400c40683?w=500&auto=format&fit=crop'),
        ('Tennis Hoàn Kiếm', 'Sân số 1', 'Tennis', 'Sân đất nện chất lượng cao', 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=500&auto=format&fit=crop'),
        ('Sân Bóng Đá Đống Đa', 'Sân số 1', 'Bóng đá mini', 'Sân cỏ nhân tạo 5 người', 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop'),
        ('CLB Cầu Lông Tây Hồ', 'Sân số 1', 'Cầu lông', 'Sân thảm gỗ chống trơn', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Pickleball Hà Đông', 'Sân số 1', 'Pickleball', 'Sân trong nhà có mái che', 'https://images.unsplash.com/photo-1601647998802-984400c40683?w=500&auto=format&fit=crop'),
        ('Sân Cầu Lông Thanh Xuân', 'Sân số 1', 'Cầu lông', 'Thảm tiêu chuẩn thi đấu', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Bóng Rổ Hai Bà Trưng', 'Sân số 1', 'Bóng rổ', 'Sân gỗ thi đấu', 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=500&auto=format&fit=crop'),
        ('Pickleball Hải Châu', 'Sân số 1', 'Pickleball', 'Sân ven sông lộng gió', 'https://images.unsplash.com/photo-1601647998802-984400c40683?w=500&auto=format&fit=crop'),
        ('Sân Cầu Lông Thanh Khê', 'Sân số 1', 'Cầu lông', 'Sân thảm cao cấp', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('CLB Tennis Sơn Trà', 'Sân số 1', 'Tennis', 'Sân cứng tiêu chuẩn', 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=500&auto=format&fit=crop'),
        ('Mini Football Ngũ Hành Sơn', 'Sân số 1', 'Bóng đá mini', 'Cỏ nhân tạo chất lượng tốt', 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop'),
        ('Sân Bóng Rổ Liên Chiểu', 'Sân số 1', 'Bóng rổ', 'Sân bóng rổ ngoài trời', 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=500&auto=format&fit=crop'),
        ('Sân Cầu Lông Cẩm Lệ', 'Sân số 1', 'Cầu lông', 'Sân thảm tập luyện', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Pickleball Quận 1', 'Sân số 1', 'Pickleball', 'Sân trung tâm quận 1', 'https://images.unsplash.com/photo-1601647998802-984400c40683?w=500&auto=format&fit=crop'),
        ('CLB Cầu Lông Quận 3', 'Sân số 1', 'Cầu lông', 'Thảm Yonex chính hãng', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Tennis Bình Thạnh', 'Sân số 1', 'Tennis', 'Sân trong nhà mát mẻ', 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=500&auto=format&fit=crop'),
        ('Mini Football Quận 7', 'Sân số 1', 'Bóng đá mini', 'Sân 7 người tiêu chuẩn', 'https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=500&auto=format&fit=crop'),
        ('Sân Bóng Rổ Thủ Đức', 'Sân số 1', 'Bóng rổ', 'Sân bóng rổ chất lượng', 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=500&auto=format&fit=crop'),
        ('CLB Cầu Lông Tân Bình', 'Sân số 1', 'Cầu lông', 'Sân thảm giá rẻ', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Pickleball Gò Vấp', 'Sân số 1', 'Pickleball', 'Sân bóng rổ kết hợp', 'https://images.unsplash.com/photo-1601647998802-984400c40683?w=500&auto=format&fit=crop'),
        ('Sân Cầu Lông Quận 10', 'Sân số 1', 'Cầu lông', 'Sân thảm chất lượng', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Pickleball Bình Tân', 'Sân số 1', 'Pickleball', 'Sân pickleball trong nhà', 'https://images.unsplash.com/photo-1601647998802-984400c40683?w=500&auto=format&fit=crop'),
        ('CLB Cầu Lông Phú Nhuận', 'Sân số 1', 'Cầu lông', 'Sân thảm thoáng mát', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=500&auto=format&fit=crop'),
        ('Sân Pickleball Nhà Bè', 'Sân số 1', 'Pickleball', 'Sân ngoài trời thoáng đãng', 'https://images.unsplash.com/photo-1601647998802-984400c40683?w=500&auto=format&fit=crop')
) AS c(branch_name, court_name, type_name, description, image_url)
JOIN court_types ct ON ct.name = c.type_name
WHERE b.name = c.branch_name
  AND NOT EXISTS (
      SELECT 1 FROM courts 
      WHERE branch_id = b.id AND name = c.court_name
  );

-- 11. Seed Time Slot Templates (Cross Join to generate 8 slots * 7 days for every court)
INSERT INTO time_slot_templates (court_id, start_time, end_time, price, day_of_week, is_active)
SELECT 
    c.id as court_id,
    t.start_time::time,
    (t.start_time::time + interval '2 hours')::time as end_time,
    CASE 
        WHEN t.start_time >= '17:00' THEN 150000.00
        WHEN t.start_time <= '08:00' THEN 80000.00
        ELSE 100000.00
    END as price,
    dow.day_of_week as day_of_week,
    true as is_active
FROM courts c
CROSS JOIN (
    VALUES 
        ('06:00'), ('08:00'), ('10:00'), ('13:00'), ('15:00'), ('17:00'), ('19:00'), ('21:00')
) AS t(start_time)
CROSS JOIN (
    SELECT generate_series(0, 6) AS day_of_week
) AS dow
WHERE NOT EXISTS (
    SELECT 1 FROM time_slot_templates 
    WHERE court_id = c.id 
      AND day_of_week = dow.day_of_week 
      AND start_time = t.start_time::time
);
