服务安装说明

1. 解压hurricane-server-0.0.1-SNAPSHOT-bin.tar.gz

tar xzvf hurricane-server-0.0.1-SNAPSHOT-bin.tar.gz

2. 进入hmw目录，为bin目录下脚本添加执行执行权限

cd hmw
chmod +x bin/*.sh

3. 启动中间件服务

bin/startup.bat

4. 检查logs目录下hurricane.out文件，查看服务是否正常启动

正常启动后，文件内容为：
 INFO | Use configuration : /usr/local/hurricane/hmw/conf/hurricane-site.xml
 INFO | Use configuration : /usr/local/hurricane/hmw/conf/hurricane-site.xml
 INFO | jetty-7.6.15.v20140411
 INFO | Started SelectChannelConnector@0.0.0.0:12345
Manage Server start!

5. 在本地启动客户端测试程序

bin\demo.bat [insert|query1|query2|getbyid {messageid}]

可首先执行insert命令

C:\workspace\hmw\dist\hmw>bin\demo insert
-------------------------------------
Insert 10 records.
edbf4965-75aa-430b-89a0-f57452b102e5
bcabdbf8-f6f6-4b14-96b5-e0653a142965
f6d92eaf-7b7e-49fb-af36-d7a6f2e6c172
b119a780-9302-490d-9e51-a24afe395fe2
57708ac6-804c-4670-83b3-ccdfe586c0d3
0fc39d8f-774d-4640-b3a3-92f3dca11218
f67a99c7-587a-4996-9621-e27b0704cfe4
b47ec4a7-5c4c-4e24-b613-cd973f011c59
3d32aa79-a480-4443-bca9-78903f48869e
997097dd-df80-4ae1-af23-8c9d25a87a9b

然后再执行查询命令，检查插入是否正常

C:\workspace\hmw\dist\hmw>bin\demo getbyid 0fc39d8f-774d-4640-b3a3-92f3dca11218
Got 1 rows.
--------------
fromaddr       :        [58同城简历 <mailservice@58.com>]
attachmentdata :        [0:186, 186:47]
subject        :        会计/会计师-（现职位）会计/会计师-58.com
messageid      :        0fc39d8f-774d-4640-b3a3-92f3dca11218
signature      :        null
toaddr         :        [wwang369@163.com, abc@abc.com, test@test.com]
BLOB           :        233 bytes