主页网址
http://localhost:8080/SmartWeb/main/showIndex.do?two=0&three=0

1.excel本地文件路径：  SmartWeb\src\main\resources\set.properties

1.账号密码配置在 resource下面的userInfo.json中


如果要取消验证码
注释掉cn.huahai.tel.service.UserServer中的
	if(!verify(request,received)) {
		throw new VerifyNotPass("验证码不正确");
	}



2018-06-13修改: lizhuodong
	1.0  基础功能完成（15分钟级数据展示和相关excel数据下载，vip重点小区设置，小区组设置，cell表导入）
	
   
   
	