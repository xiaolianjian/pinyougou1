app.controller("cartController",function ($scope,cartService) {
    //查询购物车列表
    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;

            //统计总数量与总金额
            $scope.totalValue={totalNum:0, totalMoney:0.00 };//合计实体
            for(var i = 0; i < response.length; i++){
                var items = response[i].orderItemList;
                for(var j = 0; j < items.length; j++){
                    $scope.totalValue.totalNum += items[j].num;
                    $scope.totalValue.totalMoney += items[j].totalFee;
                }
            }
        })
    }

    //添加购物车
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(function (response) {
            if(response.success){
                //刷新页面
                $scope.findCartList();
            }else{
                alert(response.message);
            }
        })
    }

    //查询收件人列表
    $scope.findAddressList=function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList = response;
            $scope.address = response[0];
        })
    }

    //选择收件人
    $scope.selectAddress=function (address) {
        $scope.address = address;
    }

    //订单对象{paymentType:支付方式}
    $scope.order={paymentType:1};

    //设置支付方式
    $scope.selectPayType=function (type) {
        $scope.order.paymentType = type;
    }

    //保存订单
    $scope.submitOrder=function () {
        // 收件人
        $scope.order.receiver = $scope.address.contact;
        // 联系电话
        $scope.order.receiverMobile = $scope.address.mobile;
        // 收货地址
        $scope.order.receiverAreaName = $scope.address.address;

       cartService.submitOrder($scope.order).success(function (response) {
           alert(response.message);
           //下单成功，跳转支付页
           if(response.success){
               if($scope.order.paymentType == 1) {
                   window.location.href = "pay.html";
               }else{
                   window.location.href = "ordersuccess.html";
               }
           }
       })
    }

})