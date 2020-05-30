---
title: MQTT征程（一）服务器搭建

tags:
	- MQTT
	- 物联网技术
	- MQTT征程

categories:
  	- 实训笔记
	- MQTT征程
---

​		本来这个网站是打算建在云服务器上，但实操发现GithubPage+CodingPage的方式更适合刚上手的小白（是本人没错了！

​		因此，购买的服务器就搁置了，最近突发奇想一个小型MQTT项目，刚好需要用的资源手头都有，So从这个项目的核心----mosquitto（开源消息代理,实现了MQTT协议版本3.1和3.1.1）开始入手。

# 什么是MQTT

**MQTT(消息队列遥测传输)是ISO 标准下基于发布/订阅范式的消息协议。它工作在 TCP/IP协议族上，是为硬件性能低下的远程设备以及网络状况糟糕的情况下而设计的发布/订阅型消息协议。**

​		MQTT是一个基于客户端-服务器的消息发布/订阅传输协议。MQTT协议是轻量、简单、开放和易于实现的，这些特点使它适用范围非常广泛。在很多情况下，包括受限的环境中，如：机器与机器（M2M）通信和物联网（IoT）。其在，通过卫星链路通信传感器、偶尔拨号的医疗设备、智能家居、及一些小型化设备中已广泛使用。

# 什么是Mosquitto

**Mosquitto**是一款实现了消息推送协议 MQTT v3.1 的开源消息代理软件，提供轻量级的，支持可发布/可订阅的的消息推送模式，使设备对设备之间的短消息通信变得简单，比如现在应用广泛的低功耗传感器，手机、嵌入式计算机、微型控制器等移动设备。它具有强大的社区支持，并且易于安装和配置。



**结合腾讯云社区的教程([物联网入门：搭建MQTT服务器](https://cloud.tencent.com/developer/article/1161563)),以及向学长的求助下,最终完成了服务器的搭建,虽然还有一些地方有待实现(SSL证书加密配置),但基础的发布/订阅功能已经实现,在这里重新整理记录一下步骤**

# 环境配置

+ 一台安装Centos 7系统的服务器
+ nano文本编辑器 `sudo yum -y install nano`

## **第一步、安装Mosquitto**

在默认情况下，CentOS 7没有`mosquitto`程序包。首先我们将安装一个额外的软件软件包，即Epel。这个存储库囊括了安装在CentOS、RedHat和其他面向企业的Linux发行版上的附加软件。

使用`yum`包管理器来安装`epel-release`包。

```javascript
sudo yum -y install epel-release
```

这条命令会将`Epel`存储库信息添加到我们的系统中，`-y`选项在整个过程中对一些提示自动回答“是”。现在我们可以安装`mosquitto`包。

```javascript
sudo yum -y install mosquitto
```

这个包附带了一个简单的默认配置，所以让我们运行它来测试我们的安装。

```javascript
sudo systemctl start mosquitto
```

我们还需要启用服务，以确保它在重新启动系统时启动：

```javascript
sudo systemctl enable mosquitto
```

现在让我们测试默认配置。`mosquitto`包附带了一些MQTT客户端命令行。我们将使用其中一个订阅代理上的**主题。**

**主题**是您发布消息并订阅的标签。在本教程中，我们将使用一个简单的测试主题来测试配置。

您需要创建一个新的终端，创建方法很简单，重新打开一个新的终端页面，重新登录服务器即可。在新的终端中，使用`mosquitto_sub`订阅测试主题：

```javascript
mosquitto_sub -h localhost -t test
```

`-h`用于指定MQTT服务器的主机名，`-t`是主题名。`ENTER`后没有输出，是因为`mosquitto_sub`在等待消息的到来。切换回另一个终端并发布一条消息：

```javascript
mosquitto_pub -h localhost -t test -m "hello world"
```

`mosquitto_pub`的选项与`mosquitto_sub`相同，这一次我们使用了额外的`-m`选项来指定我们的消息。点击`ENTER`，你应该看看`hello world` 输出在在另一个终端中。你已经学会发送第一条MQTT信息！

在第二个终端中按下`CTRL+C`退出`mosquitto_sub`，不要关闭其他终端哦，后面我们还要进行测试。



## **第二步、配置MQTT密码**

Mosquitto包含一个工具，用于生成一个特殊的密码文件，名为`mosquitto_passwd`。此工具将提示您输入指定用户名的密码，并将结果放在`/etc/mosquitto/passwd`.

```javascript
sudo mosquitto_passwd -c /etc/mosquitto/passwd sammy
```

现在，我们将替换默认的配置文件，并告诉Mosquito使用这个密码文件来要求所有连接的登录。首先，删除现有的`mosquitto.conf`.

```javascript
sudo rm /etc/mosquitto/mosquitto.conf
```

现在打开一个新的空配置。

```javascript
sudo nano /etc/mosquitto/mosquitto.conf
```

粘贴在下面。

```javascript
allow_anonymous false
password_file /etc/mosquitto/passwd
```

`allow_anonymous false`将禁用所有未经身份验证的连接，并且`password_file`告诉Mosquitto在哪里查找用户和密码，保存并退出文件。

现在我们需要重新启动Mosquitto并测试。

```
sudo systemctl restart mosquitto
```

尝试在没有密码的情况下发布消息。

```javascript
mosquitto_pub -h localhost -t "test" -m "hello world"
```

你应该看到被拒绝的信息：

```javascript
Connection Refused: not authorised.
Error: The connection was refused.
```

在我们再次尝试使用密码登录之前，请再次切换到您的第二个终端窗口，并使用用户名和密码订阅“test”主题：

```javascript
mosquitto_sub -h localhost -t test -u "sammy" -P "password"
```

现在用另一个终端使用用户名和密码发布一条消息：

```javascript
mosquitto_pub -h localhost -t "test" -m "hello world" -u "sammy" -P "password"
```

消息应该按照步骤1执行。我们已经成功地为Mosquitto增加了密码保护。但是，我们在互联网上发送未加密的密码。**接下来，我们将通过向Mosquitto添加SSL加密来修复这个问题。**

> SSL加密着实有点让人头疼,鉴于我们目前不需要这么高的安全等级,采用学长的意见,跳过SSL加密,以后再补充(是我太菜,实在搞不好这玩意



其实一直到这一步可以说跟教程是完全一样的,但是我们是要跟外部通信啊!于是我从教程SSL加密部分精简出了打开端口实现外部连接的方法。

## 实现外部连接(无SSL加密)

打开我们以前的配置文件。

```javascript
sudo nano /etc/mosquitto/mosquitto.conf
```

在文件末尾粘贴以下内容，写下下面的内容：

```javascript
. . .
listener 1883 localhost
```

我们要增加一个单独的`listener`到配置。`listener 1883 localhost`，更新`1883`端口上的默认mqtt侦听器。`1883`是标准的未加密的MQTT端口。**`Mosquitto`只将该端口绑定到`localhost`接口，因此无法从外部访问它。外部请求无论如何都会被防火墙阻止。**

在重新启动`Mosquitto`以加载新配置之前，我们需要安装一个`mosquitto`服务文件。`systemd`这个文件用于确定如何运行`mosquitto`。在你最喜欢的编辑器里打开它。

```javascript
sudo nano /etc/systemd/system/multi-user.target.wants/mosquitto.service
```

找一行写着`User=mosquitto`然后删除它，然后保存并退出该文件。

我们需要重新加载systemd，因此它会注意到我们对服务文件所做的更改。

```javascript
sudo systemctl daemon-reload
```

现在我们可以重新启动Mosquitto来更新设置。

```javascript
sudo systemctl restart mosquitto
```

更新防火墙以允许连接到`8883`端口。

```javascript
sudo firewall-cmd --permanent --add-port=1883/tcp
```

重新加载防火墙。

```javascript
sudo firewall-cmd --reload
```

由于没有启用SSL加密,所以在发布和测试上的命令与步骤二一致,但其实到此时,服务器就已经可以使用了.

```javascript
sudo mosquitto
```

开始造做啦!!!

Ps:

[MQTTBox](http://workswithweb.com/mqttbox.html)　好用到发指的MQTT可视化调试工具！

![ＭＱＴＴＢｏｘ‘ｓ　ｉｍｇ](https://img-blog.csdnimg.cn/20200529202438315.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3p6ajA0MzdfQ1NETg==,size_16,color_FFFFFF,t_70)