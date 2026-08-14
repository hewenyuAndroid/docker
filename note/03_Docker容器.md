[toc]

# 1、`Docker` 容器

## 1.1、什么是 `Docker` 容器

- 容器时基于镜像创建的一个可运行的实例，可以启动、停止、删除。
- 每个容器相互隔离，拥有自己的文件系统、网络、进程空间。
- 容器与镜像的关系类似 面向对象中的“类”与“对象”：镜像是静态的只读模板，容器是动态的运行环境。

> 容器的完整生命周期

- `docker run` = `docker create` + `docker start`（一步到位创建并启动新容器）
- `docker start` 只能用于已经存在的容器（`Created` 或 `Stopped` 状态）

```xml
docker create
                         │
                         ▼
                    ┌─────────┐
                    │ Created │ （已创建，未运行）
                    └─────────┘
                         │
                    docker start
                         ▼
                    ┌─────────┐
                    │ Running │ （运行中）
                    └─────────┘
                         │
                    docker stop
                         ▼
                    ┌─────────┐
                    │ Stopped │ （已停止）
                    └─────────┘
                         │
                    docker rm
                         ▼
                      （被删除）
```

## 1.2、`Docker` 容器运行的本质

`Docker` 容器存在的意义就是为了运行容器中的应用，对外提供服务，所以启动容器的目的就是启动运行该容器中的应用。容器中的应用运行完毕后，容器就会自动终止。所以，如果不想让容器启动后立即终止运行，则就需要使容器应用不能立即结束。通常采用的方式有两种，使应用处于 **与用户交互的状态** 或 **等待状态** 。

## 1.3、`Docker` 容器的启动流程

![docker 容器启动流程](../imgs/docker_container_run.png)

通过 `docker run` 命令可以启动运行一个容器。该命令在执行时首先会在本地查找指定的镜像，如果找到了，则直接启动，否则会到镜像中心查找。如果镜像中心存在该镜像，则会下载到本地并启动，如果镜像中心也没有，则直接报错。

如果再与多架构镜像原理相整合，则就形成了完整的容器启动流程。

![docker 多架构镜像容器启动流程](../imgs/docker_container_run_multi.png)

## 1.4、`docker` 容器命令

### 1.4.1、创建类(生成新容器)

| 命令                   | 作用                  |
| ---------------------- | --------------------- |
| `docker create 镜像名` | 仅创建容器（不启动）  |
| `docker run 镜像名`    | 创建 + 启动（最常用） |

> 注意: `docker run` 每次都会创建新容器，重复执行会创建多个；要复用同一个容器，用 `docker start`。

### 1.4.2、生命周期管理类（操作已存在的容器）

| 命令                    | 作用                 |
| ----------------------- | -------------------- |
| `docker start 容器名`   | 启动已停止的容器     |
| `docker stop 容器名`    | 优雅停止容器         |
| `docker restart 容器名` | 重启容器             |
| `docker pause 容器名`   | 暂停容器（冻结进程） |
| `docker unpause 容器名` | 恢复暂停的容器       |
| `docker rm 容器名`      | 删除已停止的容器     |
| `docker rm -f 容器名`   | 强制删除（含运行中） |

### 1.4.3、查看类

| 命令                    | 作用                     |
| ----------------------- | ------------------------ |
| `docker ps`             | 查看运行中的容器         |
| `docker ps -a`          | 查看所有容器（含已停止） |
| `docker logs 容器名`    | 查看日志                 |
| `docker inspect 容器名` | 查看详细信息             |
| `docker stats 容器名`   | 查看资源占用             |
| `docker top 容器名`     | 查看容器内进程           |

### 1.4.4、交互类

| 命令                           | 作用                       |
| ------------------------------ | -------------------------- |
| `docker exec -it 容器名 shell` | 在容器内执行新命令（推荐） |
| `docker attach 容器名`         | 连接到容器主进程           |


## 1.5、容器的创建与启动

对于容器的运行，有两种运行模式：交互模式与分离模式。下面通过运行 `ubuntu` 与 `tomcat` 来演示这两种运行模式的不同。


### 1.5.1、交互模式运行 `ubuntu`

```shell
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run --name myubuntu -it ubuntu:latest /bin/bash
# 本地没有 ubuntu:latest 镜像
Unable to find image 'ubuntu:latest' locally
# 从远程拉取
latest: Pulling from library/ubuntu
a7fb98a8eddd: Pull complete
617772c7d19b: Pull complete
cc2ffdbc1bf7: Download complete
Digest: sha256:678c6550cc43645e08669028bc177f50be4e7c5b8cca677067b1914d4afc7a03
Status: Downloaded newer image for ubuntu:latest
# 镜像拉取到本地后，运行成为容器
root@ff86e9d07d1d:/#

# ff86e9d07d1d 表示的是 docker 容器的id
root@ff86e9d07d1d:/# pwd
/
# 容器中的系统是一个精简系统，很多命令是没有的
root@ff86e9d07d1d:/# ifconfig
bash: ifconfig: command not found
root@ff86e9d07d1d:/# touch a.txt
root@ff86e9d07d1d:/# ls
a.txt  bin  boot  dev  etc  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
root@ff86e9d07d1d:/# vim a.txt
bash: vim: command not found
root@ff86e9d07d1d:/# nano a.txt
bash: nano: command not found
root@ff86e9d07d1d:/# vi a.txt
bash: vi: command not found
root@ff86e9d07d1d:/#
```

`docker run --name myubuntu -it ubuntu:latest /bin/bash` 指令解析:

- `docker run`: 创建并运行一个新的容器
- `--name myubuntu`: 配置运行的容器名称
- `-it`: 指定以交互模式运行容器，且为容器分配一个伪终端。
- `ubuntu:latest`: 启动的容器镜像;
- `/bin/bash`: 用于指定容器启动后需要运行的命令为 `/bin` 下的 `bash` 命令，该命令会启动一个 `bash` 终端;


由于容器中的该系统是一个精简的系统，有很多常用命令是没有安装的，所以如果要使用这些命令，就需要安装;


### 1.5.2、交互模式运行 `tomcat`

#### 1.5.2.1、启动容器进入终端，不启动 `tomcat`

```shell
# 以交互模式启动 tomcat 并配置了 /bin/bash 终端
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run --name tomcat8 -it tomcat:8.5.32 /bin/bash
Unable to find image 'tomcat:8.5.32' locally
8.5.32: Pulling from library/tomcat
2dcc81b69024: Pull complete
1290813abd9d: Pull complete
abb029e68402: Pull complete
9a8ea045c926: Pull complete
d068d0a738e5: Pull complete
42ee47bb0c52: Pull complete
ae9c861aed25: Pull complete
8a6b982ad6d7: Pull complete
1607093a898c: Pull complete
60bba9d0dc8d: Pull complete
55cbf04beb70: Pull complete
15222e409530: Pull complete
Digest: sha256:bbdb0de8298ab7281ff28331a9e4129562820ac54e243e44c3749f389876f562
Status: Downloaded newer image for tomcat:8.5.32
# 容器起来后，并没有启动 tomcat，而是进入了 /bin/bash 终端
root@2f1ff3ac9072:/usr/local/tomcat# ls
LICENSE  NOTICE  RELEASE-NOTES  RUNNING.txt  bin  conf  include  lib  logs  native-jni-lib  temp  webapps  work
root@2f1ff3ac9072:/usr/local/tomcat#
# 查看容器的进程信息
root@2f1ff3ac9072:/usr/local/tomcat# ls /proc/
1          config.gz  driver         iomem      kmsg           meminfo  pagetypeinfo  softirqs       timer_list
9          consoles   dynamic_debug  ioports    kpagecgroup    misc     partitions    stat           tty
acpi       cpuinfo    execdomains    irq        kpagecount     modules  pressure      swaps          uptime
buddyinfo  crypto     fb             kallsyms   kpageflags     mounts   schedstat     sys            version
bus        devices    filesystems    kcore      latency_stats  mpt      scsi          sysrq-trigger  vmallocinfo
cgroups    diskstats  fs             key-users  loadavg        mtrr     self          sysvipc        vmstat
cmdline    dma        interrupts     keys       locks          net      slabinfo      thread-self    zoneinfo
root@2f1ff3ac9072:/usr/local/tomcat#
```

关键点：`/bin/bash` 的作用

- `Tomcat` 官方镜像的默认 `CMD` 是 `catalina.sh run`（启动 `Tomcat` 服务器）。
- 在命令末尾显式写了 `/bin/bash`，这会覆盖默认的 `CMD`，容器启动后不再启动 `Tomcat`，而是直接运行 `Bash Shell`。
- 因为加了 `-it`，会立即进入容器的 `Bash` 终端（`root@2f1ff3ac9072:/usr/local/tomcat#`）。

#### 1.5.2.2、启动容器，并启动`tomcat`

```shell
# 以交互模式运行tomcat，不带 /bin/bash，此时会默认启动 tomcat
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run --name tomcat8081 -it -p 8081:8080 tomcat:8.5.32
Using CATALINA_BASE:   /usr/local/tomcat
Using CATALINA_HOME:   /usr/local/tomcat
Using CATALINA_TMPDIR: /usr/local/tomcat/temp
Using JRE_HOME:        /docker-java-home/jre
Using CLASSPATH:       /usr/local/tomcat/bin/bootstrap.jar:/usr/local/tomcat/bin/tomcat-juli.jar
14-Aug-2026 07:18:23.241 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server version:        Apache Tomcat/8.5.32
14-Aug-2026 07:18:23.245 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server built:          Jun 20 2018 19:50:35 UTC
14-Aug-2026 07:18:23.245 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server number:         8.5.32.0
14-Aug-2026 07:18:23.245 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log OS Name:               Linux
14-Aug-2026 07:18:23.246 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log OS Version:            6.6.87.2-microsoft-standard-WSL2
...
14-Aug-2026 07:18:24.222 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler ["http-nio-8080"]
14-Aug-2026 07:18:24.235 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler ["ajp-nio-8009"]
14-Aug-2026 07:18:24.242 INFO [main] org.apache.catalina.startup.Catalina.start Server startup in 778 ms

```

`docker run --name tomcat8080 -it -p 8080:8081 tomcat:8.5.32` 命令没有 `/bin/bash`，此时会真正启动 `tomcat`;

`-p 8080:8081` 为端口映射参数

- 左侧 `8081`：宿主机对外暴露的端口。
- 右侧 `8080`：容器内 `Tomcat` 默认监听的端口（在 `server.xml` 中配置）。

这是一个正确的映射：访问 `http://宿主机IP:8081` 即可访问容器内的 Tomcat 服务。


> 在 `tomcat` 容器的宿主机 `wsl` 上访问 `tomcat`

![在wsl访问容器中的tomcat](../imgs/docker_run_tomcat_client_in_wsl.png)

> 在 `wsl` 的宿主机 `win11` 上访问 `tomcat` 

![在wsl的宿主机win11上访问tomcat](../imgs/docker_run_tomcat_client_in_win.png)



### 1.5.3、分离模式运行 `tomcat`


```shell
# 分离模式启动一个 tomcat 容器
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run --name mytomcat1 -dp 8081:8080 tomcat:8.5.32
93251bcb87f37d01434908d97d1e1351a8b8e853d8289867fb06d99235f03556
# 分离模式再启动一个 tomcat 容器
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run --name mytomcat2 -dp 8082:8080 tomcat:8.5.32
5a9d06f00d94e371354fa12f7e3b98ba6c30b5dd631e2ff36f168ed45fddebeb
hewenyu@hewenyu:/mnt/c/Users/he875$ docker ps
# status Up 表示当前容器在运行
CONTAINER ID   IMAGE           COMMAND             CREATED          STATUS          PORTS                                         NAMES
5a9d06f00d94   tomcat:8.5.32   "catalina.sh run"   9 seconds ago    Up 8 seconds    0.0.0.0:8082->8080/tcp, [::]:8082->8080/tcp   mytomcat2
93251bcb87f3   tomcat:8.5.32   "catalina.sh run"   18 seconds ago   Up 17 seconds   0.0.0.0:8081->8080/tcp, [::]:8081->8080/tcp   mytomcat1
hewenyu@hewenyu:/mnt/c/Users/he875$
```

`-d` 选项表示以分离模式（`detached mode`）运行容器，即命令在后台运行，命令的运行与宿主机的运行分离开来。

```shell
# 不添加 -d 参数时，会占用当前终端
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run --name mytomcat3 -p 8083:8080 tomcat:8.5.32
14-Aug-2026 08:19:17.003 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server version:        Apache Tomcat/8.5.32
14-Aug-2026 08:19:17.007 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server built:          Jun 20 2018 19:50:35 UTC
14-Aug-2026 08:19:17.007 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log Server number:         8.5.32.0
14-Aug-2026 08:19:17.008 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log OS Name:               Linux
14-Aug-2026 08:19:17.008 INFO [main] org.apache.catalina.startup.VersionLoggerListener.log OS Version:            6.6.87.2-microsoft-standard-WSL2
...
14-Aug-2026 08:19:17.930 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler ["http-nio-8080"]
14-Aug-2026 08:19:17.940 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler ["ajp-nio-8009"]
14-Aug-2026 08:19:17.948 INFO [main] org.apache.catalina.startup.Catalina.start Server startup in 753 ms

```

### 1.5.4、分离了模式运行 `ubuntu`


```shell
hewenyu@hewenyu:/mnt/c/Users/he875$ docker images
                                                                                                                 i Info →   U  In Use
IMAGE           ID             DISK USAGE   CONTENT SIZE   EXTRA
redis:7.2       6461ca4ac0c5        169MB         45.6MB
tomcat:8.5.32   bbdb0de8298a        710MB          195MB    U
ubuntu:latest   678c6550cc43        160MB         45.3MB    U
# 分离模式，在后台启动 ubuntu:latest 镜像的容器
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run -d ubuntu:latest
839b43c3b8afad6838f832b1bba2fc73186b5174154465bcd87dc9c81958e254
# 查看正在运行的容器，没有找到新创建的 ubuntu 容器
hewenyu@hewenyu:/mnt/c/Users/he875$ docker ps
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
# docker ps -a 可以看到 ubuntu 的容器起来后退出了
hewenyu@hewenyu:/mnt/c/Users/he875$ docker ps -a
CONTAINER ID   IMAGE           COMMAND             CREATED             STATUS                        PORTS     NAMES
839b43c3b8af   ubuntu:latest   "/bin/bash"         7 seconds ago       Exited (0) 6 seconds ago                cranky_brattain
fe991e4a5d35   ubuntu:latest   "/bin/bash"         55 seconds ago      Exited (0) 54 seconds ago               magical_maxwell
a336afe7e4b3   tomcat:8.5.32   "catalina.sh run"   59 minutes ago      Exited (130) 58 minutes ago             mytomcat3
5a9d06f00d94   tomcat:8.5.32   "catalina.sh run"   About an hour ago   Exited (143) 24 seconds ago             mytomcat2
93251bcb87f3   tomcat:8.5.32   "catalina.sh run"   About an hour ago   Exited (143) 24 seconds ago             mytomcat1
304ab9fac4c3   7f4da0fc94bc    "/hello"            2 days ago          Exited (0) 2 days ago                   exciting_banach
hewenyu@hewenyu:/mnt/c/Users/he875$
```

`docker run -d ubuntu:latest` 命令启动容器会立刻变成 `Exited`​ 状态，原因：

- `Ubuntu` 镜像默认命令是 `/bin/bash`。
- `bash` 在后台模式下没有 `-it` 分配终端，启动后检测到没有输入，立即退出。
- 容器主进程（`bash`）结束 → 容器停止。

结论：要让 `Ubuntu` 容器在后台持续运行，必须给它一个永不退出的前台进程。

解决方案：

> 方案1：`tail -f /dev/null`（最推荐）

`docker run -d --name myubuntu ubuntu tail -f /dev/null`

- `tail -f /dev/null` 一直阻塞读取空文件，永不退出。
- 容器保持 Up 状态。

```shell
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run -d ubuntu tail -f /dev/null
ac69a952042c318f39b573cf3453fb685a067ef5af73ce1d1a411c8f512d54ae
hewenyu@hewenyu:/mnt/c/Users/he875$ docker ps
CONTAINER ID   IMAGE     COMMAND               CREATED         STATUS         PORTS     NAMES
ac69a952042c   ubuntu    "tail -f /dev/null"   9 seconds ago   Up 8 seconds             distracted_gauss
hewenyu@hewenyu:/mnt/c/Users/he875$
```

> 方案2: `sleep infinity`,无限休眠，保持容器运行。

```shell
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run --name u1 -d ubuntu:latest sleep infinity
65e58075346ab8e5f8f2682b1896c445ee7df7c1af7917deba9c9a5d23367ec2
hewenyu@hewenyu:/mnt/c/Users/he875$ docker ps
CONTAINER ID   IMAGE           COMMAND            CREATED         STATUS         PORTS     NAMES
65e58075346a   ubuntu:latest   "sleep infinity"   5 seconds ago   Up 4 seconds             u1
hewenyu@hewenyu:/mnt/c/Users/he875$
```

> 方案3: bash -c "while true; do sleep 3600; done"

死循环，每隔一小时醒来一次，永不停歇

```shell
hewenyu@hewenyu:/mnt/c/Users/he875$ docker run -d --name myubuntu ubuntu bash -c "while true; do sleep 3600; done"
d6e80acab53caf74d7efa32adf75c042d00780e6146edefe0cee694d0fea0548
hewenyu@hewenyu:/mnt/c/Users/he875$ docker ps
CONTAINER ID   IMAGE           COMMAND                  CREATED          STATUS          PORTS     NAMES
d6e80acab53c   ubuntu          "bash -c 'while true…"   5 seconds ago    Up 5 seconds              myubuntu
65e58075346a   ubuntu:latest   "sleep infinity"         18 minutes ago   Up 18 minutes             u1
```

### 1.5.5、进入后台运行的容器

无论用哪种方法启动后台容器，进入方式相同:

```shell
hewenyu@hewenyu:/mnt/c/Users/he875$ docker ps
CONTAINER ID   IMAGE           COMMAND                  CREATED          STATUS          PORTS     NAMES
179d31a6e4b5   ubuntu:latest   "/bin/bash"              4 minutes ago    Up 4 minutes              ecstatic_bartik
d6e80acab53c   ubuntu          "bash -c 'while true…"   5 minutes ago    Up 5 minutes              myubuntu
65e58075346a   ubuntu:latest   "sleep infinity"         23 minutes ago   Up 23 minutes             u1
# 进入运行的容器，传容器id的方式
hewenyu@hewenyu:/mnt/c/Users/he875$ docker exec -it 179d31a6e4b5 bash
root@179d31a6e4b5:/# ls
bin  boot  dev  etc  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
root@179d31a6e4b5:/#

# 进入运行的容器，传容器名称的方式
hewenyu@hewenyu:/mnt/c/Users/he875$ docker exec -it myubuntu bash
root@d6e80acab53c:/# ls
bin  boot  dev  etc  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
root@d6e80acab53c:/#
```

进入运行的容器内部后，如果想停止容器运行可以使用 `docker stop 容器名称` 的方式。如果想退出容器但是保持容器运行，可以使用 `ctrl+p+q` (先按住 `ctrl+p` 然后按 `q`)的快捷键组合退出容器；
