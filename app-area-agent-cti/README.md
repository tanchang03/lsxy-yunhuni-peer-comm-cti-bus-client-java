# yunhuni.cti.busnetcli

`com.lsxy.app.area.cti.busnetcli` 是云呼你项目的区域落地部分所采用的 CTI 服务器(IPSC)专用的消息总线客户端的 Java 包装。

它用于向 Java 开发者提供 IPSC 数据总线的客户端功能。

## 特点

- 采用 JNI 技术直接封装 CTI Bus 的C语言实现客户端
- 完整的SmartBus客户端功能包装。其功能基本上与C语言实现客户端一对一。

## 基础
见： http://git.liushuixingyun.com/projects/YHN/repos/yunhuni-peer-comm-cti-bus-client-c/

## 作用
开发人员可以使用这个库，快速的构建符合 http://git.liushuixingyun.com/projects/YHN/repos/yunhuni-peer-comm-cti-flow/ 中 API 文档规定的区域呼叫控制程序。

## 迁移
该项目最终将合并到 http://git.liushuixingyun.com/projects/YHN/repos/yunhuni-peer-internet/

目前的 git repo 仅在尚未合并项目时做单独的开发调测用。
