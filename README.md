# 实验室设备借用管理系统 (Lab Equipment Borrowing Management System)

## 项目背景
本项目为“软件工程系统开发方向”招新考核方案的实施项目。旨在通过数字化手段解决实验室设备（摄像头、开发板、传感器等）借用过程中的状态不明、记录缺失等管理痛点。

## 开源地址
GitHub: [Lab-Borrow-Management-System](https://github.com/WindborneX/Lab-Borrow-Management-System)

## 业务开发历程
| 时间 | 阶段 | 核心任务 | 状态 |
| :--- | :--- | :--- | :--- |
| 2026-09-17 | **需求分析 V1** | 明确 MVP 需求、角色、核心流程与业务规则 | 已完成 |
| 2026-09-17 | **准则确立** | 编写 Agent 指令与开发原则，规范化开发流程 | 已完成 |
| 2026-09-17 | **详细设计 V1** | 完成数据库 Schema 设计与 API 接口详细定义 | 已完成 |
| 2026-09-17 | **功能开发 V1** | 实现设备管理与借还核心逻辑，通过 API 验证 | 已完成 |
| 待定 | **V2 迭代** | 根据新增反馈进行功能调整 | 待开始 |

## 当前进度
- [x] 需求初步分析 (基于招新考核方案 V1)
- [x] 确立开发原则与 Agent 指令 ([AGENT_INSTRUCTIONS.md](file:///D:/Projects/Lab Equipment Borrowing Management System/AGENT_INSTRUCTIONS.md))
- [x] V1 详细设计 (API & 数据库) ([DESIGN_V1.md](file:///D:/Projects/Lab Equipment Borrowing Management System/DESIGN_V1.md))
- [x] V1 功能实现与测试 (Spring Boot + SQLite)

## 核心架构
- **最小开发原则**：聚焦借还闭环，不进行过度设计。
- **演进式开发**：根据考核阶段 (V1-V4) 逐步迭代方案。

## 开发者指南
所有参与开发的 Agent 必须严格遵守 [AGENT_INSTRUCTIONS.md](file:///D:/Projects/Lab Equipment Borrowing Management System/AGENT_INSTRUCTIONS.md) 中定义的准则。
