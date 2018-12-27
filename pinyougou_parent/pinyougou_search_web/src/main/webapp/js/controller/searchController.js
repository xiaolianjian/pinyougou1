app.controller("searchController",function ($scope,$location,searchService) {

    /**
     * 搜索对象
     * @type {{keywords: 关键字, category: 商品分类, brand: 品牌, spec: {'网络'：'移动4G','机身内存':'64G'},
     *          price:价格区间，pageNo:当前页,pageSize:每页查询的记录数,sort:排序方式(asc|desc),sortField:排序业务域}}
     */
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'',
                        'pageNo':1,'pageSize':40,'sort':'','sortField':''};

    //搜索商品
    $scope.search=function () {
        searchService.search($scope.searchMap).success(function (response) {
            //绑定搜索结果
            $scope.resultMap = response;

            //开始构建分页
            buildPageLabel();
        });
    }

    /**
     * 构建分页标签
     */
    buildPageLabel=function () {
        $scope.pageLable=[];  //分页标签项，记录了一共有多少页

        var firstPage=1;  //分页标签-起始编号
        var endPage=$scope.resultMap.totalPage;//分页标签-结束编号

        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点
        //总页数大于5
        if($scope.resultMap.totalPage > 5){
            //如果当前页码 <= 3，显示前5页
            if($scope.searchMap.pageNo <= 3){
                endPage = 5;
                $scope.firstDot=false;  //前面没点
            //如果当前页码在最后两页（总页数-2）， 显示后5页。 [总共100页,当前页99],显示后5页[96   97   98   99   100]
            }else if($scope.searchMap.pageNo >= ($scope.resultMap.totalPage - 2)){
                firstPage = $scope.resultMap.totalPage - 4;
                $scope.lastDot=false;//后面没点
            }else{
                //显示当前页为中心的5个页码
                firstPage = $scope.searchMap.pageNo - 2;
                endPage = $scope.searchMap.pageNo + 2;
            }
        }else{   //页数不足5页
            $scope.firstDot=false;//前面没点
            $scope.lastDot=false;//后边没点
        }
        //组装分页标签
        for(var i = firstPage; i <= endPage; i++){
            $scope.pageLable.push(i);
        }
    }

    /**
     * 添加搜索项
     * @param key 查询条件的属性名
     * @param value 查询条件的属性值
     */
    $scope.addSearchItem=function (key,value) {
        if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = value;
        }else{
            $scope.searchMap.spec[key] = value;
        }

        //提交查询
        $scope.search();
    }

    /**
     * 删除搜索项
     * @param key 删除的属性名
     */
    $scope.deleteSearchItem=function (key) {
        if(key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchMap[key] = '';
        }else{
            //$scope.searchMap.spec[key] = '';
            //删除属性
            delete $scope.searchMap.spec[key];
        }

        //提交查询
        $scope.search();
    }

    /**
     * 跳转到相应的页码
     * @param page 要跳转的页码
     */
    $scope.queryByPage=function (page) {
        if(page < 1 || page > $scope.resultMap.totalPage){
            alert("请输入正确的页码！");
            return;
        }
        $scope.searchMap.pageNo = page;
        //刷新数据
        $scope.search();
    }

    /**
     * 排序查询
     * @param sort 排序方式asc|desc
     * @param sortField 排序的业务域
     */
    $scope.sortSearch=function (sort,sortField) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        //刷新数据
        $scope.search();
    }

    /**
     * 识别关键字是否包含品牌内容
     * @return 查找的结果
     */
    $scope.keywordsIsBrand=function () {
        for(var i = 0; i < $scope.resultMap.brandIds.length; i++){
            if($scope.searchMap.keywords == $scope.resultMap.brandIds[i].text){
                return true;
            }
        }
        return false;
    }

    /**
     * 接收其它页面传递的关键字参数
     */
    $scope.loadkeywords=function () {
        var keywords = $location.search()["keywords"];
        $scope.searchMap.keywords = keywords;
        $scope.search();
    }

})