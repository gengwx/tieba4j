# 重要通知！！！！！！！！
## 5月腾讯云函数要改版了，没有免费额度了，这个链接https://curl.qcloud.com/Q9DM1Qcs 1块钱可以买一年，最多买两次，下个月改版后好像也没了，链接也不是我的推广，如果还想白嫖的话，可以去看看其他厂商的，百度、华为、微软的好像是终身免费的，可以自己去研究研究，源代码写的很烂，自己拿去改改吧。也可以等我研究研究别家的(不要抱太大希望)




# 贴吧签到腾讯云函数专用版
## 代码是从原https://github.com/ghosx/ 拿过来改成java的加了消息推送
## 准备工作
    1.从百度获取自己的 BDUSS
    2.打包源码或者下载releases中我打包好的
![image](https://user-images.githubusercontent.com/10470892/122930469-9a2f0b80-d39e-11eb-8bd1-ac6be2b7e8eb.png)<br>
![image](https://user-images.githubusercontent.com/10470892/122930534-a7e49100-d39e-11eb-8a38-35daa2a684ea.png)

## 流程
    登录腾讯云函数-》新建 -》 自定义创建 -》设置
## 设置(接流程中的设置)
    1.函数名称随意填写
    2.地域随意选择
    3.运行环境选择Java8
    4.函数代码选择 本地上传zip包(上传的是打包的.jar文件)
    5.执行方法设置 tb.AutoSign::mainHandler
    6.高级设置中设置 执行超时时间 设置到最大900秒
    7.账号设置：高级设置中设置 环境变量BDUSS 其中BDUSS支持多个需要在每个账号的BDUSS后边加上# 
      只有一个也最好加上，不加也行。
    8.推送设置：高级设置中设置 环境变量
        1.推送加+  从 http://www.pushplus.plus 中获取一对一推送token
            环境变量中设置 PUSH_PLUS_TOKEN
        2.TG（TG推送必须要用国外服务器：香港） 环境变量中设置 TG_TOKEN与TG_CHAT_ID(注意：推送功能来自https://github.com/JunzhouLiu/BILIBILI-HELPER#telegram-订阅执行结果
            获取方式：在 Telegram 中添加 BotFather 这个账号，然后依次发送/start /newbot 按照提示即可创建一个新的机器人。记下来给你生成的 token。
            搜索刚刚创建的机器人的名字，并给它发送一条消息。
            特别注意：需要先与机器人之间创建会话，机器人才能下发消息，否则机器人无法主动发送消息，切记！
            在 Telegram 中搜索 userinfobot，并给它发送一条消息，它会返回给你 chatid。)
        3.签到失败发信  环境变量中设置 ERROR_SEND 并且为Y时会进行消息筛选，只有签到失败才会进行推送，签到成功不会。此功能只进行了简单测试，
            且只在V1.1.1及以上版本有，请根据情况选择。
    9.触发器设置选择自定义创建，触发方式选择定时触发，触发周期选择自定义触发周期，然后根据自己需要的时间设置。详情参考腾讯文档中的七域cron。
      https://cloud.tencent.com/document/product/583/9708#cron-.E8.A1.A8.E8.BE.BE.E5.BC.8F
      以下为参考示例：每天6点执行一次。
![image](https://user-images.githubusercontent.com/10470892/122933417-39550280-d3a1-11eb-9edd-8a89eb3317ef.png)
## 升级或者重新部署
   打开创建的函数-》函数管理-》函数代码-》上传新的jar-》部署-》测试
