create database hurricane;
grant all privileges on hurricane.* to biguser@'%' identified by '123456';
flush privileges;