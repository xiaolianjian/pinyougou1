app.controller("itemController",function ($scope,$http) {
    /**
     * 修改购物车数量
     * @param x
     */
    $scope.addNum=function (x) {
        $scope.num = x;
        if($scope.num < 1){
            $scope.num = 1;
        }
    }

    //记录用户选择的规格 网络：4G
    $scope.specificationItems={};
    /**
     * 用户勾选规格点击事件
     * @param specName 规格名称
     * @param optionName 选项名称
     */
    $scope.selectSpecification=function (specName,optionName) {
        $scope.specificationItems[specName] = optionName;

        //更新sku
        searchSku();
    }

    /**
     * 识别要不要勾选上规格
     * @param specName
     * @param optionName
     */
    $scope.isSelected=function (specName,optionName) {
        return $scope.specificationItems[specName] == optionName;
    }

    /**
     * 加载默认sku信息
     */
    $scope.loadSku=function () {
        //记录默认sku
        $scope.sku = skuList[0];
        //勾选规格，这里一定要用深克隆，因为规格一直在改，但是我们基本数据skuList是不能改的
        $scope.specificationItems=JSON.parse(JSON.stringify(skuList[0].spec));
    }

    /**
     * 匹配两个对象
     * @param map1 选中的规格
     * @param map2 当前遍历中的sku信息
     * @return {boolean}
     */
    matchObject=function(map1,map2){
        for(var k in map1){
            if(map1[k]!=map2[k]){
                return false;
            }
        }
        for(var k in map2){
            if(map2[k]!=map1[k]){
                return false;
            }
        }
        return true;
    }

    /**
     * 配置sku是否与用户勾选的规格一致
     */
    searchSku=function(){
        for(var i=0;i<skuList.length;i++ ){
            if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
                $scope.sku=skuList[i];
                return ;
            }
        }
        $scope.sku={id:0,title:'--------',price:0};//如果没有匹配的
    }

    //添加商品到购物车
    $scope.addToCart=function(){
        //alert('skuid:'+$scope.sku.id);

        $http.get("http://localhost:8088/cart/addGoodsToCartList.do?itemId="+$scope.sku.id+"&num="+$scope.num,{'withCredentials':true})
            .success(function (response) {
                alert(response.message);
                if(response.success){
                    window.location.href = "http://localhost:8088/cart.html";
                }
        });
    }

});