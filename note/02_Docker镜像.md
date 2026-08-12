# 1、`Docker` 镜像

## 1.1、镜像简介

镜像是一种轻量级、可执行的独立软件包，也可以说是一个精简的操作系统。镜像中包含应用软件及应用软件的运行环境。具体来说镜像包含运行某个软件所需的所有内容，包括代码、库、环境变量和配置文件等。

几乎所有应用，直接打包为Docker镜像后就可以运行。 由于镜像的运行时是容器，容器的设计初衷就是快速和小巧，所以镜像通常都比较小，镜像中不包含内核，其共享宿主机的内核；镜像中只包含简单的Shell，或没有Shell。

## 1.2、镜像仓库分类

镜像中心中存储着大量的镜像仓库 `Image Repository`，每个镜像仓库中包含着大量相关镜像。根据这些镜像发布者的不同，形成了四类不同的镜像仓库。

`docker hub` 地址: https://hub.docker.com/repositories/hewenyudocker

![docker hub](../imgs/docker_hub.png)

### 1.2.1、`Docker Official Image`

`Docker` 官方镜像仓库。该类仓库中的镜像由 `Docker` 官方构建发布，代码质量较高且安全，有较完善的文档。该类仓库中的镜像会及时更新。一般常用的系统、工具软件、中间件都有相应的官方镜像仓库。例如，Zookeeper、Redis、Nginx等。

官方镜像仓库的名称 `<repository>` 一般直接为该类软件的名称 `<software-name>`。

### 1.2.2、`Verified Publisher`

已验证发布者仓库。该类仓库中的镜像由非 `Docker` 官方的第三方发布。但该第三方是由 `Docker` 公司审核认证过的，一般为大型企业、团体或组织。审核通过后，`Docker`公司会向其颁发 `“VERIFIED PUBLISHER”` 标识。这种仓库中镜像的质量还有有保证的。

除了官方镜像仓库，其它都是非官方镜像仓库。非官方镜像仓库名称 `<repository>` 一般由发布者用户名与软件名称两部分构成，形式为：`<username>/<software-name>`。

### 1.2.3、`Sponsored OSS`

由 `Docker` 公司赞助开发的镜像仓库。该类仓库中的镜像也由非 `Docker` 官方的第三方发布，但该镜像的开发是由 `Docker` 公司赞助的。该类型的第三方一般为个人、团队或组织。

这种仓库中镜像的质量也是有保证的。

### 1.2.4、无认证仓库

没有以上任何标识的仓库。这种仓库中镜像的质量良莠不齐，质量上无法保证，在使用时需谨慎。

## 1.3、第三方镜像中心

镜像中心默认使用的都是 `Docker` 官方的 `Docker Hub`。不过，镜像中心是可配置的，可以使用指定的第三方镜像中心。对于第三方镜像中心中的仓库名称 `<repository>`由三部分构成：`<domain-name>/<username>/<software-name>`。其中的 `<domain-name>` 指的是第三方镜像中心的域名或IP。

## 1.4、镜像定位

对于任何镜像，都可通过 `<repository>:<tag>` 进行唯一定位。其中 `<tag>` 一般称为镜像的版本号。`<tag>` 中有一个比较特殊的版本—— `latest`。如果不指定，默认`<tag>`即为 `latest`。不过，虽然其字面意思是最新版，一般其也的确存放的是最新版，但并不能保证其真的就是最新版。

# 2、镜像相关命令

## 2.1、`docker pull`

`docker pull` 是 `Docker` 客户端用于从镜像仓库（默认 `Docker Hub`）下载镜像到本地的核心命令。

`docker pull` 根据镜像名称，从远程 `Registry` 找到指定的镜像，并将本地缺失的镜像数据（`Layers` 等）下载到 `Docker Engine` 的本地镜像存储中。它不是简单地下载一个 `.tar` 文件，而是根据镜像的 `Manifest` 找到组成 `Image` 所需要的多个 `Layer`，然后下载这些 `Layer`。

### 2.1.1、`docker pull` 基本语法

```shell
docker pull [OPTIONS] NAME[:TAG|@DIGEST]

# 完整写法
docker pull docker.io/library/redis:7.4

# 拆解下命令
docker pull [OPTIONS] NAME       :TAG
                         │        │
                         │        └── 7.4
                         │
                         └── docker.io/library/redis


# 简写， 默认的 register是 docker.io ，默认的 namespace 是 library
docker pull redis:7.4
```

- `NAME`：镜像名称。
- `TAG`：标签，默认为 `latest`。例如 `nginx:1.25`。
- `@DIGEST`：通过内容哈希值精确指定镜像版本，如 `nginx@sha256:xxx`。


> 一个完整的 `NAME` 由 `Registry`、`NameSpace`、`Respository` 组成：

- `Registry`: `Docker` 的镜像中心，类似于 `github`，默认为 `docker.io`;
- `NameSpace`: 镜像中心的组织名称，用于解决不同组织之间的 `Respository` 冲突问题，默认为 `library`;
- `Respository`: 镜像仓库，类似于 `maven` 仓库 `gav` 中的 `ga`，表示一个库，这个库可以有很多版本，版本就是 `tag`;

`docker` 镜像中的 `tag` 并不是指向一个完整的 `image` 镜像，`docker` 中的 `image` 镜像按照 `layer` 拆解成多个文件，不同版本可以复用相同的 `layer`，因此 `tag` 指向的是一个 `manifest` 清单文件，这个文件列出了对应 `tag` 版本所需要的 `layer` 文件列表;


> OPTIONS 选项

| 选项                      | 说明                                               |
| ------------------------- | -------------------------------------------------- |
| `-a, --all-tags`          | 拉取仓库中所有标签的镜像                           |
| `--disable-content-trust` | 跳过镜像签名验证（默认启用）                       |
| `--platform`              | 指定目标操作系统/架构，如 linux/amd64, linux/arm64 |
| `-q, --quiet`             | 只显示摘要信息，不输出进度条                       |

> `docker pull` 命令流程拆解

```txt
docker pull [OPTIONS] NAME[:TAG|@DIGEST]
                         │
                         ▼
                       NAME
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
           Registry   Namespace  Repository
              │          │          │
           docker.io   library     redis
                                      │
                              ┌───────┴───────┐
                              ▼               ▼
                            :TAG          @DIGEST
                             7.4          sha256:...
                              │               │
                              └───────┬───────┘
                                      ▼
                                  Manifest
                                      │
                                    Layers
                                      │
                                      ▼
                                    Image
```

### 2.1.2、拉取 `redis`

```shell
hewenyu@hewenyu:/mnt/c/Users/he875$ sudo docker pull redis
[sudo] password for hewenyu:
Using default tag: latest
latest: Pulling from library/redis
910e63375a2f: Download complete
514dfa5816db: Pull complete
a326415e779c: Pull complete
26c307b5e35a: Pull complete
4f4fb700ef54: Pull complete
dbfb374a7f58: Pull complete
65405b53eed3: Pull complete
80c4a8d1ffc0: Pull complete
8e9e522279cf: Download complete
Digest: sha256:344e3945a0b431c8ff1eecd58c5573538126bd756f02fc7e218ddf1fc2546366
Status: Downloaded newer image for redis:latest
docker.io/library/redis:latest
```

> 查看本地镜像

```shell
# docker images 查看本地镜像
hewenyu@hewenyu:/mnt/c/Users/he875$ sudo docker images
                                                                                                    i Info →   U  In Use
IMAGE                ID             DISK USAGE   CONTENT SIZE   EXTRA
hello-world:latest   7f4da0fc94bc       25.9kB         9.49kB    U
redis:latest         344e3945a0b4        212MB         57.4MB
# docker image ls 查看本地镜像
hewenyu@hewenyu:/mnt/c/Users/he875$ sudo docker image ls
                                                                                                    i Info →   U  In Use
IMAGE                ID             DISK USAGE   CONTENT SIZE   EXTRA
hello-world:latest   7f4da0fc94bc       25.9kB         9.49kB    U
redis:latest         344e3945a0b4        212MB         57.4MB
```