## 代码是从https://github.com/ghosx/tieba 拿过来改成java的加了消息推送
## 准备工作
    1.从百度获取自己的 BDUSS
    2.从 http://www.pushplus.plus 中获取一对一推送token
    3.打包源码或者下载我打包过的
## 流程
    登录腾讯云函数-》新建 -》 自定义创建 -》设置
## 设置(接流程中的设置)
    1.函数名称随意填写
    2.地域随意选择
    3.运行环境选择Java8
    4.函数代码选择 本地上传zip包
    5.执行方法设置 tb.AutoSign::mainHandler
    6.高级设置中设置 执行超时时间 设置到最大900秒
    7.高级设置中设置 环境变量 PUSH_PLUS_TOKEN 与 BDUSS 其中BDUSS支持多个需要在每个账号的BDUSS后边加上# 
      只有一个也最好加上，不加也行。你如果非要抬杠，那我劝你去工地。
    8.触发器设置选择自定义创建，触发方式选择定时触发，触发周期选择自定义触发周期，然后根据自
      己需要的时间设置。详情参考腾讯文档中的七域cron 
      https://cloud.tencent.com/document/product/583/9708#cron-.E8.A1.A8.E8.BE.BE.E5.BC.8F

## 升级或者重新部署
   打开创建的函数-》函数管理-》函数代码-》上传新的jar-》部署-》测试