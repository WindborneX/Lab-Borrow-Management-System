# 实验室设备借用管理系统 - V1 详细设计文档

根据 [AGENT_INSTRUCTIONS.md](file:///D:/Projects/Lab Equipment Borrowing Management System/AGENT_INSTRUCTIONS.md) 准则 9.2，本设计文档涵盖了数据模型设计与 API 接口规范。

## 1. 数据模型设计 (Data Model)

### 1.1 实体关系简述 (ER Description)
*   **用户 (User)**：系统使用者，分为学生和管理员。
*   **设备 (Equipment)**：核心资产，拥有唯一的借用状态。
*   **借用记录 (BorrowRecord)**：连接用户与设备的业务凭证，保留历史追溯性。

### 1.2 数据库表结构 (Schema)

#### 表：`users` (用户信息)
| 字段名 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, AUTO_INC | 内部唯一 ID |
| `student_id` | VARCHAR(20) | UNIQUE, NOT NULL | 学号/工号 |
| `username` | VARCHAR(50) | NOT NULL | 姓名 |
| `role` | TINYINT | NOT NULL, DEFAULT 0 | 0:学生, 1:管理员 |
| `created_at` | DATETIME | NOT NULL, DEFAULT NOW | 创建时间 |

#### 表：`equipment` (设备信息)
| 字段名 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, AUTO_INC | 内部唯一 ID |
| `name` | VARCHAR(100) | NOT NULL | 设备名称 |
| `category` | VARCHAR(50) | | 分类 (如: 开发板, 传感器) |
| `status` | TINYINT | NOT NULL, DEFAULT 0 | 0:在库, 1:借出, 2:维修, 3:停用 |
| `description` | TEXT | | 设备备注信息 |
| `created_at` | DATETIME | NOT NULL, DEFAULT NOW | 录入时间 |

#### 表：`borrow_records` (借用历史记录)
| 字段名 | 类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| `id` | BIGINT | PK, AUTO_INC | 内部唯一 ID |
| `equipment_id` | BIGINT | FK, NOT NULL | 关联设备 ID |
| `user_id` | BIGINT | FK, NOT NULL | 关联借用人 ID |
| `borrow_time` | DATETIME | NOT NULL, DEFAULT NOW | 实际借出时间 |
| `expect_return_time` | DATETIME | | 预计归还时间 |
| `actual_return_time` | DATETIME | | 实际归还时间 |
| `status` | TINYINT | NOT NULL, DEFAULT 0 | 0:借用中, 1:已归还, 2:超期未还 |

---

## 2. API 接口设计 (API Design)

所有接口前缀统一为 `/api/v1`。详细规范参见 [API_SPEC_V1.md](file:///D:/Projects/Lab Equipment Borrowing Management System/API_SPEC_V1.md)。

### 2.1 设备管理 (Equipment)
*   **GET `/equipment`**：获取设备列表（支持按名称/状态过滤）。
*   **GET `/equipment/{id}`**：获取特定设备详情。
*   **POST `/equipment`**：录入新设备 (Admin Only)。

### 2.2 借还业务 (Borrowing)
*   **POST `/borrow-applications`**：提交借用申请。
    *   逻辑：校验设备 `status=0` → 开启事务 → 设备 `status=1` → 插入 `borrow_records` → 提交。
*   **POST `/borrow-records/{id}/return`**：登记归还设备。
    *   逻辑：校验记录 `status=0` → 开启事务 → 设备 `status=0` → 更新记录归还时间与状态 → 提交。
*   **GET `/borrow-records`**：查看借用台账（支持按用户/状态过滤）。

---

## 3. 业务一致性保证
*   **事务控制**：借用与归还操作必须包裹在数据库事务中，确保“设备状态变更”与“记录更新”同时成功或失败。
*   **追溯性**：严禁物理删除 `borrow_records`，所有归还操作仅更新 `actual_return_time` 和 `status`。
