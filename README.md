XLogging
=====

XLogging is an inspector for Android OkHttp clients. 

## Example
Level.HEADERS

```
    --> GET http://image.baidu.com/channel/listjson?pn=0&rn=3&tag1=%E7%BE%8E%E5%A5%B3&tag2=%E5%85%A8%E9%83%A8&ftags=%E6%A0%A1%E8%8A%B1&ie=utf8 http/1.1
    Host: image.baidu.com
    User-Agent: okhttp/3.6.0
    Connection: Keep-Alive
    Accept-Encoding: gzip
    --> END GET
    
    <-- 200 OK (63ms, 5789-byte body)
    Vary: Accept-Encoding
    Set-Cookie: BAIDUID=F388A9E0BF460F3D334FC962498CFD82:FG=1; expires=Fri, 15-Jun-18 05:41:15 GMT; max-age=31536000; path=/; domain=.baidu.com; version=1
    X-Bd-Id: 18117792609767620757
    Access-Control-Allow-Credentials: true
    Connection: keep-alive
    Content-Type: text/html
    P3p: CP=" OTI DSP COR IVA OUR IND COM "
    X-Bd-Ul: bef17eea90d82f4457acb3bb4d6b0991
    Date: Thu, 15 Jun 2017 05:41:15 GMT
    Tracecode: 24759133060915721482061513
    X-Bd-Oc: 0
    Server: Apache
    Transfer-Encoding: chunked
    Content-Encoding: gzip
    Access-Control-Allow-Origin: https://m.baidu.com,https://www.baidu.com,http://m.baidu.com,http://www.baidu.com
    --> END HTTP
```

License
-------

    Copyright (C) 2017 hello2mao.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
