-- =============================================================
-- 智能校园微服务课程选退课系统 数据库初始化脚本
-- 适用：MySQL 5.7 / 8.0
-- 默认账号：
--   管理员 admin   / Admin123
--   教师   T1001   / Pass1234
--   学生   20210001~20210005 / Pass1234
-- =============================================================
CREATE DATABASE IF NOT EXISTS smart_campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE smart_campus;

-- -------------------------------------------------------------
-- 1. 用户表（user-service）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(32)  NOT NULL COMMENT '学号/工号，系统内唯一',
    password      VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
    name          VARCHAR(32)  NOT NULL COMMENT '姓名',
    role          VARCHAR(16)  NOT NULL COMMENT '角色：STUDENT/TEACHER/ADMIN',
    college       VARCHAR(64)  DEFAULT NULL COMMENT '院系',
    major         VARCHAR(64)  DEFAULT NULL COMMENT '专业',
    email         VARCHAR(64)  DEFAULT NULL COMMENT '邮箱，唯一',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    gender        TINYINT      DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    interest_tags VARCHAR(255) DEFAULT NULL COMMENT '兴趣标签，逗号分隔',
    status        TINYINT      DEFAULT 1 COMMENT '状态：1正常 0禁用',
    fail_count    INT          DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until  DATETIME     DEFAULT NULL COMMENT '锁定截止时间',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    KEY idx_role (role),
    KEY idx_college (college)
) ENGINE = InnoDB COMMENT = '用户表';

-- -------------------------------------------------------------
-- 2. 系统公告表（user-service）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title        VARCHAR(100) NOT NULL COMMENT '公告标题',
    content      TEXT COMMENT '公告内容',
    publisher    VARCHAR(32)  DEFAULT NULL COMMENT '发布人',
    status       TINYINT      DEFAULT 1 COMMENT '状态：1发布 0下架',
    publish_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB COMMENT = '系统公告表';

-- -------------------------------------------------------------
-- 3. 课程表（course-service）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_no      VARCHAR(32)  NOT NULL COMMENT '课程编号，唯一，如 CS101',
    name           VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher_no     VARCHAR(32)  DEFAULT NULL COMMENT '授课教师工号',
    teacher_name   VARCHAR(32)  DEFAULT NULL COMMENT '授课教师姓名',
    credits        INT          NOT NULL COMMENT '学分 1-6',
    class_time     VARCHAR(255) DEFAULT NULL COMMENT '上课时间，如：周一 1-2节, 周三 3-4节',
    location       VARCHAR(100) DEFAULT NULL COMMENT '上课地点',
    capacity       INT          NOT NULL COMMENT '容量上限',
    selected_count INT          DEFAULT 0 COMMENT '已选人数',
    prerequisite   VARCHAR(255) DEFAULT NULL COMMENT '先修课程编号，多个逗号分隔',
    college        VARCHAR(64)  NOT NULL COMMENT '开课院系',
    course_type    VARCHAR(16)  NOT NULL COMMENT '课程类型：REQUIRED必修/ELECTIVE选修/GENERAL通识',
    description    VARCHAR(500) DEFAULT NULL COMMENT '课程描述',
    status         TINYINT      DEFAULT 1 COMMENT '状态：1开放 0停开',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_course_no (course_no),
    KEY idx_name (name),
    KEY idx_college (college),
    KEY idx_type (course_type)
) ENGINE = InnoDB COMMENT = '课程表';

-- -------------------------------------------------------------
-- 4. 教学计划表（course-service）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS teaching_plan;
CREATE TABLE teaching_plan (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    course_id    BIGINT       NOT NULL COMMENT '课程ID',
    course_no    VARCHAR(32)  NOT NULL COMMENT '课程编号',
    outline      TEXT COMMENT '教学大纲',
    schedule     TEXT COMMENT '教学进度表',
    textbook     VARCHAR(255) DEFAULT NULL COMMENT '参考教材',
    audit_status TINYINT      DEFAULT 0 COMMENT '审核状态：0待审核 1通过 2驳回',
    audit_remark VARCHAR(255) DEFAULT NULL COMMENT '审核意见',
    update_by    VARCHAR(32)  DEFAULT NULL COMMENT '最后更新人',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_course_id (course_id),
    KEY idx_course_no (course_no)
) ENGINE = InnoDB COMMENT = '教学计划表';

-- -------------------------------------------------------------
-- 5. 学生成绩表（course-service）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS student_grade;
CREATE TABLE student_grade (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_no  VARCHAR(32) NOT NULL COMMENT '学号',
    course_no   VARCHAR(32) NOT NULL COMMENT '课程编号',
    course_type VARCHAR(16) DEFAULT NULL COMMENT '课程类型',
    semester    VARCHAR(16) DEFAULT NULL COMMENT '学期',
    score       DECIMAL(5, 2) NOT NULL COMMENT '成绩 0-100',
    update_time DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_student (student_no),
    KEY idx_course (course_no)
) ENGINE = InnoDB COMMENT = '学生成绩表';

-- -------------------------------------------------------------
-- 6. 选课记录表（selection-service）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS selection_record;
CREATE TABLE selection_record (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    student_no  VARCHAR(32) NOT NULL COMMENT '学号',
    course_no   VARCHAR(32) NOT NULL COMMENT '课程编号',
    semester    VARCHAR(16) DEFAULT '2026-2027-1' COMMENT '学期',
    status      VARCHAR(16) DEFAULT 'SELECTED' COMMENT '状态：SELECTED已选/DROPPED已退',
    select_time DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    drop_time   DATETIME    DEFAULT NULL COMMENT '退课时间',
    PRIMARY KEY (id),
    KEY idx_student (student_no),
    KEY idx_course (course_no),
    KEY idx_status (status)
) ENGINE = InnoDB COMMENT = '选课记录表';

-- -------------------------------------------------------------
-- 7. 选课参数配置表（selection-service）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS selection_config;
CREATE TABLE selection_config (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    config_key   VARCHAR(64)  NOT NULL COMMENT '配置键',
    config_value VARCHAR(255) NOT NULL COMMENT '配置值',
    description  VARCHAR(255) DEFAULT NULL COMMENT '说明',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE = InnoDB COMMENT = '选课参数配置表';

-- =============================================================
-- 种子数据
-- =============================================================
INSERT INTO sys_user (username, password, name, role, college, major, email, phone, gender, interest_tags, status)
VALUES
('admin', '$2b$10$DFPaboULV5oeZ4uSxrmd0.uUzTI1Cgizlz9/DWEAXrx5Qr6eOtnoW', '系统管理员', 'ADMIN', '教务处', NULL, 'admin@smart.edu.cn', NULL, 1, NULL, 1),
('T1001', '$2b$10$Rt.2M8Q9iV40fnH5u8oN/.7j/kF5Oo.4A/kz7CBi0PgLIFgqR3t.S', '张伟', 'TEACHER', '计算机学院', NULL, 'zhangwei@smart.edu.cn', '13800000001', 1, NULL, 1),
('T1002', '$2b$10$Rt.2M8Q9iV40fnH5u8oN/.7j/kF5Oo.4A/kz7CBi0PgLIFgqR3t.S', '李娜', 'TEACHER', '计算机学院', NULL, 'lina@smart.edu.cn', '13800000002', 2, NULL, 1),
('T1003', '$2b$10$Rt.2M8Q9iV40fnH5u8oN/.7j/kF5Oo.4A/kz7CBi0PgLIFgqR3t.S', '王强', 'TEACHER', '外国语学院', NULL, 'wangqiang@smart.edu.cn', '13800000003', 1, NULL, 1),
('20210001', '$2b$10$Rt.2M8Q9iV40fnH5u8oN/.7j/kF5Oo.4A/kz7CBi0PgLIFgqR3t.S', '陈晨', 'STUDENT', '计算机学院', '软件工程', 'chenchen@stu.smart.edu.cn', '13900000001', 1, '编程,人工智能', 1),
('20210002', '$2b$10$Rt.2M8Q9iV40fnH5u8oN/.7j/kF5Oo.4A/kz7CBi0PgLIFgqR3t.S', '林芳', 'STUDENT', '计算机学院', '计算机科学与技术', 'linfang@stu.smart.edu.cn', '13900000002', 2, '算法,数据结构', 1),
('20210003', '$2b$10$Rt.2M8Q9iV40fnH5u8oN/.7j/kF5Oo.4A/kz7CBi0PgLIFgqR3t.S', '黄磊', 'STUDENT', '外国语学院', '英语', 'huanglei@stu.smart.edu.cn', '13900000003', 1, '英语,翻译', 1),
('20210004', '$2b$10$Rt.2M8Q9iV40fnH5u8oN/.7j/kF5Oo.4A/kz7CBi0PgLIFgqR3t.S', '周洁', 'STUDENT', '计算机学院', '网络工程', 'zhoujie@stu.smart.edu.cn', '13900000004', 2, '网络安全,编程', 1),
('20210005', '$2b$10$Rt.2M8Q9iV40fnH5u8oN/.7j/kF5Oo.4A/kz7CBi0PgLIFgqR3t.S', '吴桐', 'STUDENT', '计算机学院', '软件工程', 'wutong@stu.smart.edu.cn', '13900000005', 1, NULL, 1);

INSERT INTO sys_notice (title, content, publisher, status) VALUES
('2026-2027 学年第一学期选课通知', '本学期选课开放时间为 2026-08-01 至 2026-09-30，退课截止时间为 2026-09-15。每人最多选修 5 门课程，请同学们合理安排学习计划。', 'admin', 1),
('选课系统使用说明', '登录后在"课程列表"中检索课程，点击"选课"即可完成选课；系统会自动检测时间冲突、先修课程与名额情况。', 'admin', 1);

INSERT INTO course (course_no, name, teacher_no, teacher_name, credits, class_time, location, capacity, selected_count, prerequisite, college, course_type, description, status)
VALUES
('CS101', '数据结构', 'T1001', '张伟', 4, '周一 1-2节, 周三 1-2节', '教学楼A-301', 60, 12, NULL, '计算机学院', 'REQUIRED', '线性表、树、图等基础数据结构与算法分析。', 1),
('CS102', '操作系统', 'T1001', '张伟', 3, '周三 3-4节, 周五 1-2节', '教学楼A-302', 60, 12, 'CS101', '计算机学院', 'REQUIRED', '进程管理、内存管理、文件系统等操作系统核心原理。', 1),
('CS201', 'Java程序设计', 'T1001', '张伟', 3, '周二 1-2节, 周四 1-2节', '实验楼B-201', 50, 8, NULL, '计算机学院', 'ELECTIVE', 'Java 语言基础、面向对象编程与集合框架。', 1),
('CS202', '人工智能导论', 'T1002', '李娜', 2, '周五 5-6节', '教学楼A-401', 40, 10, NULL, '计算机学院', 'ELECTIVE', '人工智能发展史、搜索、知识表示与机器学习入门。', 1),
('CS203', '机器学习', 'T1002', '李娜', 3, '周五 5-6节, 周一 7-8节', '实验楼B-301', 40, 5, 'CS202', '计算机学院', 'ELECTIVE', '监督学习、无监督学习与模型评估方法。', 1),
('CS204', 'Web前端开发', 'T1001', '张伟', 2, '周三 1-2节', '实验楼B-202', 40, 15, NULL, '计算机学院', 'ELECTIVE', 'HTML/CSS/JavaScript 与 Vue 前端开发实践。', 1),
('EN101', '大学英语', 'T1003', '王强', 2, '周二 3-4节, 周四 3-4节', '教学楼C-101', 80, 30, NULL, '外国语学院', 'GENERAL', '大学英语综合能力训练。', 1),
('EN201', '英语口语', 'T1003', '王强', 1, '周一 3-4节', '教学楼C-201', 30, 20, 'EN101', '外国语学院', 'ELECTIVE', '英语口语表达与演讲训练。', 1),
('MA101', '高等数学', 'T1002', '李娜', 4, '周二 5-6节, 周四 5-6节', '教学楼B-501', 100, 45, NULL, '计算机学院', 'GENERAL', '微积分、线性代数与概率统计基础。', 1),
('PE101', '大学体育', 'T1003', '王强', 1, '周五 7-8节', '体育馆', 60, 25, NULL, '体育部', 'GENERAL', '身体素质训练与专项体育技能。', 1);

INSERT INTO teaching_plan (course_id, course_no, outline, schedule, textbook, audit_status, audit_remark, update_by) VALUES
(1, 'CS101', '1.线性表 2.栈与队列 3.树与二叉树 4.图 5.查找与排序', '第1-4周 线性表；第5-8周 树；第9-12周 图；第13-16周 查找与排序', '《数据结构（C语言版）》 清华大学出版社', 1, '通过', 'T1001'),
(2, 'CS102', '1.进程与线程 2.处理机调度 3.内存管理 4.文件系统', '第1-5周 进程管理；第6-10周 内存管理；第11-16周 文件系统', '《计算机操作系统》 西安电子科技大学出版社', 1, '通过', 'T1001'),
(3, 'CS201', '1.Java基础语法 2.面向对象 3.集合与泛型 4.IO与多线程', '第1-6周 语法与OOP；第7-12周 集合与IO；第13-16周 多线程与网络', '《Java核心技术》 机械工业出版社', 0, NULL, 'T1001');

INSERT INTO student_grade (student_no, course_no, course_type, semester, score) VALUES
('20210001', 'CS101', 'REQUIRED', '2025-2026-1', 85),
('20210001', 'EN101', 'GENERAL', '2025-2026-1', 78),
('20210001', 'MA101', 'GENERAL', '2025-2026-1', 88),
('20210002', 'CS101', 'REQUIRED', '2025-2026-1', 92),
('20210002', 'EN101', 'GENERAL', '2025-2026-1', 90),
('20210002', 'MA101', 'GENERAL', '2025-2026-1', 95);

INSERT INTO selection_record (student_no, course_no, semester, status, select_time) VALUES
('20210001', 'CS102', '2026-2027-1', 'SELECTED', '2026-08-03 10:15:00'),
('20210001', 'CS202', '2026-2027-1', 'SELECTED', '2026-08-03 10:20:00'),
('20210002', 'CS102', '2026-2027-1', 'SELECTED', '2026-08-03 11:05:00'),
('20210002', 'CS201', '2026-2027-1', 'SELECTED', '2026-08-03 11:12:00'),
('20210003', 'EN201', '2026-2027-1', 'SELECTED', '2026-08-04 09:30:00'),
('20210004', 'CS202', '2026-2027-1', 'SELECTED', '2026-08-04 14:22:00'),
('20210004', 'CS204', '2026-2027-1', 'SELECTED', '2026-08-04 14:40:00'),
('20210005', 'PE101', '2026-2027-1', 'SELECTED', '2026-08-05 08:55:00');

INSERT INTO selection_config (config_key, config_value, description) VALUES
('select_start', '2026-08-01 00:00:00', '选课开放时间'),
('select_end', '2026-09-30 23:59:59', '选课截止时间'),
('drop_end', '2026-09-15 23:59:59', '退课窗口期截止时间'),
('max_course_count', '5', '每人选课数量上限'),
('max_credits', '30', '本学期学分上限'),
('allow_drop_required', 'false', '是否允许退必修课');
