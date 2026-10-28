-- 通用后台底座 admin_base 初始化脚本(MySQL 8/9)
CREATE DATABASE IF NOT EXISTS admin_base DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE admin_base;

DROP TABLE IF EXISTS sys_user_post, sys_user_role, sys_role_menu, sys_oper_log, sys_login_log,
  sys_config, sys_dict_data, sys_dict_type, sys_post, sys_menu, sys_role, sys_user, sys_dept;

CREATE TABLE sys_dept (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT NOT NULL DEFAULT 0,
  ancestors VARCHAR(200) NOT NULL DEFAULT '0',
  name VARCHAR(64) NOT NULL,
  leader_name VARCHAR(32) NULL,
  phone VARCHAR(32) NULL,
  email VARCHAR(64) NULL,
  sort INT NOT NULL DEFAULT 0,
  status CHAR(1) NOT NULL DEFAULT '0',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '部门表';

CREATE TABLE sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dept_id BIGINT NULL,
  account VARCHAR(32) NOT NULL,
  nick_name VARCHAR(32) NOT NULL,
  email VARCHAR(64) NULL,
  phone VARCHAR(20) NULL,
  sex CHAR(1) DEFAULT '0',
  avatar VARCHAR(200) NULL,
  password VARCHAR(100) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  del_flag CHAR(1) NOT NULL DEFAULT '0',
  login_ip VARCHAR(64) NULL,
  login_time DATETIME NULL,
  remark VARCHAR(200) NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_account (account),
  KEY idx_dept (dept_id)
) COMMENT '用户表';

CREATE TABLE sys_post (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(32) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  status CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(200) NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code)
) COMMENT '岗位表';

CREATE TABLE sys_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(32) NOT NULL,
  code VARCHAR(32) NOT NULL,
  sort INT NOT NULL DEFAULT 0,
  data_scope TINYINT NOT NULL DEFAULT 1 COMMENT '1全部 2自定义 3本部门 4本部门及以下 5仅本人',
  status CHAR(1) NOT NULL DEFAULT '0',
  builtin TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(200) NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code)
) COMMENT '角色表';

CREATE TABLE sys_menu (
  id BIGINT PRIMARY KEY,
  parent_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(32) NOT NULL,
  type CHAR(1) NOT NULL COMMENT 'M目录 C页面 F按钮',
  path VARCHAR(64) NULL,
  component VARCHAR(64) NULL,
  perm VARCHAR(64) NULL,
  icon VARCHAR(32) NULL,
  sort INT NOT NULL DEFAULT 0,
  visible CHAR(1) NOT NULL DEFAULT '0',
  status CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(200) NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '菜单表';

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色';

CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, menu_id)
) COMMENT '角色菜单';

CREATE TABLE sys_user_post (
  user_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, post_id)
) COMMENT '用户岗位';

CREATE TABLE sys_dict_type (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(32) NOT NULL,
  type VARCHAR(32) NOT NULL,
  status CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(200) NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_type (type)
) COMMENT '字典类型';

CREATE TABLE sys_dict_data (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  type VARCHAR(32) NOT NULL,
  label VARCHAR(32) NOT NULL,
  value VARCHAR(32) NOT NULL,
  tag VARCHAR(16) NULL,
  sort INT NOT NULL DEFAULT 0,
  is_default TINYINT NOT NULL DEFAULT 0,
  status CHAR(1) NOT NULL DEFAULT '0',
  remark VARCHAR(200) NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_type (type)
) COMMENT '字典数据';

CREATE TABLE sys_config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_name VARCHAR(32) NOT NULL DEFAULT '通用',
  name VARCHAR(64) NOT NULL,
  `key` VARCHAR(64) NOT NULL,
  value VARCHAR(200) NULL,
  val_type VARCHAR(16) NOT NULL DEFAULT 'text' COMMENT 'text/switch/select/pass',
  options VARCHAR(200) NULL,
  builtin TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(200) NULL,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_key (`key`)
) COMMENT '参数配置';

CREATE TABLE sys_oper_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  module VARCHAR(32) NULL,
  action VARCHAR(32) NULL,
  method VARCHAR(10) NULL,
  uri VARCHAR(200) NULL,
  oper_account VARCHAR(32) NULL,
  oper_name VARCHAR(32) NULL,
  ip VARCHAR(64) NULL,
  zone VARCHAR(16) NULL,
  result TINYINT NOT NULL DEFAULT 0 COMMENT '0成功 1失败',
  err_msg VARCHAR(500) NULL,
  cost_ms BIGINT NULL,
  params TEXT NULL,
  oper_time DATETIME NOT NULL,
  KEY idx_time (oper_time)
) COMMENT '操作日志';

CREATE TABLE sys_login_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  account VARCHAR(32) NULL,
  ip VARCHAR(64) NULL,
  zone VARCHAR(16) NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0成功 1失败',
  msg VARCHAR(100) NULL,
  login_time DATETIME NOT NULL,
  KEY idx_time (login_time)
) COMMENT '登录日志';

-- ============ 种子数据 ============

INSERT INTO sys_dept (id, parent_id, ancestors, name, leader_name, phone, email, sort) VALUES
(1, 0, '0', '酷炫科技集团', '周明远', '010-8800-1000', 'hq@yunyuan.com', 1),
(2, 1, '0,1', '研发中心', '陈思远', '010-8800-1001', 'rd@yunyuan.com', 1),
(3, 1, '0,1', '产品中心', '赵硕', '010-8800-1002', 'pd@yunyuan.com', 2),
(4, 1, '0,1', '职能中心', '钱佳', '010-8800-1003', 'fn@yunyuan.com', 3),
(5, 2, '0,1,2', '平台架构部', '李维', '010-8800-1011', 'arch@yunyuan.com', 1),
(6, 2, '0,1,2', '应用开发部', '郭阳', '010-8800-1012', 'app@yunyuan.com', 2),
(7, 2, '0,1,2', '测试质量部', '韩雪', '010-8800-1013', 'qa@yunyuan.com', 3),
(8, 3, '0,1,3', '产品设计部', '孙浩', '010-8800-1021', 'design@yunyuan.com', 1),
(9, 3, '0,1,3', '用户研究部', '冯婷', '010-8800-1022', 'ur@yunyuan.com', 2),
(10, 4, '0,1,4', '人力资源部', '钱佳', '010-8800-1031', 'hr@yunyuan.com', 1),
(11, 4, '0,1,4', '财务部', '袁倩', '010-8800-1032', 'fin@yunyuan.com', 2);

SET @pwd = '$2a$10$RWw8ug40FPneHqotvK/w0egzWwuU6i0aq1HUPPrPa382jPgEjMeLW'; -- admin123

INSERT INTO sys_user (id, dept_id, account, nick_name, email, phone, sex, password, status, remark, create_time) VALUES
(1, 5, 'admin', '周明远', 'admin@yunyuan.com', '13800008800', '0', @pwd, '0', '技术总监', '2026-08-01 09:00:00'),
(2, 5, 'chensy', '陈思远', 'chensy@yunyuan.com', '13900022100', '1', @pwd, '0', '架构师', '2026-08-01 09:05:00'),
(3, 6, 'wumin', '吴敏', 'wumin@yunyuan.com', '13700033101', '0', @pwd, '0', '研发工程师', '2026-08-02 10:00:00'),
(4, 5, 'liwei', '李维', 'liwei@yunyuan.com', '13700033102', '0', @pwd, '0', '架构师', '2026-08-02 10:10:00'),
(5, 3, 'zhaoshuo', '赵硕', 'zhaoshuo@yunyuan.com', '13600044100', '0', @pwd, '0', '产品负责人', '2026-08-03 11:00:00'),
(6, 7, 'hanxue', '韩雪', 'hanxue@yunyuan.com', '13600044101', '1', @pwd, '0', '测试工程师', '2026-08-03 11:20:00'),
(7, 8, 'sunhao', '孙浩', 'sunhao@yunyuan.com', '13500055100', '0', @pwd, '0', '产品设计师', '2026-08-05 14:00:00'),
(8, 9, 'fengting', '冯婷', 'fengting@yunyuan.com', '13500055101', '1', @pwd, '0', '用户研究员', '2026-08-05 14:30:00'),
(9, 6, 'guoyang', '郭阳', 'guoyang@yunyuan.com', '13400066100', '0', @pwd, '0', '开发工程师', '2026-08-08 09:40:00'),
(10, 10, 'qianjia', '钱佳', 'qianjia@yunyuan.com', '13400066101', '1', @pwd, '0', 'HRBP', '2026-08-10 15:00:00'),
(11, 11, 'yuanqian', '袁倩', 'yuanqian@yunyuan.com', '13300077100', '1', @pwd, '0', '会计', '2026-08-12 16:00:00'),
(12, 4, 'guest', '访客', 'guest@yunyuan.com', '13300077101', '0', @pwd, '0', '体验账号', '2026-09-01 10:00:00');

INSERT INTO sys_post (id, code, name, sort, remark) VALUES
(1, 'cto', '技术总监', 1, NULL),
(2, 'arch', '架构师', 2, NULL),
(3, 'dev', '研发工程师', 3, NULL),
(4, 'pm', '产品经理', 4, NULL),
(5, 'qa', '测试工程师', 5, NULL),
(6, 'design', '设计师', 6, NULL),
(7, 'hr', '人力资源', 7, NULL),
(8, 'fin', '财务', 8, NULL);

INSERT INTO sys_role (id, name, code, sort, data_scope, status, builtin, remark) VALUES
(1, '超级管理员', 'super_admin', 1, 1, '0', 1, '内置角色,自动拥有全部菜单与按钮,不受授权勾选影响'),
(2, '系统管理员', 'sys_admin', 2, 1, '0', 0, '负责系统配置与日常运维'),
(3, '业务管理员', 'biz_admin', 3, 4, '0', 0, '管理本部门及下级的业务数据'),
(4, '研发工程师', 'dev_engineer', 4, 5, '0', 0, '仅查看本人数据'),
(5, '审计专员', 'audit_officer', 5, 3, '0', 0, '审计本部门操作与登录记录'),
(6, '访客', 'guest', 6, 5, '0', 0, '只读体验账号');

INSERT INTO sys_menu (id, parent_id, name, type, path, component, perm, icon, sort) VALUES
(100, 0, '工作台', 'C', 'dashboard', 'dashboard/index', 'dashboard:view', 'dashboard', 1),
(200, 0, '系统管理', 'M', 'system', NULL, NULL, 'system', 2),
(201, 200, '用户管理', 'C', 'user', 'system/user', 'system:user:list', 'user', 1),
(2011, 201, '用户查询', 'F', NULL, NULL, 'system:user:query', NULL, 1),
(2012, 201, '用户新增', 'F', NULL, NULL, 'system:user:add', NULL, 2),
(2013, 201, '用户修改', 'F', NULL, NULL, 'system:user:edit', NULL, 3),
(2014, 201, '用户删除', 'F', NULL, NULL, 'system:user:remove', NULL, 4),
(2015, 201, '重置密码', 'F', NULL, NULL, 'system:user:resetPwd', NULL, 5),
(2016, 201, '分配角色', 'F', NULL, NULL, 'system:user:assignRole', NULL, 6),
(202, 200, '角色管理', 'C', 'role', 'system/role', 'system:role:list', 'role', 2),
(2021, 202, '角色新增', 'F', NULL, NULL, 'system:role:add', NULL, 1),
(2022, 202, '角色修改', 'F', NULL, NULL, 'system:role:edit', NULL, 2),
(2023, 202, '角色删除', 'F', NULL, NULL, 'system:role:remove', NULL, 3),
(2024, 202, '分配权限', 'F', NULL, NULL, 'system:role:perm', NULL, 4),
(203, 200, '菜单管理', 'C', 'menu', 'system/menu', 'system:menu:list', 'menu', 3),
(2031, 203, '菜单新增', 'F', NULL, NULL, 'system:menu:add', NULL, 1),
(2032, 203, '菜单修改', 'F', NULL, NULL, 'system:menu:edit', NULL, 2),
(2033, 203, '菜单删除', 'F', NULL, NULL, 'system:menu:remove', NULL, 3),
(204, 200, '部门管理', 'C', 'dept', 'system/dept', 'system:dept:list', 'dept', 4),
(2041, 204, '部门新增', 'F', NULL, NULL, 'system:dept:add', NULL, 1),
(2042, 204, '部门修改', 'F', NULL, NULL, 'system:dept:edit', NULL, 2),
(2043, 204, '部门删除', 'F', NULL, NULL, 'system:dept:remove', NULL, 3),
(205, 200, '岗位管理', 'C', 'post', 'system/post', 'system:post:list', 'post', 5),
(2051, 205, '岗位新增', 'F', NULL, NULL, 'system:post:add', NULL, 1),
(2052, 205, '岗位修改', 'F', NULL, NULL, 'system:post:edit', NULL, 2),
(2053, 205, '岗位删除', 'F', NULL, NULL, 'system:post:remove', NULL, 3),
(206, 200, '字典管理', 'C', 'dict', 'system/dict', 'system:dict:list', 'dict', 6),
(2061, 206, '字典新增', 'F', NULL, NULL, 'system:dict:add', NULL, 1),
(2062, 206, '字典修改', 'F', NULL, NULL, 'system:dict:edit', NULL, 2),
(2063, 206, '字典删除', 'F', NULL, NULL, 'system:dict:remove', NULL, 3),
(207, 200, '参数配置', 'C', 'config', 'system/config', 'system:config:list', 'config', 7),
(2071, 207, '参数新增', 'F', NULL, NULL, 'system:config:add', NULL, 1),
(2072, 207, '参数修改', 'F', NULL, NULL, 'system:config:edit', NULL, 2),
(2073, 207, '参数删除', 'F', NULL, NULL, 'system:config:remove', NULL, 3),
(300, 0, '系统监控', 'M', 'monitor', NULL, NULL, 'monitor', 3),
(301, 300, '操作日志', 'C', 'operlog', 'monitor/operlog', 'monitor:operlog:list', 'operlog', 1),
(3011, 301, '日志删除', 'F', NULL, NULL, 'monitor:operlog:remove', NULL, 1),
(302, 300, '登录日志', 'C', 'loginlog', 'monitor/loginlog', 'monitor:loginlog:list', 'loginlog', 2),
(3021, 302, '日志删除', 'F', NULL, NULL, 'monitor:loginlog:remove', NULL, 1),
(400, 0, '系统工具', 'M', 'tool', NULL, NULL, 'tool', 4),
(401, 400, '代码生成', 'C', 'codegen', 'tool/codegen', 'tool:codegen:list', 'code', 1);

INSERT INTO sys_user_role VALUES (1, 1), (2, 2), (3, 3), (12, 6);

INSERT INTO sys_role_menu
SELECT 2, id FROM sys_menu WHERE id != 401;
INSERT INTO sys_role_menu VALUES
(3, 100), (3, 200), (3, 201), (3, 2011), (3, 2012), (3, 202), (3, 204), (3, 205), (3, 206),
(4, 100),
(5, 100), (5, 300), (5, 301), (5, 302),
(6, 100);

INSERT INTO sys_user_post VALUES
(1, 1), (2, 2), (3, 3), (4, 2), (5, 4), (6, 5), (7, 6), (8, 7), (9, 3), (10, 8), (11, 8);

INSERT INTO sys_dict_type (id, name, type, status, remark) VALUES
(1, '用户性别', 'sys_user_sex', '0', '用户性别列表'),
(2, '系统开关', 'sys_normal_disable', '0', '系统正常/停用状态'),
(3, '是否', 'sys_yes_no', '0', '通用是/否选项');

INSERT INTO sys_dict_data (type, label, value, tag, sort, is_default) VALUES
('sys_user_sex', '男', '0', 'green', 1, 1),
('sys_user_sex', '女', '1', 'blue', 2, 0),
('sys_user_sex', '未知', '2', 'gray', 3, 0),
('sys_normal_disable', '正常', '0', 'green', 1, 1),
('sys_normal_disable', '停用', '1', 'red', 2, 0),
('sys_yes_no', '是', 'Y', 'green', 1, 1),
('sys_yes_no', '否', 'N', 'gray', 2, 0);

INSERT INTO sys_config (group_name, name, `key`, value, val_type, options, builtin, remark) VALUES
('用户管理', '账号初始密码', 'sys.user.initPassword', '123456', 'pass', NULL, 1, '新用户的初始化密码'),
('账号自助', '验证码开关', 'sys.account.captchaEnabled', 'true', 'switch', NULL, 1, '是否开启登录验证码'),
('主框架', '默认外观', 'sys.index.appearance', 'light', 'select', 'light,dark,auto', 1, '可选值 light / dark / auto'),
('系统日志', '日志保留天数', 'sys.log.retentionDays', '90', 'text', NULL, 0, '超过该天数的日志可清理');

-- ============ 模拟日志(让工作台/热力图有真实数据) ============

DROP PROCEDURE IF EXISTS gen_logs;
DELIMITER $$
CREATE PROCEDURE gen_logs()
BEGIN
  DECLARE i INT DEFAULT 0;
  WHILE i < 1400 DO
    INSERT INTO sys_login_log (account, ip, zone, status, msg, login_time)
    VALUES (
      CASE WHEN RAND() < 0.55 THEN 'admin' WHEN RAND() < 0.55 THEN 'chensy' ELSE 'wumin' END,
      CASE WHEN RAND() < 0.6 THEN CONCAT('192.168.1.', FLOOR(RAND() * 250))
           WHEN RAND() < 0.5 THEN CONCAT('10.20.', FLOOR(RAND() * 4), '.', FLOOR(RAND() * 250))
           ELSE CONCAT('113.', FLOOR(RAND() * 250), '.', FLOOR(RAND() * 250), '.', FLOOR(RAND() * 250)) END,
      IF(RAND() < 0.75, '内网', '外网'),
      IF(RAND() < 0.045, 1, 0),
      IF(RAND() < 0.045, '账号或密码错误', '登录成功'),
      DATE_ADD(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL FLOOR(RAND() * 30) DAY), INTERVAL (8 + FLOOR(RAND() * 11)) HOUR), INTERVAL FLOOR(RAND() * 60) MINUTE)
    );
    SET i = i + 1;
  END WHILE;
  SET i = 0;
  WHILE i < 520 DO
    INSERT INTO sys_oper_log (module, action, method, uri, oper_account, oper_name, ip, zone, result, err_msg, cost_ms, params, oper_time)
    VALUES (
      ELT(1 + FLOOR(RAND() * 6), '用户管理', '角色管理', '菜单管理', '参数配置', '字典管理', '登录日志'),
      ELT(1 + FLOOR(RAND() * 6), '修改用户', '修改角色', '新增用户', '修改参数', '修改字典', '删除日志'),
      ELT(1 + FLOOR(RAND() * 3), 'PUT', 'POST', 'DELETE'),
      ELT(1 + FLOOR(RAND() * 4), '/api/system/users/3', '/api/system/roles/2', '/api/system/users', '/api/system/configs/3'),
      CASE WHEN RAND() < 0.7 THEN 'admin' WHEN RAND() < 0.6 THEN 'chensy' ELSE 'wumin' END,
      CASE WHEN RAND() < 0.7 THEN '周明远' WHEN RAND() < 0.6 THEN '陈思远' ELSE '吴敏' END,
      CASE WHEN RAND() < 0.7 THEN CONCAT('192.168.1.', FLOOR(RAND() * 250)) ELSE '0:0:0:0:0:0:0:1' END,
      '本机',
      IF(RAND() < 0.02, 1, 0),
      NULL,
      4 + FLOOR(RAND() * 80),
      '[]',
      DATE_ADD(DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7 * 24) HOUR), INTERVAL FLOOR(RAND() * 60) MINUTE), INTERVAL 0 SECOND)
    );
    SET i = i + 1;
  END WHILE;
END$$
DELIMITER ;

CALL gen_logs();
DROP PROCEDURE IF EXISTS gen_logs;

-- 修正操作日志的账号/姓名对应
UPDATE sys_oper_log SET oper_name = '周明远' WHERE oper_account = 'admin';
UPDATE sys_oper_log SET oper_name = '陈思远' WHERE oper_account = 'chensy';
UPDATE sys_oper_log SET oper_name = '吴敏' WHERE oper_account = 'wumin';
UPDATE sys_oper_log SET ip = '127.0.0.1' WHERE ip LIKE '192.168.1.';

SELECT '初始化完成' AS result,
  (SELECT COUNT(*) FROM sys_user) users,
  (SELECT COUNT(*) FROM sys_menu) menus,
  (SELECT COUNT(*) FROM sys_login_log) login_logs,
  (SELECT COUNT(*) FROM sys_oper_log) oper_logs;
