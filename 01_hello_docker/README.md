

发布 `spring boot` 项目到 `docker` 容器

# 1、创建 `spring boot` 项目

参考 [01_hello_docker](../)

> 1、修改 程序的端口为 8081 // 非必须
```yml
# application.yml

server:
  # 注意这里将端口修改成 8081
  port: 8081
```

> 2、修改程序打的包为 `fat jar`

```shell
# pom.xml

  <build>
    <plugins>
        <!-- 需要将当前项目打的普通jar包改成 fat jar，即 java -jar 能运行的jar包 -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>${spring-boot.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build> 
```

> 3、新增 `/hello_docker` 接口

```java
@RestController
public class TestController {

    @GetMapping("/hello_docker")
    public String helloDocker() {
        System.out.println("=================hello docker=================");
        return "hello docker...";
    }

}
```

> 4、执行 `mvn clean package`

得到编译产物 `target/hello_docker-0.0.1-SNAPSHOT.jar`

> 5、将编译产物拷贝到 `wsl` 虚拟机的任意目录

```shell
# wsl 的 /mnt 目录可以访问 宿主机的任意磁盘
hewenyu@hewenyu:/mnt$ pwd
/mnt
hewenyu@hewenyu:/mnt$ ls
c  d  e  f  wsl  wslg

# 拷贝编译产物
hewenyu@hewenyu:~/docker/hello_docker$ pwd
/home/hewenyu/docker/hello_docker
# 这里的 jar 包需要替换成自己的地址
hewenyu@hewenyu:~/docker/hello_docker$ cp /mnt/c/software/docker/docker/01_hello_docker/target/hello_docker-0.0.1-SNAPSHOT.jar hello_docker-0.0.1-SNAPSHOT.jar
hewenyu@hewenyu:~/docker/hello_docker$ ls
Dockerfile  hello_docker-0.0.1-SNAPSHOT.jar
hewenyu@hewenyu:~/docker/hello_docker$
```

# 2、在 `Docker` 容器中运行 `jar`

## 2.1、编写 `Dockerfile`

拷贝 `jar` 文件到 `wsl` 后，在当前目录下创建 `Dockerfile` 文件，然后写入如下内容:

```dockerfile
# 使用 Eclipse Temurin JRE 21（Spring Boot 3.x 需要 Java 17+）
FROM eclipse-temurin:21-jre

# 设置工作目录
WORKDIR /app

# 把 jar 包复制进镜像
COPY hello_docker-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口，注意，expose 暴露的端口只是文档意义上的端口，实际的访问端口是 spring boot 程序里面指定的
EXPOSE 8081

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 2.2、构建镜像

```shell
hewenyu@hewenyu:~/docker/hello_docker$ docker build -t hello_docker:1.1 .
[+] Building 3.2s (8/8) FINISHED                                                                         docker:default
 => [internal] load build definition from Dockerfile                                                               0.0s
 => => transferring dockerfile: 406B                                                                               0.0s
 => [internal] load metadata for docker.io/library/eclipse-temurin:21-jre                                          0.3s
 => [internal] load .dockerignore                                                                                  0.0s
 => => transferring context: 2B                                                                                    0.0s
 => [1/3] FROM docker.io/library/eclipse-temurin:21-jre@sha256:26d6c10ecf99071e134966d7be598e4bbbdb7462637e382b8e  0.1s
 => => resolve docker.io/library/eclipse-temurin:21-jre@sha256:26d6c10ecf99071e134966d7be598e4bbbdb7462637e382b8e  0.1s
 => [internal] load build context                                                                                  0.2s
 => => transferring context: 17.57MB                                                                               0.2s
 => CACHED [2/3] WORKDIR /app                                                                                      0.0s
 => [3/3] COPY hello_docker-0.0.1-SNAPSHOT.jar app.jar                                                             0.8s
 => exporting to image                                                                                             1.6s
 => => exporting layers                                                                                            1.0s
 => => exporting manifest sha256:6f7bb698169bbd15af6d548775222a5cab70f205e7003ea5bb4ac637356dc5f4                  0.0s
 => => exporting config sha256:18d542ce91b3cfc3571ea4186ca619011eaa37d721663ca5590992af1808989a                    0.0s
 => => exporting attestation manifest sha256:4bc14853561062961b213ada22b3fe22166aa960bd97e35dffbbb7835111fbc0      0.1s
 => => exporting manifest list sha256:be1736e03a742e0856890050b0fe08448eb14d4a765016c261034ff0d8b63c76             0.1s
 => => naming to docker.io/library/hello_docker:1.1                                                                0.0s
 => => unpacking to docker.io/library/hello_docker:1.1                                                             0.2s
hewenyu@hewenyu:~/docker/hello_docker$
hewenyu@hewenyu:~/docker/hello_docker$ docker images hello_docker
                                                                                                    i Info →   U  In Use
IMAGE              ID             DISK USAGE   CONTENT SIZE   EXTRA
hello_docker:1.1   be1736e03a74        487MB          131MB    U
hewenyu@hewenyu:~/docker/hello_docker$
```

## 2.3、运行镜像

```shell
# 宿主机端口 8088，容器内的 tomcat 端口为 spring boot 程序指定的端口为 8081
hewenyu@hewenyu:~/docker/hello_docker$ docker run --name hd1 -dp 8088:8081 hello_docker:1.1
43cd2744a05152026fe167070afc1eecc88dae69897335c7983447a9956c856b
hewenyu@hewenyu:~/docker/hello_docker$ docker ps
CONTAINER ID   IMAGE              COMMAND               CREATED         STATUS         PORTS                                                   NAMES
43cd2744a051   hello_docker:1.1   "java -jar app.jar"   3 seconds ago   Up 3 seconds   8080/tcp, 0.0.0.0:8088->8081/tcp, [::]:8088->8081/tcp   hd1
hewenyu@hewenyu:~/docker/hello_docker$
```

> 查看 hd1 容器的日志

```shell
hewenyu@hewenyu:~/docker/hello_docker$ docker logs hd1

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v2.6.13)

level-INFO  - Starting Application using Java 21.0.11 on 43cd2744a051 with PID 1 (/app/app.jar started by root in /app)
level-INFO  - No active profile set, falling back to 1 default profile: "default"
level-INFO  - Tomcat initialized with port(s): 8081 (http)
level-INFO  - Starting service [Tomcat]
level-INFO  - Starting Servlet engine: [Apache Tomcat/9.0.68]
level-INFO  - Initializing Spring embedded WebApplicationContext
level-INFO  - Root WebApplicationContext: initialization completed in 1421 ms
# 程序运行在 docker 容器内的 8081 端口，与 spring boot 中的端口配置一致
level-INFO  - Tomcat started on port(s): 8081 (http) with context path ''
level-INFO  - Started Application in 2.671 seconds (JVM running for 3.473)
hewenyu@hewenyu:~/docker/hello_docker$
```

## 2.4、宿主机访问容器

```shell
# 宿主机的 8088 端口映射到 hd1 容器的 8081 端口，因此宿主机需要使用 8088 端口访问
hewenyu@hewenyu:~/docker/hello_docker$ curl http://localhost:8088/hello_docker
hello docker...
hewenyu@hewenyu:~/docker/hello_docker$
```

> 此时再次查看 hd1 容器的日志

```shell
hewenyu@hewenyu:~/docker/hello_docker$ docker logs hd1

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v2.6.13)

level-INFO  - Starting Application using Java 21.0.11 on 43cd2744a051 with PID 1 (/app/app.jar started by root in /app)
level-INFO  - No active profile set, falling back to 1 default profile: "default"
level-INFO  - Tomcat initialized with port(s): 8081 (http)
level-INFO  - Starting service [Tomcat]
level-INFO  - Starting Servlet engine: [Apache Tomcat/9.0.68]
level-INFO  - Initializing Spring embedded WebApplicationContext
level-INFO  - Root WebApplicationContext: initialization completed in 1421 ms
level-INFO  - Tomcat started on port(s): 8081 (http) with context path ''
level-INFO  - Started Application in 2.671 seconds (JVM running for 3.473)
level-INFO  - Initializing Spring DispatcherServlet 'dispatcherServlet'
level-INFO  - Initializing Servlet 'dispatcherServlet'
level-INFO  - Completed initialization in 2 ms
# 这条日志为 /hello_docker 接口的日志
=================hello docker=================
```