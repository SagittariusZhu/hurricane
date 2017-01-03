call "%HURRICANE_HOME%\bin\cpappend.bat" "%HURRICANE_HOME%\schema"
call "%HURRICANE_HOME%\bin\cpappend.bat" "%HURRICANE_HOME%\conf"
for %%i in ("%HURRICANE_HOME%\lib\adapter\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
rem for %%i in ("%HURRICANE_HOME%\lib\hdfs\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
rem for %%i in ("%HURRICANE_HOME%\lib\solrj\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
rem for %%i in ("%HURRICANE_HOME%\lib\neo4j\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
rem for %%i in ("%HURRICANE_HOME%\lib\sql\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
rem for %%i in ("%HURRICANE_HOME%\lib\jetty7\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
rem for %%i in ("%HURRICANE_HOME%\lib\hurricane\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
rem for %%i in ("%HURRICANE_HOME%\lib\3rd\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
rem for %%i in ("%HURRICANE_HOME%\lib\*.jar") do call "%HURRICANE_HOME%\bin\cpappend.bat" %%i
