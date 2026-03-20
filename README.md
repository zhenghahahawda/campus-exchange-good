# 校园换物平台 Campus Exchange

> 以物换物，让闲置物品重获价值。

本仓库包含校园换物平台的完整源码，分为四个子项目：

| 目录 | 说明 | 技术栈 | 端口 |
|------|------|--------|------|
| `admin-ui` | 管理员前端 | Nuxt 2 + Vue 2 + Element UI | 3000 |
| `campus-exchange` | 管理员后端 | Spring Boot 3.2 + Java 17 | 10010 |
| `campus-font` | 用户前端 | Nuxt 2 + Vue 2 + Element UI | 3001 |
| `houduan` | 用户后端 | Spring Boot 2.7 + Java 8 | 8081 |
| `Sql` | 数据库脚本 | MySQL 8.0 | — |

---

## 环境要求

- **Node.js** 16.x 或 18.x（推荐 18 LTS）
- **Java 17**（campus-exchange）
- **Java 8 / 11**（houduan）
- **Maven** 3.6+
- **MySQL** 8.0
- **Redis** 6.x+

---

## 一、数据库初始化

```sql
-- 登录 MySQL 后执行
source Sql/exchange_system.sql;
```

或直接在 MySQL 客户端（Navicat / DBeaver）中导入 `Sql/exchange_system.sql`。

---

## 二、管理员后端（campus-exchange）

### 1. 配置文件

```bash
cd campus-exchange/src/main/resources
cp application-example.yml application.yml
```

编辑 `application.yml`，填入以下内容：

```yaml
spring:
  datasource:
    url: jdbc:mysql://YOUR_DB_HOST:3306/exchange_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD
  redis:
    host: YOUR_REDIS_HOST
    port: 6379
    password: YOUR_REDIS_PASSWORD

cos:
  secret-id: YOUR_COS_SECRET_ID
  secret-key: YOUR_COS_SECRET_KEY
  region: ap-guangzhou
  bucket-name: YOUR_BUCKET_NAME
```

### 2. 启动

```bash
cd campus-exchange
mvn spring-boot:run
```

或打包后运行：

```bash
mvn clean package -DskipTests
java -jar target/campus-exchange-0.0.1-SNAPSHOT.jar
```

启动后访问 API 文档：http://localhost:10010/swagger-ui/index.html

---

## 三、用户后端（houduan）

### 1. 配置文件

```bash
cd houduan/src/main/resources
cp application-example.yml application.yml
```

编辑 `application.yml`，填入数据库、Redis、COS、JWT 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://YOUR_DB_HOST:3306/exchange_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf-8
    username: YOUR_DB_USERNAME
    password: YOUR_DB_PASSWORD
  redis:
    host: YOUR_REDIS_HOST
    port: 6379
    password: YOUR_REDIS_PASSWORD

tencent:
  cos:
    secret-id: YOUR_COS_SECRET_ID
    secret-key: YOUR_COS_SECRET_KEY
    region: ap-guangzhou
    bucket-name: YOUR_BUCKET_NAME

jwt:
  secret: YOUR_JWT_SECRET_KEY_AT_LEAST_256_BITS
  expiration: 604800
```

### 2. 启动

```bash
cd houduan
mvn spring-boot:run
```

或打包后运行：

```bash
mvn clean package -DskipTests
java -jar target/houdaun-1.0-SNAPSHOT.jar
```

服务启动在 http://localhost:8081

---

## 四、管理员前端（admin-ui）

### 1. 安装依赖

```bash
cd admin-ui
npm install
```

### 2. 配置后端地址

编辑 `nuxt.config.js` 中的 `axios.baseURL`，默认指向 `http://localhost:10010/`，如有变动请修改。

### 3. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 4. 生产构建

```bash
npm run build
npm run start
```

---

## 五、用户前端（campus-font）

### 1. 安装依赖

```bash
cd campus-font
npm install
```

### 2. 配置后端地址

编辑 `nuxt.config.js` 中的 `axios.baseURL`，默认指向 `http://localhost:8081/api`，如有变动请修改。

### 3. 启动开发服务器

```bash
npm run dev
```

默认端口为 3000，如果 admin-ui 已占用，可在 `nuxt.config.js` 中添加：

```js
server: {
  port: 3001
}
```

访问 http://localhost:3001

### 4. 生产构建

```bash
npm run build
npm run start
```

---

## 六、启动顺序建议

```
1. MySQL  →  导入 Sql/exchange_system.sql
2. Redis  →  确保服务运行
3. campus-exchange（管理员后端，端口 10010）
4. houduan（用户后端，端口 8081）
5. admin-ui（管理员前端，端口 3000）
6. campus-font（用户前端，端口 3001）
```

---

## 七、默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |

---

## 八、项目截图

> 可在各子项目 README 中查看详细截图。

---

## 九、注意事项

- `application.yml` 已加入 `.gitignore`，不会提交到仓库，请根据 `application-example.yml` 自行创建。
- 腾讯云 COS 用于存储用户头像和商品图片，如不需要可替换为本地存储。
- 两个后端共用同一个 MySQL 数据库 `exchange_system`。
