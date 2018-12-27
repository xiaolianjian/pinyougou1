app.controller("payController",function ($scope,$location,payService) {

    //生成二维码
    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            //应付金额
            $scope.money=(response.total_fee / 100).toFixed(2);
            //支付单号
            $scope.out_trade_no = response.out_trade_no;
            //二维码
            var qr = new QRious({
                element:document.getElementById('qrious'),
                size:250,
                level:'Q',
                value:response.code_url
            });

            //开始查询订单状态
            queryPayStatus(response.out_trade_no);

        })
    }

    //查询订单
    queryPayStatus=function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            //支付成功
            if(response.success){
                window.location.href = "paysuccess.html#?money="+$scope.money;
            }else{
                if(response.message == "支付超时"){
                    window.location.href = "paytimeout.html";
                }else {
                    window.location.href = "payfail.html";
                }
            }
        })
    }

    $scope.getMoney=function () {
        $scope.money = $location.search()["money"];
    }
})