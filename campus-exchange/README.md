# 校园二手物品交换平台

基于 Spring Boot + MyBatis-Plus 的校园二手物品交换平台后端服务。

## 技术栈

- Java 8
- Spring Boot 2.2.5
- MyBatis-Plus 3.4.0
- MySQL 8.0
- Redis
- RabbitMQ
- 腾讯云 COS（图片存储）
- JWT（身份认证）
- Swagger2（API 文档）

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+（可选，不影响核心功能启动）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/zhenghahahawda/campus-exchange.git
cd campus-exchange
```

### 2. 创建数据库

连接 MySQL，执行建表脚本：

```bash
mysql -u root -p < Sql/create_all_tables.sql
```

这会自动创建 `exchange_system` 数据库及所有表。

如需初始化测试数据，可继续执行：

```bash
mysql -u root -p exchange_system < Sql/init_categories_data.sql
mysql -u root -p exchange_system < Sql/init_goods_test_data.sql
mysql -u root -p exchange_system < Sql/init_orders_test_data.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改以下配置为你本地的环境：

```yaml
spring:
  redis:
    host: 127.0.0.1          # Redis 地址
    port: 6379
    password: yourpassword    # Redis 密码，没有则留空
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/exchange_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root            # MySQL 用户名
    password: yourpassword    # MySQL 密码
```

如需使用图片上传功能，还需配置腾讯云 COS：

```yaml
cos:
  secret-id: 你的SecretId
  secret-key: 你的SecretKey
  region: ap-guangzhou
  bucket-name: 你的存储桶名称
```

### 4. 编译项目

```bash
mvn clean package -DskipTests
```

### 5. 启动项目

```bash
java -jar target/campus-exchange-0.0.1-SNAPSHOT.jar
```

或者使用 Maven 直接运行：

```bash
mvn spring-boot:run
```

启动成功后，服务运行在 `http://localhost:10010`。

### 6. 访问 API 文档

浏览器打开 Swagger 文档：

```
http://localhost:10010/swagger-ui.html
```

## 项目结构

```
src/main/java/com/campus/exchange/
├── controller/     # 控制器层
├── service/        # 服务接口
│   └── impl/       # 服务实现
├── mapper/         # MyBatis-Plus Mapper
├── entity/         # 实体类
├── dto/            # 数据传输对象
├── config/         # 配置类
└── util/           # 工具类

Sql/
├── create_all_tables.sql       # 建表脚本
├── init_categories_data.sql    # 分类初始数据
├── init_goods_test_data.sql    # 商品测试数据
└── init_orders_test_data.sql   # 订单测试数据
```

## 常见问题

**Q: 启动报数据库连接失败？**
检查 MySQL 是否启动，以及 `application.yml` 中的数据库地址、用户名、密码是否正确。

**Q: 启动报 Redis 连接失败？**
检查 Redis 是否启动，以及配置中的 Redis 地址和密码是否正确。

**Q: RabbitMQ 未安装能否启动？**
RabbitMQ 相关功能会报错，但不影响核心功能。如不需要可在 `pom.xml` 中注释掉 `spring-boot-starter-amqp` 依赖。
