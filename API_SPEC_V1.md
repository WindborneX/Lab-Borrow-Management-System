# 实验室设备借用管理系统 - V1 API 接口规范

本规范遵循 [AGENT_INSTRUCTIONS.md](file:///D:/Projects/Lab Equipment Borrowing Management System/AGENT_INSTRUCTIONS.md) 中的 API 设计原则，采用业务语义化的命名方式。

## 1. 基础约定
*   **Base URL**: `/api/v1`
*   **Content-Type**: `application/json`
*   **通用响应结构**:
    ```json
    {
      "code": 200,          // 业务状态码
      "message": "success", // 提示信息
      "data": null          // 响应数据
    }
    ```

---

## 2. 设备相关接口 (Equipment)

### 2.1 获取设备列表
*   **Endpoint**: `GET /equipment`
*   **查询参数**:
    *   `name`: 可选，按名称模糊搜索。
    *   `category`: 可选，按分类搜索。
    *   `status`: 可选，按状态过滤 (0:在库, 1:借出, 2:维修, 3:停用)。
*   **响应数据**: `data: Equipment[]`

### 2.2 获取设备详情
*   **Endpoint**: `GET /equipment/{id}`
*   **响应数据**: `data: EquipmentDetail` (包含基本信息及当前是否被借用)。

### 2.3 录入设备 (Admin)
*   **Endpoint**: `POST /equipment`
*   **请求体**:
    ```json
    {
      "name": "设备名称",
      "category": "分类",
      "description": "备注"
    }
    ```

---

## 3. 借还业务接口 (Borrowing)

### 3.1 提交借用申请
*   **Endpoint**: `POST /borrow-applications`
*   **描述**: 用户提交借用设备的请求。
*   **请求体**:
    ```json
    {
      "equipmentId": 1001,
      "userId": 501,
      "expectReturnTime": "2026-09-20 18:00:00"
    }
    ```
*   **核心逻辑**:
    1.  校验设备 ID 是否存在。
    2.  校验设备当前状态是否为 `0 (在库)`。
    3.  开启事务：
        *   修改 `equipment` 表中该设备状态为 `1 (借出)`。
        *   在 `borrow_records` 表中插入新记录（状态为 `0 (借用中)`）。
    4.  提交事务。
*   **异常处理**:
    *   设备已借出：返回 `409 Conflict`, message: "设备已被借出"。
    *   设备维修中/停用：返回 `403 Forbidden`, message: "设备当前不可用"。

### 3.2 归还设备
*   **Endpoint**: `POST /borrow-records/{id}/return`
*   **描述**: 登记设备归还，释放资源。
*   **核心逻辑**:
    1.  校验借用记录 ID 是否存在且状态为 `0 (借用中)`。
    2.  开启事务：
        *   查询记录关联的 `equipmentId`。
        *   修改 `equipment` 表中该设备状态为 `0 (在库)`。
        *   更新 `borrow_records` 表，填写 `actual_return_time`，修改状态为 `1 (已归还)`。
    3.  提交事务。
*   **异常处理**:
    *   记录已归还：返回 `400 Bad Request`, message: "该记录已处理，请勿重复操作"。

### 3.3 查看借用台账
*   **Endpoint**: `GET /borrow-records`
*   **查询参数**:
    *   `userId`: 可选，学生查看个人记录。
    *   `status`: 可选，过滤进行中/已完成的记录。
*   **响应数据**: `data: BorrowRecord[]`

---

## 4. 异常码规范 (准则 13)
*   `400`: 参数错误或业务逻辑违规（如重复归还）。
*   `401`: 未登录/身份验证失败。
*   `403`: 权限不足（如非管理员录入设备）。
*   `404`: 资源不存在（如错误的设备 ID）。
*   `409`: 资源冲突（如设备已被抢借）。
*   `500`: 服务器内部错误。
