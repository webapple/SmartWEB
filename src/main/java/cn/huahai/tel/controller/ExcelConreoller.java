package cn.huahai.tel.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.huahai.tel.bean.ResponseResult;
import cn.huahai.tel.service.IDataServer;
import cn.huahai.tel.service.IGenerateExcelDataServer;
import cn.huahai.tel.service.IReadExcel;
import cn.huahai.tel.service.IkeyPointCellServer;
import cn.huahai.tel.util.ExcelMap;
import cn.huahai.tel.util.FileNameConversion;
import cn.huahai.tel.util.Pinyin;

/**
 * 概述：小区组，和小区表的导入
 * @author lizhuodong
 *
 */

@Controller
@RequestMapping("/excel")
public class ExcelConreoller {
	@Resource
	IDataServer ds;
	@Resource
	IkeyPointCellServer kpcs;
	@Resource
	IReadExcel ire;
	@Resource
	IGenerateExcelDataServer geds;
	
	//打印日志
	private static Logger logger = Logger.getLogger(ExcelConreoller.class);
	/**
	 * 设置重点小区组
	 * @param cellStr 小区组中包含的小区数组
	 * @param tagname 小区组名称
	 * @return 是否设置成功
	 */
	@RequestMapping("/setCell.do")
	@ResponseBody
	public ResponseResult<Void> setCell(@RequestParam(value = "cellStr[]")String cellStr,String tagname) {
		/**
		 * 声明返回值类型
		 */
		ResponseResult<Void> rr = new ResponseResult<Void>();
		
		String[] cellList = cellStr.split(",");
		 // String数组转List集合
	    List<String> mlist = Arrays.asList(cellList);
	    //获取表名首字母
	    String pinyintagName = Pinyin.getPinYinHeadChar("YM_BZH_"+tagname);
	    //查看表名称是否唯一
	    if(kpcs.selectTagNameExists(pinyintagName)) {
	    	/*添加小区组*/
	    	kpcs.addCellMapping(pinyintagName, tagname, mlist);
	    	rr.setMessage("设置成功");
	 		rr.setState(1);
	    }else {
	    	rr.setMessage("小区组名称 (拼音首字母一致，即认为相同) 已经存在,请重新输入");
			rr.setState(0);
	    }
		return rr;
	}
	/**
	 * 查询所有的映射关系
	 * @return 所有的小区组列表
	 */
	@RequestMapping("/selectAllMapping.do")
	@ResponseBody
	public ResponseResult<List<String>> selectAllMapping(){
		ResponseResult<List<String>> rr = new ResponseResult<List<String>>();
		rr.setData(kpcs.selectAllMapping());
		rr.setMessage("查询成功");
		rr.setState(1);
		return rr;
	}
	
	/**
	 * 根据tagname删除映射关系
	 * @param tagname 重点小区组名称
	 * @return 是否成功
	 */
	@RequestMapping("/deleteMappingByTagname.do")
	@ResponseBody
	public ResponseResult<Void> deleteMappingByTagname(String tagname){
		ResponseResult<Void> rr = new ResponseResult<Void>();
		kpcs.deleteTagname(tagname);
		rr.setMessage("删除成功");
		rr.setState(1);
		return rr;
	}
	

	/**
	 * 获取前端提交的cell表格，解析excel，更新cell表，
	 * 并追加某些字段，<b>excel的正确性由前端来判断，后台假设cell数据已经是正确的了</b>
	 * @param session 获取文件流
	 * @param file 文件对象
	 * @param fields 字段
	 * @return 是否设置成功
	 */
	@RequestMapping("/updateCell.do")
	@ResponseBody
	public ResponseResult<String> updateCell(HttpSession session, 
			@RequestParam("file") MultipartFile file,
			@RequestParam("fields") List<String> fields){
		ResponseResult<String> rr = new ResponseResult<String>();
		try {
			//获取服务器的真实路径
			String path =  session.getServletContext().getRealPath("/");
			file.transferTo(new File(path,"/upload/"+file.getOriginalFilename()));
			File fileExcel = new File(path,"/upload/"+file.getOriginalFilename());
			FileInputStream fis =new FileInputStream(fileExcel);
			//将fields设置到map中
			ExcelMap map = new ExcelMap();
			ire.setMapping(fields, map.m);
			//解析excel
			List<Map<String,String>> l = ire.readExcel(fis, fileExcel.getName(), map.m);
			//创建cell表
			ire.createTable("YM_SYS_CELL_T", fields);
			//批量插入数据
			ire.insertBatchData(l,"YM_SYS_CELL_T");
			//创建NB表，同时赋值数据
			ire.createYM_SYS_NB_T();
			//追加ps字段
			ire.addfield();
//			删除原表
			ire.deleteTablename("YM_SYS_CELL");
			ire.deleteTablename("YM_SYS_NB");
//			更改名称
			ire.changTablename("YM_SYS_CELL_T", "YM_SYS_CELL");
			ire.changTablename("YM_SYS_NB_T", "YM_SYS_NB");
			rr.setMessage("上传成功");
			rr.setState(1);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			rr.setData(e.getMessage());
			rr.setMessage("解析失败，请重新上传，或者联系后台管理员");
			rr.setState(0);
		}
		return rr;
	}
	
	
	
	
	/**
	 * 根据vip小区或者重点小区组，筛选数据，返回excel表，
	 * @param tagName 组名称 
	 * @param excelName 文件名称 （符合一定的格式：如：全网监控_Q201806141115）
	 * @param flag flag 如果为“cell” 查询筛选小区数据 如果为“AllNetWork” 则查询筛选全网数据
	 * @param session session对象
	 * @param response 响应对象
	 * @param request request
	 * @param filename 文件名
	 * @return excel文件二进制数据
	 */
	@RequestMapping("/generatePointKeyExcel.do")
	@ResponseBody
	public byte[] generatePointKeyExcel(@RequestParam("tagName")String tagName, 
			@RequestParam("excelName") String excelName,
			@RequestParam("flag") String flag,
			@RequestParam("filename") String filename,
			HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		String timeNumber=excelName.split("\\.")[0].split("_Q")[1];
		try {
			FileNameConversion.setHeader(response, request, filename);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error(e);
			e.printStackTrace();
		}
		byte[] data = null;
		if("cell".equals(flag)) {
			data = geds.generateExcel(tagName, timeNumber, session);
		}else if("AllNetWork".equals(flag)) {
			System.out.println("查询全网excelvip数据");
			System.out.println(tagName);
			System.out.println(timeNumber);
			data = geds.generateNetWorkExcel(tagName, timeNumber, session);
		}
		return data;
	}
	/**
	 * 生成小时级excel数据
	 * @param tagName 查询范围的tag 如： vip小区，all，某某演唱会
	 * @param excelName 文件名（包含查询的时间点）
	 * @param flag 查询的是小时级全网数据，还是小时级小区数据 如果为“cell” 查询筛选小区数据 如果为“AllNetWork” 则查询筛选全网数据
	 * @param session session
	 * @param filename 文件名
	 * @param response response
	 * @param request request
	 * @return  文件流
	 */
	@RequestMapping("/generateHourPointKeyExcel.do")
	@ResponseBody
	public byte[] generateHourPointKeyExcel(@RequestParam("tagName")String tagName, 
			@RequestParam("excelName") String excelName,
			@RequestParam("flag") String flag,
			@RequestParam("filename") String filename,
			HttpSession session,HttpServletResponse response,HttpServletRequest request) {
		String timeNumber=excelName.split("ALLCELL")[1];
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		try {
			FileNameConversion.setHeader(response, request, filename);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] data = null;
		if("cell".equals(flag)) {
			data = geds.generateHourCellExcel(tagName, excelName, session);
		}else if("AllNetWork".equals(flag)) {
			System.out.println("查询全网(小时级)excelvip数据");
			System.out.println(tagName);
			System.out.println(timeNumber);
			data = geds.generateHourNetWorkExcel(tagName, excelName, session);
		}
		return data;
	}
	/**
	 * 生成天级的小区级数据
	 * @param tagName tag名称
	 * @param excelName 原始表名
	 * @param flag 判断是否是全网数据（全网数据接口弃用，改用generateDayAllNetTableExcel），还是小区数据
	 * @param session 返回二进制数据（输入流）
	 * @param filename 文件名
	 * @param request 添加响应头（文件名）所需
	 * @param response 添加响应头（文件名）所需
	 * @return 天级excel小区表数据
	 */
	@RequestMapping("/generateDayPointKeyExcel.do")
	@ResponseBody
	public byte[] generateDayPointKeyExcel(@RequestParam("tagName")String tagName, 
			@RequestParam("excelName") String excelName,
			@RequestParam("flag") String flag,
			@RequestParam("filename") String filename,
			HttpSession session,HttpServletRequest request,HttpServletResponse response) {
		String timeNumber=excelName.split("ALLCELL")[1];

		try {
			FileNameConversion.setHeader(response, request, filename);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] data = null;
		if("cell".equals(flag)) {
			data = geds.generateDayCellExcel(tagName, excelName, session);
		}else if("AllNetWork".equals(flag)) {
			System.out.println("查询全网(小时级)excelvip数据");
			System.out.println(tagName);
			System.out.println(timeNumber);
			data = geds.generateHourNetWorkTableExcel(tagName, session);
		}
		return data;
	}
	/**
	 * 下载小时级全网数据excel前24条
	 * @param tagName 查询范围的tag 如： vip小区，all，某某演唱会
	 * @param session 生成二进制数据流所需
	 * @param response 添加响应头所需
	 * @param request 添加响应头所需
	 * @return excel二进制数据
	 */
	@RequestMapping("/generateHourAllNetTableExcel.do")
	@ResponseBody
	public byte[] generateHourAllNetTableExcel(
			@RequestParam("tagName")String tagName,
			HttpSession session,
			HttpServletResponse response,
			HttpServletRequest request) {
		try {
			FileNameConversion.setHeader(response, request, "小时级小区数据.xlsx");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] data = geds.generateHourNetWorkTableExcel(tagName, session);
		return data;
	}
	
	/**
	 * 下载天级全网数据excel前30条
	 * @param tagName 查询范围的tag 如： vip小区，all，某某演唱会
	 * @param session 生成二进制数据流所需
	 * @param response 添加响应头所需
	 * @param request 添加响应头所需
	 * @return excel二进制数据
	 */
	@RequestMapping("/generateDayAllNetTableExcel.do")
	@ResponseBody
	public byte[] generateDayAllNetTableExcel(
			@RequestParam("tagName")String tagName,
			HttpSession session,
			HttpServletResponse response,
			HttpServletRequest request) {
		try {
			FileNameConversion.setHeader(response, request, "天级小区数据.xlsx");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] data = geds.generateDayNetWorkTableExcel(tagName, session);
		return data;
	}
	
}
