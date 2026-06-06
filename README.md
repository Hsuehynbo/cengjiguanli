# 企业等级谈话管理系统

## 项目简介
企业等级谈话管理系统是一个基于Vue3和Spring Boot的企业内部管理系统，用于管理组织架构和谈话记录。

## 系统功能
1. **组织架构可视化**：使用AntV G6实现炫酷的组织架构树形图，支持折叠展开、懒加载、搜索定位等功能
2. **权限控制**：每个用户只能查看自己和下级的信息，只有上级可以给下级添加谈话记录
3. **谈话记录管理**：支持新增、查看、删除谈话记录，包含时间、地点、内容、照片等信息
4. **个人信息管理**：查看个人信息和下级列表

## 技术栈
- **前端**：Vue3 + Ant Design Vue + AntV G6 + Axios
- **后端**：Spring Boot + Spring Security + JPA + MySQL
- **数据库**：MySQL

## 项目结构
```
demo2/
├── backend/            # 后端Spring Boot项目
├── frontend/           # 前端Vue3项目
├── database.sql        # 数据库SQL文件
└── README.md           # 项目说明文档
```

## 运行说明

### 1. 数据库配置
1. 安装MySQL数据库
2. 创建数据库 `company_talk_system`
3. 执行 `database.sql` 文件，导入数据库结构和测试数据

### 2. 后端服务
1. 安装JDK 17或更高版本
2. 安装Maven 3.6或更高版本
3. 进入 `backend` 目录，执行以下命令：
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
4. 后端服务将在 `http://localhost:8080` 运行

### 3. 前端服务
1. 安装Node.js 14或更高版本
2. 进入 `frontend` 目录，执行以下命令：
   ```bash
   npm install
   npm run dev
   ```
3. 前端服务将在 `http://localhost:3000` 运行

## 测试数据
- **总经理**：工号 `GM001`，密码 `123456`
- **部门总监**：工号 `D001`-`D015`，密码 `123456`
- **经理**：工号 `M001`-`M045`，密码 `123456`
- **主管**：工号 `S001`-`S120`，密码 `123456`
- **员工**：工号 `E001`-`E320`，密码 `123456`

## 系统截图

### 登录页面
![登录页面](https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=login%20page%20for%20enterprise%20system%20with%20job%20number%20and%20password%20fields&image_size=landscape_16_9)

### 组织架构页面
![组织架构页面](https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=organizational%20chart%20visualization%20with%20tree%20structure%20and%20gradient%20colors&image_size=landscape_16_9)

### 谈话记录页面
![谈话记录页面](https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=talk%20records%20management%20table%20with%20details%20and%20actions&image_size=landscape_16_9)

### 个人信息页面
![个人信息页面](https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=personal%20profile%20page%20with%20user%20info%20and%20subordinates%20list&image_size=landscape_16_9)
