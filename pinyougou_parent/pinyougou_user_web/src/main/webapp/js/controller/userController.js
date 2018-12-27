 //控制层 
app.controller('userController' ,function($scope,userService){

    $scope.entity={phone:""};
    $scope.code = "";
	
	//用户注册
	$scope.reg=function () {
		if($scope.entity.password != $scope.password){
            alert("再次输入的密码不一致！");
            return;
		}
		if($scope.code == ""){
            alert("请先输入验证码！");
            return;
		}
		userService.add($scope.entity,$scope.code).success(function (response) {
			alert(response.message);
        })
    }

    //发送验证码
    $scope.sendCode=function () {
		if($scope.entity.phone == ""){
            alert("请先输入手机号！");
            return;
		}
		userService.sendCode($scope.entity.phone).success(function (response) {
            alert(response.message);
        })
    }
    
});	
