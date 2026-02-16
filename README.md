# 乐逸零食店管理系统

一个完整的线下零食店管理系统，包含顾客端（C端）和店家端（B端）。

## 技术栈

### 前端
- Vue 3 (Composition API + script setup)
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios
- ECharts
- dayjs
- SCSS

### 后端
- Spring Boot 3.2
- MyBatis
- MySQL 8.0
- JWT
- Lombok

## 项目结构

```
乐逸零食店/
├── database/                # 数据库脚本
│   └── init.sql            # 初始化SQL
├── backend/                 # Spring Boot后端
│   ├── pom.xml
│   └── src/
├── frontend-customer/       # 顾客端Vue项目 (端口3000)
│   └── src/
└── frontend-admin/          # 店家端Vue项目 (端口3001)
    └── src/
```

## 快速开始

### 1. 数据库初始化

```bash
# 登录MySQL，执行初始化脚本
mysql -u root -p < database/init.sql
```

或者在MySQL客户端中执行 `database/init.sql` 文件内容。

### 2. 后端启动

```bash
cd backend

# 修改数据库连接配置 (如需要)
# 编辑 src/main/resources/application.yml

# Maven打包运行
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/leyi-snack-1.0.0.jar
```

后端服务将在 http://localhost:8080 启动

### 3. 前端启动

**顾客端：**
```bash
cd frontend-customer
npm install
npm run dev
```
访问地址：http://localhost:3000

**店家端：**
```bash
cd frontend-admin
npm install
npm run dev
```
访问地址：http://localhost:3001

## 测试账号

### 顾客端
- 手机号：任意11位手机号
- 验证码：000000（开发环境）

### 店家端
- 店长账号：13800000001 / 123456
- 店员账号：13800000002 / 123456

## 功能模块

### 顾客端（C端）
- ✅ 手机号验证码登录（首次登录自动注册）
- ✅ 商品浏览（分类、搜索、详情）
- ✅ 购物车管理
- ✅ 订单提交（生成取货码）
- ✅ 订单管理（查看、取消）
- ✅ 商品评价
- ✅ 个人中心

### 店家端（B端）
- ✅ 账号密码登录
- ✅ 数据概览（营业额、订单统计、趋势图）
- ✅ 订单核销（输入取货码核销）
- ✅ 订单管理
- ✅ 商品管理（店长专属）
- ✅ 分类管理（店长专属）
- ✅ 店员管理（店长专属）
- ✅ 库存预警
- ✅ 临期商品预警

## 核心业务流程

### 顾客下单流程
1. 登录 → 浏览商品 → 加入购物车
2. 结算 → 提交订单 → 获取取货码
3. 到店报取货码 → 店员核销 → 付款取货
4. 完成后可评价商品

### 店员核销流程
1. 顾客到店报取货码
2. 店员输入取货码查询订单
3. 核对商品信息
4. 确认核销 → 线下收款

## 注意事项

1. 确保MySQL服务已启动，数据库配置正确
2. 后端需要Java 17+环境
3. 前端需要Node.js 16+环境
4. 开发环境验证码固定为 000000
5. 文件上传目录默认为 `./uploads`

## 配置说明

### 后端配置 (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/leyi_snack
    username: root
    password: 123456

jwt:
  secret: your-secret-key
  expiration: 604800000  # 7天
```

### 前端代理配置 (vite.config.js)
```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```



