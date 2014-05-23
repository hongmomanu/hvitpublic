--  --  --  --  --  --  --  --  --  --  --  系统用户角色功能表设计--  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  
--  用户表
CREATE TABLE IF NOT EXISTS users
(

  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,                   --  自增主键
  username VARCHAR(50),                                   --  用户名字
  password VARCHAR(500),                                   --  用户密码
  displayname VARCHAR(50),                                 --  显示名称
  divisionid  INT,                                    --  行政区划id
  time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  --  注册时间
  roleid  INT                                           --  角色id

);


--  行政区划表
CREATE  TABLE IF NOT EXISTS divisions
  (

  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,            --  自增主键
  parentid int,                                --  父节点
  divisionname VARCHAR(50),                         --  角色名称
  signaturepath VARCHAR(250),                         --  签章路径
  divisionpath varchar(50)                         --  行政区划路径


  );


--  角色表
CREATE TABLE IF NOT EXISTS roles
(

  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,   --  自增主键
  rolename VARCHAR(50)                   --  角色名称


);



--  功能表
CREATE TABLE IF NOT EXISTS functions
(

  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,   --  自增主键
  pid int ,                           --  父节点
  funcname VARCHAR(50),                   --  功能名称
  label  VARCHAR(50),                     --  功能标识
  imgcss  VARCHAR(250),                   --  图片标识
  sortnum int                        --  排序号


);

--  角色功能关联表
CREATE TABLE IF NOT EXISTS functorole
(

  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,   --  自增主键
  funcid int,                   --  功能id
  roleid int                    --  角色id

);

--  --  --  --  --  --  --  --  --  --  --  业务基础功能表设计--  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  
--  枚举类型表
CREATE  TABLE IF NOT EXISTS enumerate
(
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,            --  自增主键
  enumeratetype            VARCHAR(50),            --  枚举类型
  enumeratevalue           VARCHAR(50),            --  枚举值
  enumeratelabel           VARCHAR(50)             --  枚举标识

);

--  附件表
CREATE  TABLE IF NOT EXISTS attachment
  (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,            --  自增主键
  time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  --  提交时间
  businessid               int,                --  业务信息id
  attachmentname           VARCHAR(50),            --  附件名称
  attachmenttype           VARCHAR(50),            --  附件类型
  attachmentpath           VARCHAR(500)            --  附件路径

  );

