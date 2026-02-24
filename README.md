# GIF Maker（Spring Boot）

一个本地网页应用：上传多张图片，生成 GIF 并下载。

## 1. 本地运行（Maven）

```bash
mvn spring-boot:run
```

访问：http://localhost:8090

## 2. 打包并运行

```bash
mvn clean package
java -jar target/gif-maker-0.0.1-SNAPSHOT.jar
```

访问：http://localhost:8090

## 3. 使用 Docker 运行

```bash
docker build -t gif-maker:local .
docker run --rm -p 8090:8090 gif-maker:local
```

访问：http://localhost:8090

## 4. API

- `POST /api/gif/create`
- `multipart/form-data`
- 参数：
  - `files`：多张图片（至少 2 张）
  - `delayMs`：帧间隔，`50~5000`，默认 `300`
  - `loop`：是否循环，默认 `true`

返回：`image/gif`

## 5. 限制

- 单张图片最大：10MB
- 图片数量上限：60
- 所有帧会统一缩放到最小公共宽高