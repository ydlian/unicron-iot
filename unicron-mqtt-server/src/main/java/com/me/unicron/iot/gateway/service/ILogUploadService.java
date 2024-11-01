package com.me.unicron.iot.gateway.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ILogUploadService {
    
    /**
     * 文件上传到gift系统
     * @param request
     * @param response
     */
    public void filesUpload(HttpServletRequest request, HttpServletResponse response);

}
