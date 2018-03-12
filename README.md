XLogging
=========================
![](https://img.shields.io/badge/build-parsing-brightgreen.svg)
![](https://img.shields.io/badge/release-v2.0.3-blue.svg)
![](https://img.shields.io/badge/license-Apache%202-red.svg)

XLogging is an enhanced network performance monitor SDK for Android.

Quick Start
-------------
**Gradle:**
```
compile 'com.hello2mao:xlogging:2.0.3'
```
**Usage:**
``` java
XLogging.install();

```
**Or you can add a callback to get specific data:**
``` java
XLogging.install(new XLoggingCallback() {
            @Override
            public void handle(TransactionData transactionData) {
                System.out.println(transactionData.getBytesSent());
            }
        });
```

Example log
-------------

```
I/XLogging: <<<<<<<<XLogging Begin<<<<<<<<
            host:             image.baidu.com
            ip:               112.80.248.122
            scheme:           https
            protocol:         HTTP/1.1
            port:             443
            pathAndQuery:     /channel/listjson?pn=0&rn=30&tag1=%E7%BE%8E%E5%A5%B3&tag2=%E5%85%A8%E9%83%A8&ftags=%E6%A0%A1%E8%8A%B1&ie=utf8
            requestMethod:    GET
            statusCode:       200
            bytesSent:        277 bytes
            bytesReceived:    10285 bytes
            tcpConnectTime:   6 ms
            sslHandshakeTime: 21 ms
            requestTime:      1 ms
            firstPackageTime: 55 ms
            responseTime:     68 ms
            socketReuse:      false
            =========XLogging End==========
```

License
-------------

    Copyright (C) 2018 hello2mao.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
