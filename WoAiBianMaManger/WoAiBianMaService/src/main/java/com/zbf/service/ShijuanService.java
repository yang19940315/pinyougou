package com.zbf.service;

import com.zbf.core.page.Page;
import com.zbf.mapper.ShijuanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ShijuanService {

    @Autowired
    private ShijuanMapper shijuanMapper;

    public void getUserList(Page<Map<String, Object>> page) {
        List<Map<String,Object>> list = shijuanMapper.getUserList(page);
       /*list.forEach((item)->{
            if(item.get("sex").toString().equals("1")){
                item.put("sex","男");
            }else {
                item.put("sex","女");

            }
        });*/
        for (Map<String, Object> map : list) {
            if(map.get("sex").toString().equals("1")){
                map.put("sex","男");
            }else{
                map.put("sex","女");
            }
        }


        page.setResultList(list);
    }
}
