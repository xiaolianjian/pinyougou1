package com.pinyougou.manager.controller;

import com.pinyougou.utils.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Steven
 * @version 1.0
 * @description com.pinyougou.shop.controller
 * @date 2018-11-22
 */
@RestController
public class UploadController {

    @Value("${FAST_DFS_SERVICE_URL}")
    private String FAST_DFS_SERVICE_URL;

    @RequestMapping("upload")
    public Result upload(MultipartFile file){

        try {
            //获取文件的后缀
            String oldName = file.getOriginalFilename();  //原来的文件名
            String extName = oldName.substring(oldName.lastIndexOf(".") + 1);  //后缀名
            //把用户上传的文件，上传到fastDFS中
            FastDFSClient dfsClient = new FastDFSClient("classpath:fdfs_client.conf");
            //开始上传，得到fileId
            String fileId = dfsClient.uploadFile(file.getBytes(), extName, null);
            //把拼接图片url返回
            String url = FAST_DFS_SERVICE_URL + fileId;
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, "文件上传失败！");
    }
}
