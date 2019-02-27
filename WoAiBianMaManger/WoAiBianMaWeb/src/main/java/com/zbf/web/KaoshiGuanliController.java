package com.zbf.web;


import com.alibaba.fastjson.JSON;
import com.zbf.common.ResponseResult;
import com.zbf.core.CommonUtils;
import com.zbf.core.page.Page;
import com.zbf.enmu.MyRedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 考试管理新的
 */
@RestController
@RequestMapping("/kaoshi")
public class KaoshiGuanliController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 加载下拉列表的数据
     * @param request
     * @return
     */
    @RequestMapping("/getShijuanData")
    public ResponseResult getShijuanData(HttpServletRequest request){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        List<Map<String,Object>> range = redisTemplate.opsForList().range(MyRedisKey.SHI_JUAN.getKey(), 0, -1);
        responseResult.setResult(range);

        return responseResult;
    }

    /**
     * 分数分析
     * @param request
     * @return
     */
    @RequestMapping("/getScoreRangData")
    public ResponseResult getScoreRangData(HttpServletRequest request){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        Map<String, Object> parameterMap = CommonUtils.getParameterMap(request);
        //区间值
        List<Double> fenshu1 = JSON.parseObject(parameterMap.get("fenshu1").toString(), List.class);
        List<Double> fenshu2 = JSON.parseObject(parameterMap.get("fenshu2").toString(), List.class);
        List<Map<String,Object>> listbingdata=new ArrayList<>();
        List<String> listbingdatatext=new ArrayList<>();
        for (int i=0;i<fenshu1.size();i++){
            Set shijuanid = redisTemplate.opsForZSet().rangeByScore(parameterMap.get("shijuanid").toString(), fenshu1.get(i), fenshu2.get(i));
            Map<String, Object> map = new HashMap<>();
            String name=""+ fenshu1.get(i)+"-"+fenshu2.get(i);
            map.put("name",name);
            map.put("value",shijuanid.size());
            listbingdata.add(map);
            listbingdatatext.add(name);
        }
        Map<String,Object> mapdata=new HashMap<>();
        mapdata.put("listbingdata",listbingdata);
        mapdata.put("listbingdatatext",listbingdatatext);
        responseResult.setResult(mapdata);


        return responseResult;
    }




}
