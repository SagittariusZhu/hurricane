export SOLR_LIBS=/usr/local/hurricane/apache-tomcat-6.0.41/webapps/solr/WEB-INF
java -cp $SOLR_LIBS:$SOLR_LIBS/classes/*:$SOLR_LIBS/lib/* org.apache.solr.cloud.ZkCLI -cmd clear $1 -zkhost sr1:2181,sr2:2181,sr3:2181 
