不强制刷新获取Token
    curl https://api.weixin.qq.com/cgi-bin/stable_token \
    -H "Content-Type: application/json" \
    -d '{
    "grant_type": "client_credential",
    "appid": "xxxxx",
    "secret": "xxxxx"
    }'

application.yaml
spring.application.name=AbaloneRSS
server.port=3001

rss.uri=https://abalone.ink/rss.xml
wx.appname=Abalone Blog
wx.appid=xxx
wx.appsecret=xxx

