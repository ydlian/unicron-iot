package com.me.unicron.iot.gateway.service.impl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.orange.comp.commonapi.gift.GiftApi;

public class LogUploadServiceImpl {
    

    @Resource
    private GiftApi giftApi;
    
    /**
     * 文件上传到gift系统
     * @param request
     * @param response
     */
    public void filesUpload(HttpServletRequest request, HttpServletResponse response) {

    }

}
