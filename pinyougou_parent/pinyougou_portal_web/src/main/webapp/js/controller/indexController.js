app.controller("indexController",function ($scope,contentService) {

    $scope.contentList=[];//广告集合

    //查询所有广告
    $scope.findByCategoryId=function () {
        //查询大海报
        contentService.findByCategoryId(1).success(function (response) {
            //alert(JSON.stringify(response));
            $scope.contentList[1] = response;
        });
    }

    $scope.search=function () {
        if($scope.keywords == null ){
            alert("请先输入查询条件!");
            return;
        }
        window.location.href = "http://localhost:8084/search.html#?keywords=" + $scope.keywords;
    }
})