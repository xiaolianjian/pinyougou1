 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,
										   itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id = $location.search()["id"];
		if(id != null) {
            goodsService.findOne(id).success(
                function (response) {
                    $scope.entity = response;
                    //设置商品描述
                    editor.html($scope.entity.goodsDesc.introduction)
                    //把商品图片字符串转换为json对象
                    $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                    //把商品扩展属性字符串转换为json对象
                    $scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                    //把商品规格字符串转换为json对象
					$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

					//把商品sku中的规格信息字符串转换为json对象
                    for(var i = 0; i < $scope.entity.itemList.length; i++){
                        $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
					}
                }
            );
        }
	}

    /**
	 * 验证规格checkbox是否在选中
     * @param specName 规格名称
     * @param optionName 选项名称
	 * @return 结果true|false
     */
	$scope.checkAttributeValue=function (specName,optionName) {
        var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", specName);
        if(obj != null){
        	//如果找到相应规格选项
			if(obj.attributeValue.indexOf(optionName) >= 0){
                return true;
			}
		}
        return false;
    }
	
	//保存 
	$scope.save=function(){
		//先取出富文本编辑器的内容，绑定到desc
        $scope.entity.goodsDesc.introduction = editor.html();

		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
                alert(response.message);
				if(response.success){
					//清空商品实体
					//$scope.entity={};
					//清空富文本
                    //editor.html("");
                    window.location.href = "goods.html";
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    $scope.image_entity={url:""};
    //上传图片
    $scope.uploadFile=function () {
        uploadService.uploadFile().success(function (response) {
            //如果上传成功,绑定url到表单
            if(response.success){
                $scope.image_entity.url=response.message;
            }else{
                alert(response.message);
            }
        }).error(function() {
            alert("上传发生错误");
        });
    }
	//定义页面实体结构
	//{goods:{商品基本信息},goodsDesc:{itemImages:[图片列表],specificationItems:[规格列表]}};
    $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};

    //添加图片列表
    $scope.add_image_entity=function(){
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //列表中移除图片
    $scope.remove_image_entity=function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //查询商品一级类目
    $scope.findItemCat1List=function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCat1List = response;
        })
    }

    //加载商品二级类目
	//$watch(监听的变量，function(新的值，原来的值))
	$scope.$watch("entity.goods.category1Id",function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat2List = response;
        })
    })

    //加载商品三级类目
    //$watch(监听的变量，function(新的值，原来的值))
    $scope.$watch("entity.goods.category2Id",function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List = response;
        })
    })

    //加载模板id
    //$watch(监听的变量，function(新的值，原来的值))
    $scope.$watch("entity.goods.category3Id",function (newValue,oldValue) {
		itemCatService.findOne(newValue).success(function (response) {
			$scope.entity.goods.typeTemplateId=response.typeId;
        })
    })

    //加载品牌列表、扩展属性、规格与选项列表列表
    //$watch(监听的变量，function(新的值，原来的值))
    $scope.$watch("entity.goods.typeTemplateId",function (newValue,oldValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
            $scope.typeTemplate = response;
        	//读取品牌列表
            $scope.typeTemplate.brandIds = JSON.parse(response.brandIds);
            //读取扩展属性
            var id = $location.search()["id"];
            if(id == null) {
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
            }
            //读取规格信息
			typeTemplateService.findSpecList(newValue).success(function (response) {
                $scope.specList = response;
            })
        })
    })

    /**
	 * 页面上规格checkbox点击事件
     * @param $event checkbox本身
     * @param specValue 传入规格名称的值
     * @param optionValue 传入规格选项的值
     */
    $scope.updateSpecAttribute=function ($event,specValue,optionValue) {
        var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", specValue);
        //如果没有添加过当前规格
        if(obj == null){
            $scope.entity.goodsDesc.specificationItems.push(
            	{
                    "attributeName": specValue,
                    "attributeValue": [optionValue]
                }
            );
		}else{
        	//如果是选中的状态
        	if($event.target.checked){
                //追加一个选项
                obj.attributeValue.push(optionValue);
			}else{
        		//取消勾选，去掉一个选项
                var optionIndex = obj.attributeValue.indexOf(optionValue);
                obj.attributeValue.splice(optionIndex, 1);
                //如果我们取消勾选后，当前规格已经没有选项了
                if(obj.attributeValue.length < 1){
                    var specIndex = $scope.entity.goodsDesc.specificationItems.indexOf(obj);
                    $scope.entity.goodsDesc.specificationItems.splice(specIndex, 1);
				}
			}
		}

        $scope.createItemList();
    }

    // 1. 	创建$scope.createItemList方法，同时创建一条有基本数据，不带规格的初始数据
	$scope.createItemList=function () {
        // 参考: $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }]
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' }];
        // 2. 	查找遍历所有已选择的规格列表，后续会重复使用它，所以我们可以抽取出个变量items
		var items = $scope.entity.goodsDesc.specificationItems;
		for(var i = 0; i < items.length; i++){
            // 9. 	回到createItemList方法中，在循环中调用addColumn方法，并让itemList重新指向返回结果;
            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
    }
    // 3. 	抽取addColumn(当前的表格，列名称，列的值列表)方法，用于每次循环时追加列
    addColumn=function (list,specName,optionName) {
        // 4. 	编写addColumn逻辑，当前方法要返回添加所有列后的表格，定义新表格变量newList
		var newList = [];
        // 5. 	在addColumn添加两重嵌套循环，一重遍历之前表格的列表，二重遍历新列值列表
		for(var i = 0; i < list.length; i++){
			for(var j = 0; j < optionName.length; j++){
                // 6. 	在第二重循环中，使用深克隆技巧，把之前表格的一行记录copy所有属性，
				// 用到var newRow = JSON.parse(JSON.stringify(之前表格的一行记录));
                var newRow = JSON.parse(JSON.stringify(list[i]));
                // 7. 	接着第6步，向newRow里追加一列
                newRow.spec[specName]=optionName[j];
                // 8. 	把新生成的行记录，push到newList中
                newList.push(newRow);
			}
		}
		return newList;
    }

    //商品状态声明
    $scope.status=['未审核','已审核','审核未通过','关闭'];

    $scope.itemCatList=[];//商品分类列表
    /**
	 * 查询所有商品分类列表,按id组装数组
     */
	$scope.findAllItemCat=function () {
		itemCatService.findAll().success(function (response) {
			for(var i = 0; i < response.length; i++){
                $scope.itemCatList[response[i].id] = response[i].name;
			}
        })
    }
});
