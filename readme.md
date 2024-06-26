本工程用于 动手实现一个rpc框架

v1.0: 
- [x] 构建一个简单的Provider

v2.0:
- [x] 实现简单的Consumer

v3.0
- [x] 方法重载【使用方法签名定义】
- [x] 参数类型转换
- [ ] 启动扫描优化，现有的注解扫描是全路径，需要优化成按指定路径扫描
- [x] 通过过滤Object内置方法，来实现服务端和客户端的无用方法过滤

v4.0
- [x] 实现RPC的负载均衡策略
- [x] 实现RPC的注册中心，使用zk作为rpc的注册中心

v5.0
- [x] 修复unregister与client.close()顺序问题
- [X] 把ProviderBootstrap中invoker方法分离出去，放到ProviderInvoker
- [X] 把ConsumerBootstrap在findAnnotationFields，放到MethodUtils中
- [X] 把SoInvocationHandler中OkhttpClient单独抽象处理，使其可扩展成不同http调用
- [X] RegistryCenter重构
- [X] register(service, instance) -> register(ProviderMeta, InstanceMeta)
- [X] start() -> client中对zk的配置从写死改成配置化
- [X] TreeCache改成最新的CuratorCache

v6.0
- [x] 实现RPC的Filter功能，主要包含
  - [x] CacheFilter,为请求增加缓存,如果请求参数在一段时间内一样，就返回缓存，减少调用次数，提升性能
  - [x] MockFilter, 可以mock服务端的返回
- [x] 使用sl4j替换sout

v7.0
- [x] 去除不必要的依赖，为jar包瘦身，让项目可以通过maven test跑通, 不依赖于springboot打包
- [x] mock zookeeper，使得provider和consumer可以做单元测试


v8.0
- [x] httpInvoker调用异常处理，参数配置化
- [x] 把所有RuntimeException定义成统一的自定义的RPCException
- [x] 新增consumer调用provider重试策略

v9.0
- [x] 实现consumer调用部分接口异常时，进行接口级别的故障隔离，并使用异步线程半开探活恢复

v10.0
- [x] 实现灰度路由

v11.0
- [X] 实现隐式传参

v12.0
- [x] Properties统一成ConfigProperties
- [x] 传输层抽象 SpringBootTransport
- [x] 实现将jar包发布到maven中央仓库
- [x] 实现父子pom的版本统一维护
- [x] 实现provider端的流量控制
- [x] 集成apollo配置中心
v13.0
- [ ] 将soregistry整合到sorpc
- [ ] 使用netty替换socket调用

feature
- [x] 自定义扫描包路径+`@SoRpcProvider`注解优化 => 已整合到v12.0
- [ ] 制作starter
- [ ] 使用group和version管理service

# todo
- 滑动时间窗口算法是如何运作的？
- 将v12.0中的流控抽象，可以适配滑动窗口和令牌桶算法？
- sorpc集成nacos配置中心
- sorpc集成nacos注册中心
- 一个服务即是生产者、也是消费者的情况下
