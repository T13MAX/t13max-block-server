# Block服务端

### 模块
| 模块                | 名称       | 描述                                      |  
|:------------------|:---------|:----------------------------------------|
| [common-game]     | 一些基础东东   |                                         |
| [common-data]     | 持久化      |                                         |
| [common-proto]    | pb       |                                         |
| [common-template] | 读表模块     |                                         |
| [service-game]    | 游戏主模块    |                                         |
| [service-center]  | block中心服 | 处理登录 用户信息 等 (后续完善)                      |
| [service-client]  | 简易客服端    | 用来临时查看服务器的运行情况, 方便调试, 理论上客户端后续使用Unity开发 |
| [service-bot]     | 机器人      | 机器人 压测用                                 |

### 详情
使用Unity实现客户端, Java实现服务端 可本地可联机 实现类我的世界玩法, 后续看情况加入创新 借鉴迷你世界 饥荒 泰拉瑞亚等 

### 计划
#### 基本架构
#### 方块
几种基础的 实现搭建挖掘 群系 地形 
#### 实体
寻路 行为树 AI
#### 网络
前后端通讯 帧同步?

#### 武器装备工具 效果(附魔) 属性
#### 其他
命令 控制台 规则 插件支持 流体(水, 岩浆) 类红石系统 合成表 规则
时间 天气 (亮度)

#### 设定
- 64个玩家 

- 每个玩家加载49个区块 其中25个活跃状态 也就是90x90的面积 外面一圈预加载
- 高度是265 一共207300个格 假设一个区块耗费8(id)+方块个数x8==1619k==1.6M 算上实体 不超过2M (极端情况)

- 每个玩家有16只怪 一共1024只怪物实体 (方块实体不好估计) 一个世界单线程 tick 1024个寻路 
- 可能还有1024的其他动物? 方块实体若干 应该能顶得住吧...
- 多世界 限制每个世界的大小 暂定边长1024个区块(16384) 理论上活跃区域不会超过这么大 每个世界一张区块表 单表数据1048576条