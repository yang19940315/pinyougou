package com.zbf.web;

import com.alibaba.fastjson.JSON;
import com.zbf.common.ResponseResult;
import com.zbf.core.CommonUtils;
import com.zbf.core.page.Page;
import com.zbf.core.utils.FileUploadDownUtils;
import com.zbf.core.utils.UID;
import com.zbf.enmu.MyRedisKey;
import com.zbf.service.TiKuService;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.resource.HttpResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作者：LCG
 * 创建时间：2019/2/14 11:00
 * 描述：
 */
@RequestMapping("tiku")
@RestController
public class TiKuGuanLiController {
    @Autowired
    private TiKuService tiKuService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加题库信息
     * @param request
     * @return
     */
    @RequestMapping("toaddTiKuInfo")
    public ResponseResult toaddTiKuInfo(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        //获取数据
        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap ( request );
        //存入数据
        try {
            parameterMap.put ( "id",UID.getUUIDOrder () );
            tiKuService.addTiKuInfo ( parameterMap,redisTemplate);
            responseResult.setSuccess ( "ok" );
        }catch (Exception e){
            e.printStackTrace ();
            responseResult.setError ( "error" );
            //删除上一步骤中写入的数据
            redisTemplate.opsForHash ().delete ( MyRedisKey.TIKU.getKey (),parameterMap.get ( "id" ).toString ());
        }

        return responseResult;

    }

    /**
     * 题库列表
     * @param httpServletRequest
     * @return
     */
    @RequestMapping("getTikuList")
    public ResponseResult getTikuList(HttpServletRequest httpServletRequest){

        Map<String, Object> paramsJsonMap = CommonUtils.getParamsJsonMap ( httpServletRequest );

        Page<Map<String,Object>> page=new Page<> ();

        ResponseResult responseResult=ResponseResult.getResponseResult ();
        //设置查询参数
        page.setParams ( paramsJsonMap );

        Page.setPageInfo ( page, paramsJsonMap);

        //
        tiKuService.getTikuList ( page );
        //

        responseResult.setResult ( page );

        return responseResult;

    }

    /**
     * 更新题库信息
     * @param request
     * @return
     */
    @RequestMapping("updateTiKuInfo")
    public ResponseResult updateTiKuInfo(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        Map<String, Object> paramsJsonMap = CommonUtils.getParamsJsonMap ( request );

        tiKuService.updateTiKuInfo ( paramsJsonMap,redisTemplate );

        responseResult.setSuccess ( "ok" );

        return responseResult;
    }

    /**
     * 从redis中获取题库列表信息
     * @param request
     * @return
     */
    @RequestMapping("getTikuListFromRedis")
    public ResponseResult getTikuListFromRedis(HttpServletRequest request){

        List<Map<String,Object>> values = redisTemplate.opsForHash ().values ( MyRedisKey.TIKU.getKey () );

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        responseResult.setResult ( values );

        return responseResult;

    }

    /**
     * 手动添加试题
     * @return
     */
    @RequestMapping("toAddShiTi")
    public ResponseResult toAddShiTi(HttpServletRequest request) throws Exception {

        ResponseResult responseResult=ResponseResult.getResponseResult ();
        //获取请求数据
        Map<String, Object> parameterMap = CommonUtils.getParameterMap ( request );
        //写入数据
        tiKuService.addShitiInfo ( redisTemplate,parameterMap );

        responseResult.setSuccess ( "ok" );

        return responseResult;
    }

    /**
     *
     *  试题管理 试题列表
     * @param request
     * @return
     */
    @RequestMapping("togetShitiList")
    public ResponseResult togetShitiList(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        Map<String, Object> parameterMap = CommonUtils.getParamsJsonMap ( request );

        Page<Map<String,Object>> page=new Page<> ();

        Page.setPageInfo ( page,parameterMap );

        tiKuService.getShitiList ( page );

        responseResult.setResult ( page );

         return responseResult;
    }

    @RequestMapping("getExceltemplate")
    public void getExceltemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {

        File excelTemplate = FileUploadDownUtils.getExcelTemplate ( "exceltemplate/timu.xlsx" );

        FileUploadDownUtils.responseFileBuilder ( response,excelTemplate,"数据模板【题目】.xlsx" );

    }


    /**
     * 根据ID获取试题信息
     * @param request
     * @return
     */
    @RequestMapping("getShitiById")
    public ResponseResult getShitiById(HttpServletRequest request){

        ResponseResult responseResult=ResponseResult.getResponseResult ();

        Map<String, Object> parameterMap = CommonUtils.getParameterMap ( request );

        Map<String, Object> shiTiById = tiKuService.getShiTiById ( parameterMap );

        responseResult.setResult ( shiTiById );

        return responseResult;

    }

    @RequestMapping("/toImportExcelData")
    public ResponseResult toImportExcelData(@RequestParam("file") MultipartFile file,HttpServletRequest request) throws Exception {
        ResponseResult responseResult=ResponseResult.getResponseResult();
        //得到文件的输入流
        InputStream inputStream = file.getInputStream();
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheetAt = xssfWorkbook.getSheetAt(0);
        //获取数据的行数
        int physicalNumberOfRows = sheetAt.getPhysicalNumberOfRows();

        //获取第一行
        XSSFRow row1 = sheetAt.getRow(0);
        XSSFCell cell = row1.getCell(0);
        String stringCellValue = cell.getStringCellValue();//获取字符数据
        List<Map<String,Object>> listdata=new ArrayList<>();
        for (int i=1;i<physicalNumberOfRows;i++){
            XSSFRow row = sheetAt.getRow(i);
            row.getPhysicalNumberOfCells();
            Map<String,Object> maprow=new HashMap<>();
            maprow.put("tigan",row.getCell(0).getStringCellValue());
            maprow.put("xuanxiangbianhao",row.getCell(1).getStringCellValue());
            List<String> xuanxiangmiaoshu=new ArrayList<>();
            xuanxiangmiaoshu.add(row.getCell(2).getStringCellValue());
            xuanxiangmiaoshu.add(row.getCell(3).getStringCellValue());
            xuanxiangmiaoshu.add(row.getCell(4).getStringCellValue());
            xuanxiangmiaoshu.add(row.getCell(5).getStringCellValue());
            maprow.put("xuanxiangmiaoshu",JSON.toJSONString(xuanxiangmiaoshu));
            maprow.put("daan",row.getCell(6).getStringCellValue());
            if (row.getCell(7)!=null){
                maprow.put("timujiexi",row.getCell(7).getStringCellValue());
            }
            listdata.add(maprow);
        }
        System.out.println(JSON.toJSONString(listdata));

        return  responseResult;
    }

    /**
     * 下载模板
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/getExceltemPlay")
    public void getExceltemPlay(HttpServletRequest request, HttpServletResponse response) throws Exception {
        File excelTemplate = FileUploadDownUtils.getExcelTemplate("exceltemplate/timu.xlsx");
        FileUploadDownUtils.responseFileBuilder(response,excelTemplate,"题目模板.xlsx");
    }


    /**
     * Excel数据的导出
     * @param request
     * @param response
     */
    @RequestMapping("/exportExcelData")
    public void exportExcelData(HttpServletRequest request,HttpServletResponse response) throws IOException {
        //获取数据
        Page<Map<String,Object>> page=new Page<>();
        Map<String, Object> parameterMap = CommonUtils.getParameterMap(request);
        parameterMap.put("tikuid","1000001127103033");
        page.setPageSize(30);
        page.setParams(parameterMap);
        tiKuService.getShitiList(page);
        List<Map<String, Object>> resultList = page.getResultList();

        //POI的api的操作
        XSSFWorkbook xssfWorkbook=new XSSFWorkbook();
        XSSFSheet sheet = xssfWorkbook.createSheet("题库1");
        XSSFRow row1 = sheet.createRow(0);
        row1.createCell(0).setCellValue("ID");
        row1.createCell(1).setCellValue("答案");
        row1.createCell(2).setCellValue("答案解析");
        row1.createCell(3).setCellValue("题干描述");
        row1.createCell(4).setCellValue("试题类型");
        /*row1.createCell(5).setCellValue("ID");
        row1.createCell(6).setCellValue("ID");*/
        for(int i=0;i<resultList.size();i++){
            Map<String, Object> map = resultList.get(i);
            XSSFRow row = sheet.createRow(i+1);
            List<String> collect = map.keySet().stream().collect(Collectors.toList());

            for (int j=0;j<collect.size();j++){
                XSSFCell cell = row.createCell(j);
                cell.setCellValue(map.get(collect.get(j)).toString()!=null?map.get(collect.get(j)).toString().toString():"");

            }
        }
        //输出工作簿
        String filename=new String("信息表1.xlsx".getBytes(),"ISO8859-1");
        response.setContentType("application/octet-steam;charset=ISO8859-1");
        response.setHeader("Content-Disposition","attachment;filename="+filename);

        xssfWorkbook.write(response.getOutputStream());
    }


    /**
     * 题库导出
     * @param request
     * @return
     */
    @RequestMapping("/exportTikuData")
    public void exportTikuData(HttpServletRequest request,HttpServletResponse response) throws IOException {
        /*ResponseResult responseResult=ResponseResult.getResponseResult();*/
        //创建page用于储存map
        Page<Map<String,Object>> page=new Page<>();
        //获取前台传来的数据
        Map<String, Object> parameterMap = CommonUtils.getParameterMap(request);
        //定义下载的最大数量
        page.setPageSize(30);
        page.setParams(parameterMap);
        tiKuService.getShitiList(page);
        //获取返回的数据存入list集合中
        List<Map<String,Object>> list = page.getResultList();
        //POI操作创建XLSX表格
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        //创建并命名表格中的页名
        XSSFSheet sheet = xssfWorkbook.createSheet("题库");
        //创建其中的第一行
        XSSFRow row = sheet.createRow(0);
        //创建第一行数据
        row.createCell(0).setCellValue("ID");
        row.createCell(1).setCellValue("答案");
        row.createCell(2).setCellValue("答案解析");
        row.createCell(3).setCellValue("题干描述");
        row.createCell(4).setCellValue("试题类型");
        //把返回的数据添加到表格中
        for(int i=0;i<list.size();i++){
            //回去list的中的每条数据
            Map<String, Object> map = list.get(i);
            //创建每一行
            XSSFRow row1 = sheet.createRow(i+1);
            //获取map中的数据条数
            List<String> collect = map.keySet().stream().collect(Collectors.toList());
            for(int j=0;j<collect.size();j++){
                 row1.createCell(j).setCellValue(map.get(collect.get(j)).toString()!=null?map.get(collect.get(j)).toString():"");
            }

        }
        //输出工作簿
        //定义工作簿名字和字符集
        String filename=new String("信息表2.xlsx".getBytes(),"ISO8859-1");
        response.setContentType("application/octet-steam;charset=ISO8859-1");
        response.setHeader("Content-Disposition","attachment;filename="+filename);
        xssfWorkbook.write(response.getOutputStream());

        /*for(int i=0;i<resultList.size();i++){
            Map<String, Object> map = resultList.get(i);
            XSSFRow row = sheet.createRow(i+1);
            List<String> collect = map.keySet().stream().collect(Collectors.toList());

            for (int j=0;j<collect.size();j++){
                XSSFCell cell = row.createCell(j);
                cell.setCellValue(map.get(collect.get(j)).toString()!=null?map.get(collect.get(j)).toString().toString():"");

            }
        }
        //输出工作簿
        String filename=new String("信息表1.xlsx".getBytes(),"ISO8859-1");
        response.setContentType("application/octet-steam;charset=ISO8859-1");
        response.setHeader("Content-Disposition","attachment;filename="+filename);

        xssfWorkbook.write(response.getOutputStream());*/

       /* responseResult.setSuccess("ok");

        return responseResult;*/
    }

}
