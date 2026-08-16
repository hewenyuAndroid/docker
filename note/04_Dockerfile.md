

# 1、`Dockerfile` 简介

`Dockerfile` 是用于构建 `Docker` 镜像的脚本文件，由一系列指令构成。通过 `docker build` 命令构建镜像时，`Dockerfile` 中的指令会由上到下依次执行，每条指令都将会构建出一个镜像。这就是镜像的分层。因此，指令越多，层次就越多，创建的镜像就越多，效率就越低。所以在定义 `Dockerfile` 时，能在一个指令完成的动作就不要分为两条。

# 2、`Dockerfile` 指令

对于 `Dockerfile` 的指令，需要注意以下几点： 
- 指令是大小写不敏感的，但惯例是写为全大写。 
- 指令后至少会携带一个参数。 
- `#` 号开头的行为注释。

## 2.1、`FROM`

> `FROM <image>[:<tag>]`

用于指定基础镜像，且必须是第一条指令；若省略了 `tag`，则默认为`latest`。

## 2.2、`MAINTAINER`

> `MAINTAINER <name>`

`MAINTAINER` 指令的参数填写的一般是维护者姓名和信箱。不过，该指令官方已不建议使用，而是使用 `LABEL` 指令代替。

