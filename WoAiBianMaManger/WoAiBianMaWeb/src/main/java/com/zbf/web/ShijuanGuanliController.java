package com.zbf.web;

import com.zbf.common.ResponseResult;
import com.zbf.core.CommonUtils;
import com.zbf.core.page.Page;
import com.zbf.service.ShijuanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequestMapping("/shijuan")
@RestController
public class ShijuanGuanliController {

    @Autowired
    private ShijuanService shijuanService;

    @RequestMapping("/getUserList")
    public ResponseResult getUserList(HttpServletRequest request){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        Map<String, Object> paramsJsonMap = CommonUtils.getParamsJsonMap(request);
        Page<Map<String,Object>> page= new Page<>();
        page.setParams(paramsJsonMap);
        page.setPageInfo(page,paramsJsonMap);
        shijuanService.getUserList(page);
        responseResult.setResult(page);

        return responseResult;
    }

}
