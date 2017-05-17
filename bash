Script started on Qua 17 Mai 2017 18:22:05 WEST
]0;aid@virtualbox: ~/proj-cnv/RenderFarm[01;32maid@virtualbox[00m:[01;34m~/proj-cnv/RenderFarm[00m$ bash script.sh 4
[SCRIPT]STARTING AWESOME SCRIPT...
[SCRIPT]CLEANING PROJECT...
[SCRIPT]REMOVING ALL GENERATED FILES...
[SCRIPT]COMPILING CODE...
Picked up _JAVA_OPTIONS: -XX:-UseSplitVerifier 
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
[SCRIPT]COMPILE SUCCESS!
[SCRIPT]COPYING RESOURCES SUCCESS!
Picked up _JAVA_OPTIONS: -XX:-UseSplitVerifier 
[LOADBALANCER MAIN]Set up load balancer...
Table Description: {AttributeDefinitions: [{AttributeName: file_name,AttributeType: S}, {AttributeName: metrics_hash,AttributeType: N}],TableName: metrics-table,KeySchema: [{AttributeName: file_name,KeyType: HASH}, {AttributeName: metrics_hash,KeyType: RANGE}],TableStatus: ACTIVE,CreationDateTime: Wed May 17 18:23:45 WEST 2017,ProvisionedThroughput: {NumberOfDecreasesToday: 0,ReadCapacityUnits: 1,WriteCapacityUnits: 1},TableSizeBytes: 0,ItemCount: 0,TableArn: arn:aws:dynamodb:us-west-2:728675182323:table/metrics-table,}
Initializing load balancing algorithm
Initializing load balancer request handler
[LOADBALANCER MAIN]Loadbalancer, listening on Port: 8000
[AUTOSCALER]Auto scaler launched. 2 instances launched
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[Handler]Looking for best instance...
[EstimateRequestCost]Result: 1 [Default] no entries
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.201.128.216:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=50&coff=300&roff=300
[Handler]Instance "probably" died, going to redirect to other instance
java.net.ConnectException: Connection refused
	at java.net.PlainSocketImpl.socketConnect(Native Method)
	at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:339)
	at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:200)
	at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:182)
	at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
	at java.net.Socket.connect(Socket.java:579)
	at sun.net.NetworkClient.doConnect(NetworkClient.java:175)
	at sun.net.www.http.HttpClient.openServer(HttpClient.java:432)
	at sun.net.www.http.HttpClient.openServer(HttpClient.java:527)
	at sun.net.www.http.HttpClient.<init>(HttpClient.java:211)
	at sun.net.www.http.HttpClient.New(HttpClient.java:308)
	at sun.net.www.http.HttpClient.New(HttpClient.java:326)
	at sun.net.www.protocol.http.HttpURLConnection.getNewHttpClient(HttpURLConnection.java:997)
	at sun.net.www.protocol.http.HttpURLConnection.plainConnect(HttpURLConnection.java:933)
	at sun.net.www.protocol.http.HttpURLConnection.connect(HttpURLConnection.java:851)
	at renderfarm.loadbalancer.handlers.RequestHandler.redirectRequestToRenderFarmInstance(RequestHandler.java:177)
	at renderfarm.loadbalancer.handlers.RequestHandler.handle(RequestHandler.java:99)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:77)
	at sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:83)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:80)
	at sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:675)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:77)
	at sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:647)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)
java.io.IOException: response headers not sent yet
	at sun.net.httpserver.PlaceholderOutputStream.checkWrap(ExchangeImpl.java:428)
	at sun.net.httpserver.PlaceholderOutputStream.close(ExchangeImpl.java:453)
	at renderfarm.loadbalancer.handlers.RequestHandler.redirectRequestToRenderFarmInstance(RequestHandler.java:214)
	at renderfarm.loadbalancer.handlers.RequestHandler.handle(RequestHandler.java:99)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:77)
	at sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:83)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:80)
	at sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:675)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:77)
	at sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:647)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)
[Handler]Looking for best instance...
[EstimateRequestCost]Result: 1 [Default] no entries
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.201.128.216:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=50&coff=300&roff=300
[Handler]Instance "probably" died, going to redirect to other instance
java.net.ConnectException: Connection refused
	at java.net.PlainSocketImpl.socketConnect(Native Method)
	at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:339)
	at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:200)
	at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:182)
	at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
	at java.net.Socket.connect(Socket.java:579)
	at sun.net.NetworkClient.doConnect(NetworkClient.java:175)
	at sun.net.www.http.HttpClient.openServer(HttpClient.java:432)
	at sun.net.www.http.HttpClient.openServer(HttpClient.java:527)
	at sun.net.www.http.HttpClient.<init>(HttpClient.java:211)
	at sun.net.www.http.HttpClient.New(HttpClient.java:308)
	at sun.net.www.http.HttpClient.New(HttpClient.java:326)
	at sun.net.www.protocol.http.HttpURLConnection.getNewHttpClient(HttpURLConnection.java:997)
	at sun.net.www.protocol.http.HttpURLConnection.plainConnect(HttpURLConnection.java:933)
	at sun.net.www.protocol.http.HttpURLConnection.connect(HttpURLConnection.java:851)
	at renderfarm.loadbalancer.handlers.RequestHandler.redirectRequestToRenderFarmInstance(RequestHandler.java:177)
	at renderfarm.loadbalancer.handlers.RequestHandler.handle(RequestHandler.java:99)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:77)
	at sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:83)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:80)
	at sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:675)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:77)
	at sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:647)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)
java.io.IOException: response headers not sent yet
	at sun.net.httpserver.PlaceholderOutputStream.checkWrap(ExchangeImpl.java:428)
	at sun.net.httpserver.PlaceholderOutputStream.close(ExchangeImpl.java:453)
	at renderfarm.loadbalancer.handlers.RequestHandler.redirectRequestToRenderFarmInstance(RequestHandler.java:214)
	at renderfarm.loadbalancer.handlers.RequestHandler.handle(RequestHandler.java:99)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:77)
	at sun.net.httpserver.AuthFilter.doFilter(AuthFilter.java:83)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:80)
	at sun.net.httpserver.ServerImpl$Exchange$LinkHandler.handle(ServerImpl.java:675)
	at com.sun.net.httpserver.Filter$Chain.doFilter(Filter.java:77)
	at sun.net.httpserver.ServerImpl$Exchange.run(ServerImpl.java:647)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
	at java.lang.Thread.run(Thread.java:745)
[Handler]Looking for best instance...
[EstimateRequestCost]Result: 1 [Default] no entries
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.201.128.216:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=50&coff=300&roff=300
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[Handler]Looking for best instance...
[EstimateRequestCost]Result: 1 [Default] no entries
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.201.128.216:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=50&coff=300&roff=300
[Handler]Getting input stream...
[AUTOSCALER]Auto scaler algorithm started...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[AUTOSCALER]Auto scaler algorithm ended.
[Handler]Looking for best instance...
[EstimateRequestCost]Result: 1 [Default] no entries
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.201.128.216:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=50&coff=300&roff=300
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
^C]0;aid@virtualbox: ~/proj-cnv/RenderFarm[01;32maid@virtualbox[00m:[01;34m~/proj-cnv/RenderFarm[00m$ bash script.sh 4
[SCRIPT]STARTING AWESOME SCRIPT...
[SCRIPT]CLEANING PROJECT...
[SCRIPT]REMOVING ALL GENERATED FILES...
[SCRIPT]COMPILING CODE...
Picked up _JAVA_OPTIONS: -XX:-UseSplitVerifier 
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
[SCRIPT]COMPILE SUCCESS!
[SCRIPT]COPYING RESOURCES SUCCESS!
Picked up _JAVA_OPTIONS: -XX:-UseSplitVerifier 
[LOADBALANCER MAIN]Set up load balancer...
Table Description: {AttributeDefinitions: [{AttributeName: file_name,AttributeType: S}, {AttributeName: metrics_hash,AttributeType: N}],TableName: metrics-table,KeySchema: [{AttributeName: file_name,KeyType: HASH}, {AttributeName: metrics_hash,KeyType: RANGE}],TableStatus: ACTIVE,CreationDateTime: Wed May 17 18:39:14 WEST 2017,ProvisionedThroughput: {NumberOfDecreasesToday: 0,ReadCapacityUnits: 1,WriteCapacityUnits: 1},TableSizeBytes: 0,ItemCount: 0,TableArn: arn:aws:dynamodb:us-west-2:728675182323:table/metrics-table,}
Initializing load balancing algorithm
Initializing load balancer request handler
[LOADBALANCER MAIN]Loadbalancer, listening on Port: 8000
[AUTOSCALER]Auto scaler launched. 2 instances launched
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[Handler]Looking for best instance...
[EstimateRequestCost]Result: 1 [Default] no entries
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.245.137.32:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=50&coff=300&roff=300
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[Handler]Looking for best instance...
[EstimateRequestCost]Searching in 1 metrics
Fitest Metric: ===============================================================
File Name: test01.txt
Normalized Window: X: 0.3 Y: 0.3 Width: 0.05 Height: 0.05
Total Pixels Rendered: 1000000
Measures: 
Basic Block Count: 1924269
Store Count: 627461
Load Count: 5285449

===============================================================

[GetCostLevel]Complexity level: 0
[EstimateRequestCost]FitestMetricOvelappingArea: 0.002500001
[EstimateRequestCost]FitestMetricArea: 0.0025000002
[EstimateRequestCost]ScaleFactor: 1.0
[EstimateRequestCost]%OfOverlapingAreaInMetric: 1.0000004
[EstimateRequestCost]%OfOverlapingAreaInRequest: 1.0000004
[EstimateRequestCost]Result: -3.72529E-7 => 0
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.245.137.32:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=50&coff=300&roff=300
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[Handler]Looking for best instance...
[EstimateRequestCost]Searching in 1 metrics
Fitest Metric: ===============================================================
File Name: test01.txt
Normalized Window: X: 0.3 Y: 0.3 Width: 0.05 Height: 0.05
Total Pixels Rendered: 1000000
Measures: 
Basic Block Count: 1924267
Store Count: 627461
Load Count: 5285449

===============================================================

[GetCostLevel]Complexity level: 0
[EstimateRequestCost]FitestMetricOvelappingArea: 0.002500001
[EstimateRequestCost]FitestMetricArea: 0.0025000002
[EstimateRequestCost]ScaleFactor: 1.0
[EstimateRequestCost]%OfOverlapingAreaInMetric: 1.0000004
[EstimateRequestCost]%OfOverlapingAreaInRequest: 0.5000002
[EstimateRequestCost]Result: 0.49999982 => 0
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.245.137.32:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=100&coff=300&roff=300
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[Handler]Looking for best instance...
[FAULT DETECTOR]Fault detector ended
[EstimateRequestCost]Searching in 2 metrics
Fitest Metric: ===============================================================
File Name: test01.txt
Normalized Window: X: 0.3 Y: 0.3 Width: 0.05 Height: 0.1
Total Pixels Rendered: 1000000
Measures: 
Basic Block Count: 3867087
Store Count: 1260011
Load Count: 10610729

===============================================================

[GetCostLevel]Complexity level: 0
[EstimateRequestCost]FitestMetricOvelappingArea: 0.005000001
[EstimateRequestCost]FitestMetricArea: 0.0050000004
[EstimateRequestCost]ScaleFactor: 1.0
[EstimateRequestCost]%OfOverlapingAreaInMetric: 1.0000001
[EstimateRequestCost]%OfOverlapingAreaInRequest: 1.0000001
[EstimateRequestCost]Result: -9.313225E-8 => 0
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.245.137.32:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=50&wr=100&coff=300&roff=300
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[Handler]Looking for best instance...
[EstimateRequestCost]Searching in 2 metrics
Fitest Metric: ===============================================================
File Name: test01.txt
Normalized Window: X: 0.3 Y: 0.3 Width: 0.05 Height: 0.05
Total Pixels Rendered: 1000000
Measures: 
Basic Block Count: 1924267
Store Count: 627461
Load Count: 5285449

===============================================================

[GetCostLevel]Complexity level: 0
[EstimateRequestCost]FitestMetricOvelappingArea: 6.250003E-4
[EstimateRequestCost]FitestMetricArea: 0.0025000002
[EstimateRequestCost]ScaleFactor: 1.0
[EstimateRequestCost]%OfOverlapingAreaInMetric: 0.2500001
[EstimateRequestCost]%OfOverlapingAreaInRequest: 1.0000004
[EstimateRequestCost]Result: -3.72529E-7 => 0
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.245.137.32:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=25&wr=25&coff=300&roff=300
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[Handler]Looking for best instance...
[EstimateRequestCost]Searching in 3 metrics
Fitest Metric: ===============================================================
File Name: test01.txt
Normalized Window: X: 0.3 Y: 0.3 Width: 0.025 Height: 0.025
Total Pixels Rendered: 1000000
Measures: 
Basic Block Count: 486364
Store Count: 158213
Load Count: 1332057

===============================================================

[GetCostLevel]Complexity level: 0
[EstimateRequestCost]FitestMetricOvelappingArea: 2.4999952E-5
[EstimateRequestCost]FitestMetricArea: 6.2500004E-4
[EstimateRequestCost]ScaleFactor: 1.0
[EstimateRequestCost]%OfOverlapingAreaInMetric: 0.03999992
[EstimateRequestCost]%OfOverlapingAreaInRequest: 0.9999981
[EstimateRequestCost]Result: 1.891749E-6 => 0
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.245.137.32:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=5&wr=5&coff=300&roff=300
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[Handler]Looking for best instance...
[EstimateRequestCost]Result: 1 [Default] no entries
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.245.137.32:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=5&wr=5&coff=0&roff=0
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[Handler]Looking for best instance...
[EstimateRequestCost]Searching in 1 metrics
Fitest Metric: ===============================================================
File Name: test01.txt
Normalized Window: X: 0.0 Y: 0.0 Width: 0.005 Height: 0.005
Total Pixels Rendered: 1000000
Measures: 
Basic Block Count: 7561
Store Count: 3043
Load Count: 27420

===============================================================

[GetCostLevel]Complexity level: 0
[EstimateRequestCost]FitestMetricOvelappingArea: 2.5E-5
[EstimateRequestCost]FitestMetricArea: 2.5E-5
[EstimateRequestCost]ScaleFactor: 1.0
[EstimateRequestCost]%OfOverlapingAreaInMetric: 1.0
[EstimateRequestCost]%OfOverlapingAreaInRequest: 0.25
[EstimateRequestCost]Result: 0.75 => 1
[Load Balancing]Load balancer algorithm started!
[Load Balancing]Waiting for lock!
[Load Balancing]Load balancer algorithm ended!
[Handler]Best instance found!!!
[Handler]Redirecting request to instance URL: http://54.245.137.32:8000/r.html?f=test01.txt&sc=1000&sr=1000&wc=10&wr=10&coff=0&roff=0
[Handler]Getting input stream...
[Handler]Waiting for instance reply with image...
[Handler]SUCCESS answer to request!
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
[FAULT DETECTOR]Fault detector started...
[FAULT DETECTOR]Fault detector ended
[AUTOSCALER]Auto scaler algorithm started...
[AUTOSCALER]Auto scaler algorithm ended.
^C]0;aid@virtualbox: ~/proj-cnv/RenderFarm[01;32maid@virtualbox[00m:[01;34m~/proj-cnv/RenderFarm[00m$ git status
On branch master
Your branch is up-to-date with 'origin/master'.
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

	[31mmodified:   src/main/java/renderfarm/dynamo/AmazonDynamoDB.java[m

Untracked files:
  (use "git add <file>..." to include in what will be committed)

	[31mbash[m

no changes added to commit (use "git add" and/or "git commit -a")
]0;aid@virtualbox: ~/proj-cnv/RenderFarm[01;32maid@virtua