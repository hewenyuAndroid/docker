# 1、`Dockerfile` 简介

`Dockerfile` 是用于构建 `Docker` 镜像的脚本文件，由一系列指令构成。通过 `docker build` 命令构建镜像时，`Dockerfile` 中的指令会由上到下依次执行，每条指令都将会构建出一个镜像。这就是镜像的分层。因此，指令越多，层次就越多，创建的镜像就越多，效率就越低。所以在定义 `Dockerfile` 时，能在一个指令完成的动作就不要分为两条。

# 2、`Dockerfile` 指令

对于 `Dockerfile` 的指令，需要注意以下几点：

- 指令是大小写不敏感的，但惯例是写为全大写。
- 指令后至少会携带一个参数。
- `#` 号开头的行为注释。

## 2.1、`FROM`-指定基础镜像

```shell
FROM [--platform=<平台>] <镜像名>[:<标签>] [AS <阶段名>]

# 案例
FROM python:3.11-slim AS base
# 基于 python:3.11-slim 作为基础镜像。
# 将该阶段命名为 base，后续可通过 COPY --from=base 引用此阶段的产物。
```

| 部分                | 含义                                                                      |
| ------------------- | ------------------------------------------------------------------------- |
| `--platform=<平台>` | （可选）指定目标平台，例如 `linux/amd64`、`linux/arm64`，用于多架构构建。 |
| `<镜像名>`          | 基础镜像的名称，如 `ubuntu`、`node`、`python`。                           |
| `:<标签>`           | （可选）镜像版本标签，如 `22.04`、`18-alpine`。省略则默认 `latest`。      |
| `AS <阶段名>`       | （可选）为当前构建阶段命名，用于多阶段构建中引用。                        |

> 注意: `FROM` 指令必须是第一条指令;

## 2.2、`WORKDIR`-设置工作目录

```shell
WORKDIR <绝对路径>

# 案例
WORKDIR /app
# 之后的所有命令都在 /app 目录下执行。
# 如果 /app 不存在，Docker 会自动创建它。
# 建议始终使用绝对路径，避免歧义。
```

| 部分         | 含义                                                                                                              |
| ------------ | ----------------------------------------------------------------------------------------------------------------- |
| `<绝对路径>` | 容器内的目录路径，若不存在会自动创建。后续的 `RUN`、`CMD`、`ENTRYPOINT`、`COPY`、`ADD` 均以此目录为当前工作目录。 |

## 2.3、`COPY`-复制文件或目录

```shell
COPY [--chown=<用户>:<组>] [--chmod=<权限>] <源路径>... <目标路径>

# 案例，将构建上下文中的 package.json 和 yarn.lock 文件拷贝到 /app/ 目录下
COPY package.json yarn.lock /app/
```

- 如果目标路径以斜杠结尾（如 `/app/`），表示它是一个目录；否则视为文件名（若存在同名目录则可能出错）。
- 源路径是相对于构建上下文的，不能使用 `../` 跳出上下文。

| 部分                  | 含义                                                                             |
| --------------------- | -------------------------------------------------------------------------------- |
| `--chown=<用户>:<组>` | （可选）设置复制后文件的所有者和所属组，如 `1000:1000` 或 `node:node`。          |
| `--chmod=<权限>`      | （可选）设置复制后文件的权限模式，如 `755`、`644`。                              |
| `<源路径>`            | 构建上下文中的文件或目录路径（可以是多个，用空格分隔）。支持通配符，如 `\*.py`。 |
| `<目标路径>`          | 容器内的目标路径，可以是绝对路径或相对 `WORKDIR` 的相对路径。                    |

## 2.4、`ADD`-增强版复制

```shell
ADD [--chown=<用户>:<组>] [--chmod=<权限>] <源路径>... <目标路径>

# 案例
ADD app.tar.gz /opt/
# 将 app.tar.gz 解压到 /opt/ 目录下，相当于先复制再解压。
```

参数与 `COPY` 相同，但是增加了两个特性:

1. **自动解压**：如果源路径是一个本地 `tar` 压缩文件（`.tar`、`.tar.gz`、`.tgz`、.`bz2` 等），会自动解压到目标目录。
2. **远程 `URL`**：源路径可以是 URL，Docker 会下载该文件并放入目标路径（但不会自动解压远程 tar 包）。

## 2.5、`RUN`-执行构建命令

```shell
# shell 形式
RUN <命令>
# exec 形式
RUN ["<可执行文件>", "<参数1>", ...]

# shell 形式案例
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
# 更新包列表、安装 curl、清理缓存。
# 使用 && 连接多条命令，减少镜像层数（每条 RUN 指令生成一层）。
# 下面的三条指令会生成三个镜像
RUN apt-get update
RUN apt-get install -y curl
RUN rm -rf /var/lib/apt/lists/*

# exec形式案例
RUN ["pip", "install", "-r", "requirements.txt"]
# 直接调用 pip，不经过 shell，更安全（避免字符串转义问题）。
```

每条 `RUN` 指令都会生成一个镜像层，使用 `shell` 模式时，可以将多条指令连接成一条 `RUN` 指令，这样只会产生一层镜像


### 2.5.1、`shell` 模式与 `exec` 模式

- `shell` 模式：命令交给 `/bin/sh -c` 去执行，能用 `shell` 的所有功能。
- `exec` 模式：`Docker` 直接调用程序，不经过 `shell`，参数必须拆成 `JSON` 数组。

| 差异点                      | `shell` 模式                    | `exec` 模式               |
| --------------------------- | ------------------------------- | ------------------------- |
| 写法​                       | `CMD node app.js`               | `CMD ["node", "app.js"]`  |
| 是否经过 `shell`​           | ✅ 是（`/bin/sh -c`）           | ❌ 否（直接 `exec`）      |
| `$VAR` 变量替换​            | ✅ 会展开                       | ❌ 不展开（原样当字符串） |
| 支持 `&&、管道、>​`         | ✅ 支持                         | ❌ 不支持                 |
| `PID 1` 进程​               | `shell` 进程                    | 你的程序​                 |
| `docker stop` 信号转发​     | ⚠️ `shell` 可能不转发，程序强杀 | ✅ 信号直达程序，优雅退出 |
| `RUN` 合并命令减层数​       | ✅ 可以 `&&` 连写               | ❌ 每条 `RUN` 单独一层    |
| 无 shell 的镜像（scratch）​ | ❌ 报错                         | ✅ 可用                   |

> 变量展开规则

```shell
ENV NAME=app
CMD ["echo", "$NAME"]        # 输出字面量 $NAME，不是 app
CMD echo "$NAME"            # 输出 app
```

> 容器停不掉/强制杀死（信号处理）

- `shell` 模式：`PID 1` 是 `sh`，`docker stop` 发 `SIGTERM` 给 `sh`，`sh` 默认不传给子进程 → 程序不退出 → 10 秒后强制 `SIGKILL`。
- `exec` 模式：`PID 1` 是 `node/python/java` → 直接收到 `SIGTERM` → 程序可以优雅关闭。

> 镜像莫名其妙变大（层数问题）

```shell
# shell 模式：1 层，装完就清缓存，镜像小
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# exec 模式：3 层，缓存清不掉，镜像大
RUN ["apt-get", "update"]
RUN ["apt-get", "install", "-y", "curl"]
RUN ["rm", "-rf", "/var/lib/apt/lists/*"]  # 删了也只在最上层标记删除，下层还在
```

> 使用建议:

- `RUN` 用 `shell`（要 `&&` 合并命令、清缓存、用变量）
- `CMD/ENTRYPOINT` 用 `exec`（要信号直达、优雅停止）
- `exec` 里要用变量 → 让程序自己读 ENV，别在命令里传 $VAR

## 2.6、`ENV`-设置环境变量

```shell
ENV <键>=<值> [<键2>=<值2> ... ]
# 旧式写法，不推荐（值中包含空格时容易混淆）
ENV <键> <值>

# 案例
ENV NODE_ENV=production APP_PORT=3000
# 设置两个环境变量：NODE_ENV 为 production，APP_PORT 为 3000
```

| 部分        | 含义                                                                                                                          |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------- |
| `<键>=<值>` | 键值对，多个可以用空格分隔。值可以包含空格，但如果值本身包含空格或特殊字符，建议用双引号包裹，如 `ENV MY_VAR="hello world"`。 |

- `ENV`变量在构建过程和容器运行时都可用;
- ​每个 `ENV` 指令会创建一个新的镜像层，因此过多的 `ENV` 会增加镜像层数。可以在一行内设置多个变量。

> 使用 ENV 环境变量

```shell
# 在 RUN 指令中使用 ENV 环境变量
ENV APP_DIR=/opt/myapp
RUN mkdir -p $APP_DIR && echo "directory created at $APP_DIR"


# 在 CMD 或 ENTRYPOINT 中使用 ENV 环境变量
ENV NODE_ENV=production
# 这里使用了 exec 形式（JSON 数组），变量 $NODE_ENV 不会被 shell 展开，最终传给 node 的参数是字面量 $NODE_ENV。
CMD ["node", "app.js", "--env", "$NODE_ENV"]


# 在 COPY 或 ADD 命令中使用
ENV SRC_FILE=config.prod.json
# COPY 和 ADD 的源路径支持使用ENV环境变量替换，目标路径不支持
COPY $SRC_FILE /app/config.json
```

## 2.7、`EXPOSE`-声明端口

```shell
EXPOSE <端口>[/<协议>] [<端口2>[/<协议>] ...]


# 案例
EXPOSE 80/tcp 443/udp
# 声明容器在运行时监听 TCP 80 端口和 UDP 443 端口。
```

| 部分      | 含义                                               |
| --------- | -------------------------------------------------- |
| `<端口>`  | 容器内应用监听的端口号。                           |
| `/<协议>` | （可选）协议类型，默认为 `tcp`，也可以指定 `udp`。 |

> 这只是文档性质，实际端口映射仍需要在 `docker run` 时使用 `-p` 或 `-P` 参数

## 2.8、`CMD`-容器启动时的默认命令

```shell
# # shell 形式
CMD <命令>
# exec 形式（推荐）
CMD ["<可执行文件>", "<参数1>", ...]
# 作为 ENTRYPOINT 的默认参数
CMD ["<参数1>", "<参数2>", ...]


# 案例
CMD ["node", "app.js"]
# 容器启动时运行 node app.js
```

| 形式          | 说明                                                                             |
| ------------- | -------------------------------------------------------------------------------- |
| `shell` 形式​ | 通过 `/bin/sh -c` 执行命令，`PID` 为 `1` 的是 `shell` 进程，信号处理可能有问题。 |
| `exec` 形式​  | 直接运行指定程序，`PID` 为 `1` 的是该程序本身，能正确接收信号（如 `SIGTERM`）。  |
| 参数列表形式​ | 仅提供参数，此时必须搭配 `ENTRYPOINT` 使用，作为 `ENTRYPOINT` 的默认参数。       |

- 如果在 `docker run` 后面附加了其他命令（如 `docker run myimage bash`），则 `CMD` 会被覆盖，改为运行 `bash`。
- 一个 `Dockerfile` 中只能有一个 `CMD`，如果有多个则只有最后一个生效;

## 2.9、`ENTRYPOINT`-容器入口点

```shell
# shell 形式
ENTRYPOINT <命令>
# exec 形式（推荐）
ENTRYPOINT ["<可执行文件>", "<参数1>", ...]


# 案例
ENTRYPOINT ["python"]
CMD ["-c", "print('Hello')"]
# 容器启动时执行 python -c "print('Hello')"
# 如果运行 docker run myimage script.py，则实际执行 python script.py（CMD 被覆盖，ENTRYPOINT 保持不变）。
```

- `ENTRYPOINT` 定义的命令不会被​ `docker run` 后面的命令行参数覆盖。
- `docker run` 后面的参数会作为 `ENTRYPOINT` 的参数追加（如果 `ENTRYPOINT` 是 `exec` 形式）。
- 通常 `ENTRYPOINT` 固定程序的入口，`CMD` 提供默认参数。
- 尽量使用 `exec` 形式的 `ENTRYPOINT`，以便正确处理信号。

## 2.10、`ARG`-构建时的变量

```shell
ARG <变量名>[=<默认值>]

# 案例
ARG VERSION=1.0
RUN echo "Building version $VERSION"
# 构建时可以这样传值：docker build --build-arg VERSION=2.0 .
```

| 部分        | 含义                                                                      |
| ----------- | ------------------------------------------------------------------------- |
| `<变量名>`  | 变量名称，在 `docker build` 时可以通过 `--build-arg <变量名>=<值>` 传入。 |
| `=<默认值>` | （可选）变量的默认值，如果没有传入则使用该默认值。                        |

- `ARG` 只在构建过程中有效，容器运行时无法获取（除非再次用 `ENV` 赋值）。
- `ENV` 在容器构建时和运行时都有效;




# 3、构建镜像


在构建自己的镜像之前，首先要了解一个特殊的镜像 `scratch`。 `scratch` 镜像是一个空镜像，是所有镜像的 `Base Image`（相当于面向对象编程中的`Object`类）。`scratch` 镜像只能在 `Dockerfile` 中被继承，不能通过 `pull`命令拉取，不能`run`，也没有`tag`。并且它也不会生成镜像中的文件系统层。在`Docker`中，`scratch`是一个保留字，用户不能作为自己的镜像名称使用。


## 3.1、构建一个 `hello world` 镜像 (本地编译+Docker打包)

### 3.1.1、安装 `gcc` 编译器

```shell
# 更新软件包版本 && 安装 gcc
sudo apt update && sudo apt-get install -y gcc
# 查看 gcc 版本
gcc --version
```

### 3.1.2、编写程序并编译

```shell
hewenyu@hewenyu:~/docker$ cat hello.c
#include <stdio.h>

int main() {
    printf("Hello, World from C!\n");
    return 0;
}
```

> 编译

```shell
# hewenyu@hewenyu:~/docker$ gcc -o hello hello.c
# 注意，这里要使用 静态编译，
hewenyu@hewenyu:~/docker$ gcc --static -o hello hello.c
hewenyu@hewenyu:~/docker$ ls
hello  hello.c
hewenyu@hewenyu:~/docker$ ./hello
Hello, World from C!
```

### 3.1.3、编写 `Dockerfile`

```shell
hewenyu@hewenyu:~/docker$ ls
# Dockerfile 和 hello 文件在同一个目录下
Dockerfile  hello  hello.c
hewenyu@hewenyu:~/docker$ cat Dockerfile
# 继承空镜像
FROM scratch
# 将 hello 文件拷贝到镜像的根目录
COPY hello /
# 执行 hello 文件
CMD ["/hello"]
```

### 3.1.4、构建镜像

```shell
# 最后的 . 不要忘记
hewenyu@hewenyu:~/docker$ docker build -t my-hello-world:1.0 .
[+] Building 1.0s (5/5) FINISHED                                                                         docker:default
 => [internal] load build definition from Dockerfile                                                               0.0s
 => => transferring dockerfile: 167B                                                                               0.0s
 => [internal] load .dockerignore                                                                                  0.1s
 => => transferring context: 2B                                                                                    0.0s
 => [internal] load build context                                                                                  0.1s
 => => transferring context: 15.99kB                                                                               0.0s
 => [1/1] COPY hello /                                                                                             0.1s
 => exporting to image                                                                                             0.5s
 => => exporting layers                                                                                            0.2s
 => => exporting manifest sha256:5f634d0dfaf0e3f9aea52c26e23701d0a5294a568e0215c75fc8eb65036dcf44                  0.0s
 => => exporting config sha256:4f6bdecc9d135dc7d5b655c4b6db0e4429ee8a01f99d0595edc76817c800b048                    0.0s
 => => exporting attestation manifest sha256:e757425edf506fefd9efd2ee3090052e75adca141f4708ceee75c637f818c7ad      0.1s
 => => exporting manifest list sha256:96a85ad12b03b9e8759c968c4d1f73d5b883f5d84b8fb06b112b57de1b1cd1aa             0.0s
 => => naming to docker.io/library/my-hello-world:1.0                                                              0.0s
 => => unpacking to docker.io/library/my-hello-world:1.0                                                           0.1s
hewenyu@hewenyu:~/docker$
```

- `-t`用于指定要生成的镜像的`<repository>`与`<tag>`。若省略`tag`，则默认为`latest`。
- 最后的点 `.` 是一个宿主机的`URL`路径，构建镜像时会从该路径中查找`Dockerfile`文件。同时该路径也是在`Dockerfile`中`ADD`、`COPY`指令中若使用的是相对路径，那个相对路径就相对的这个路径。不过需要注意，即使`ADD`、`COPY`指令中使用绝对路径来指定源文件，该源文件所在路径也必须要在这个URL指定目录或子目录内，否则将无法找到该文件。

### 3.1.5、查看并执行镜像

```shell
# 查看镜像
hewenyu@hewenyu:~/docker$ docker images my-hello-world:1.0
                                                                                                    i Info →   U  In Use
IMAGE                ID             DISK USAGE   CONTENT SIZE   EXTRA
my-hello-world:1.0   96a85ad12b03       25.5kB         4.97kB
hewenyu@hewenyu:~/docker$

# 运行镜像
hewenyu@hewenyu:~/docker$ docker run my-hello-world:1.0
Hello, World from C!
```


