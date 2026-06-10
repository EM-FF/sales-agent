-- 在 db/data.sql 最顶部加上这四行，每次启动先清洗掉旧测试数据，再重新插入
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sa_sales_order;
TRUNCATE TABLE sa_product;
TRUNCATE TABLE sa_sales_rep;
TRUNCATE TABLE sa_sales_region;
SET FOREIGN_KEY_CHECKS = 1;
-- 大区
INSERT INTO sa_sales_region (id, name) VALUES
                                           (1, '华东区'),
                                           (2, '华南区'),
                                           (3, '华北区'),
                                           (4, '西南区');

-- 销售员（每区 3 人，含 1 名主管）
INSERT INTO sa_sales_rep (id, name, region_id, role, email) VALUES
-- 华东区
(1,  '李明',   1, 'SALES_MANAGER', 'liming@jichi.com'),
(2,  '张伟',   1, 'SALES_REP',     'zhangwei@jichi.com'),
(3,  '王芳',   1, 'SALES_REP',     'wangfang@jichi.com'),
-- 华南区
(4,  '陈强',   2, 'SALES_MANAGER', 'chenqiang@jichi.com'),
(5,  '刘洋',   2, 'SALES_REP',     'liuyang@jichi.com'),
(6,  '赵雪',   2, 'SALES_REP',     'zhaoxue@jichi.com'),
-- 华北区
(7,  '孙磊',   3, 'SALES_MANAGER', 'sunlei@jichi.com'),
(8,  '张磊',   3, 'SALES_REP',     'zhanglei@jichi.com'),
(9,  '周丽',   3, 'SALES_REP',     'zhouli@jichi.com'),
-- 西南区
(10, '吴刚',   4, 'SALES_MANAGER', 'wugang@jichi.com'),
(11, '郑华',   4, 'SALES_REP',     'zhenghua@jichi.com'),
(12, '林敏',   4, 'SALES_REP',     'linmin@jichi.com'),
-- 总监（全国）
(13, '黄总',   1, 'SALES_DIRECTOR','huang@jichi.com');

-- 产品（4 个品类，20 个 SKU）
INSERT INTO sa_product (id, sku_code, name, category, unit_price, cost, status) VALUES
-- 数码产品（高客单价）
(1,  'SKU-1001', '华为 Mate 60 Pro 手机',    '数码产品', 6999.00, 4200.00, 'ACTIVE'),
(2,  'SKU-1002', '苹果 iPhone 15 手机',       '数码产品', 7999.00, 5100.00, 'ACTIVE'),
(3,  'SKU-1003', '联想 ThinkPad X1 笔记本',  '数码产品', 9999.00, 6800.00, 'ACTIVE'),
(4,  'SKU-1004', '索尼 WH-1000XM5 耳机',     '数码产品', 2299.00, 1100.00, 'ACTIVE'),
(5,  'SKU-1005', '小米 14 Ultra 手机',        '数码产品', 5999.00, 3600.00, 'ACTIVE'),
-- 数码（含异常SKU）
(6,  'SKU-8821', '智能手表 Pro',              '数码产品', 1299.00,  650.00, 'ACTIVE'),
-- 家用电器（中高客单价）
(7,  'SKU-2001', '戴森 V15 吸尘器',           '家用电器', 4990.00, 2800.00, 'ACTIVE'),
(8,  'SKU-2002', '西门子洗碗机',              '家用电器', 5999.00, 3500.00, 'ACTIVE'),
(9,  'SKU-2003', '美的空调 1.5P',             '家用电器', 3299.00, 1900.00, 'ACTIVE'),
(10, 'SKU-2004', '苏泊尔电饭煲',              '家用电器',  599.00,  280.00, 'ACTIVE'),
(11, 'SKU-2005', '飞利浦空气净化器',          '家用电器', 2199.00, 1200.00, 'ACTIVE'),
-- 服装配饰（低客单价、量大）
(12, 'SKU-3001', '耐克 Air Max 运动鞋',      '服装配饰',  899.00,  420.00, 'ACTIVE'),
(13, 'SKU-3002', '优衣库羊绒大衣',            '服装配饰',  799.00,  350.00, 'ACTIVE'),
(14, 'SKU-3003', '阿迪达斯运动套装',          '服装配饰',  699.00,  310.00, 'ACTIVE'),
(15, 'SKU-3004', '蔻驰女包',                 '服装配饰', 2599.00, 1100.00, 'ACTIVE'),
-- 其他
(16, 'SKU-4001', '得力文具套装',              '其他',       99.00,   40.00, 'ACTIVE'),
(17, 'SKU-4002', '金融理财书籍套装',          '其他',      299.00,  120.00, 'ACTIVE'),
(18, 'SKU-4003', '瑜伽垫专业版',              '其他',      399.00,  160.00, 'ACTIVE'),
(19, 'SKU-4004', '咖啡机胶囊套装',            '其他',      699.00,  280.00, 'ACTIVE'),
(20, 'SKU-4005', '护肤品礼盒',               '其他',      899.00,  350.00, 'ACTIVE');

INSERT INTO sa_sales_order (order_no, rep_id, product_id, region_id, customer_name,
                            quantity, unit_price, amount, cost, profit, status, order_date) VALUES

-- ============================================================
-- B01：约 7 个月前（正常基线）
-- ============================================================
('ORD-B01-001', 2, 1,  1, '上海某科技公司',   2, 6999.00,  13998.00,  8400.00,  5598.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 205 DAY)),
('ORD-B01-002', 3, 12, 1, '南京运动装备店',  10,  899.00,   8990.00,  4200.00,  4790.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 202 DAY)),
-- ... 更多历史数据（共 10 条，覆盖各区）

-- ============================================================
-- B03：约 5 个月前（旺季高峰）
-- ============================================================
('ORD-B03-004', 8, 3,  3, '北京某企业采购', 12, 9999.00, 119988.00, 81600.00, 38388.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 133 DAY)),
-- ...

-- ============================================================
-- B05：约 3 个月前（张磊开始掉量，SKU-8821 正常）
-- ============================================================
-- 张磊只有 1 单（断崖开始）
('ORD-B05-003', 8, 9,  3, '北京某单位',       3, 3299.00,   9897.00,  5700.00,  4197.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 74 DAY)),
-- SKU-8821 正常出单
('ORD-B05-007', 2, 6,  1, '杭州数码店',      12, 1299.00,  15588.00,  7800.00,  7788.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 66 DAY)),
-- 王芳退单
('ORD-B05-010', 3, 15, 1, '上海奢品代理',     1, 2599.00,   2599.00,  1100.00,  1499.00, 'REFUNDED',  DATE_SUB(CURDATE(), INTERVAL 60 DAY)),

-- ============================================================
-- B06：约 50~20 天前（华北区最后一批；SKU-8821 最后一单）
-- ============================================================
-- SKU-8821 最后一笔（30天前，之后持续零销售 → 断货预警）
('ORD-B06-006', 2, 6,  1, '义乌批发市场',    10, 1299.00,  12990.00,  6500.00,  6490.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 30 DAY)),
-- 华北区最后一批（20天前，之后断单 → 近2周暴跌预警）
('ORD-B06-008', 7, 3,  3, '北京某企业',       2, 9999.00,  19998.00, 13600.00,  6398.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 22 DAY)),
('ORD-B06-009', 9, 4,  3, '天津批发商',       5, 2299.00,  11495.00,  5500.00,  5995.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 20 DAY)),

-- ============================================================
-- B07：近 14 天（其他区正常，华北区和 SKU-8821 刻意留空）
-- ============================================================
('ORD-B07-001', 2, 2,  1, '上海苹果授权2',    5, 7999.00,  39995.00, 25500.00, 14495.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 14 DAY)),
('ORD-B07-006', 2, 1,  1, '杭州旗舰门店',     3, 6999.00,  20997.00, 12600.00,  8397.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 4 DAY)),
('ORD-B07-007', 5, 2,  2, '广州Apple授权',    2, 7999.00,  15998.00, 10200.00,  5798.00, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 2 DAY));
-- 华北区（region_id=3）近 14 天无订单 → 暴跌预警
-- SKU-8821（product_id=6）近 30 天无订单 → 断货预警

select * from sa_sales_order;

-- 各大区订单数量
SELECT r.name, COUNT(*) AS order_count, SUM(o.amount) AS total_amount
FROM sa_sales_order o
         JOIN sa_sales_region r ON o.region_id = r.id
GROUP BY r.id, r.name
ORDER BY total_amount DESC;

-- 近7个月月度趋势（动态，始终显示最近7个月）
SELECT DATE_FORMAT(order_date, '%Y-%m') AS month,
       COUNT(*) AS orders,
       SUM(amount) AS total
FROM sa_sales_order
WHERE status = 'COMPLETED'
  AND order_date >= DATE_SUB(CURDATE(), INTERVAL 210 DAY)
GROUP BY month
ORDER BY month;

-- 验证异常预埋：华北区近2周订单数
SELECT COUNT(*) FROM sa_sales_order
WHERE region_id = 3
  AND order_date >= DATE_SUB(CURDATE(), INTERVAL 14 DAY);
-- 期望结果：0（华北区近2周无订单，异常！）

-- 验证 SKU-8821 零销售预警
SELECT COUNT(*) FROM sa_sales_order
WHERE product_id = 6
  AND order_date >= DATE_SUB(CURDATE(), INTERVAL 14 DAY);
-- 期望结果：0（SKU-8821 近30天内零销售，预警工具应检测到）

-- 验证张磊近期掉量
SELECT COUNT(*) FROM sa_sales_order
WHERE rep_id = 8
  AND order_date >= DATE_SUB(CURDATE(), INTERVAL 60 DAY);
-- 期望结果：1（仅1单，对比历史正常期明显异常）