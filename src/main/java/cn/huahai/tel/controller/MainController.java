package cn.huahai.tel.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.junit.runners.Parameterized.Parameter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.huahai.tel.bean.AllNetworkBase;
import cn.huahai.tel.bean.DayAllNetworkBase;
import cn.huahai.tel.bean.DayTopCellworkBase;
import cn.huahai.tel.bean.HourAllNetWorkBase;
import cn.huahai.tel.bean.HourTopCellworkBase;
import cn.huahai.tel.bean.LinkFailure;
import cn.huahai.tel.bean.ResponseResult;
import cn.huahai.tel.bean.TopCellworkBase;
import cn.huahai.tel.service.IDataServer;
import cn.huahai.tel.service.IGenerateExcelDataServer;
import cn.huahai.tel.service.ILinkFailureServer;
import cn.huahai.tel.service.ITopCellServer;
import cn.huahai.tel.service.IkeyPointCellServer;
import cn.huahai.tel.util.Pinyin;
/**
 * 主页面显示，全网数据，top小区数据
 * @author lizhuodong
 *
 */
@Controller
@RequestMapping("/main")
public class MainController {
	@Resource
	IDataServer ds;
	@Resource
	ITopCellServer tcs;
	@Resource
	ILinkFailureServer lfs;
	@Resource
	IkeyPointCellServer kpcs;
	@Resource
	IGenerateExcelDataServer geds;

	// 显示主页面
	/**
	 * 显示主页面
	 * @param three 三级导航
	 * @param two 二级导航
	 * @param session session对象用于绑定导航信息
	 * @return 主页面
	 */
	@RequestMapping("/showIndex.do")
	public String showIndex(@RequestParam("three") String three, @RequestParam("two") String two, HttpSession session) {
		session.setAttribute("three", three);
		session.setAttribute("two", two);
		return "index";
	}

	/**
	 * 筛选全网数据，传递一个参数threeName 
	 * （需要查询的小区组，如：“20180612重点小区”或者“VIP小区”），此方法不区分重点小区和Vip小区，方法内部会进行判断，
	 * 返回相应的数据，如果不需要进行筛选，则传递“null”字符串
	 * 根据具体的小区组名称查询和筛选相关全网数据
	 * @param threeName 当前小区组名称
	 * @param session 获取当前导航信息
	 * @return
	 */
	@RequestMapping("/selectAllData.do")
	@ResponseBody
	public ResponseResult<AllNetworkBase[]> selectAllData( String threeName,HttpSession session) {
		System.out.println(threeName);
		
//		generateNetworkCellDataExcel
		ResponseResult<AllNetworkBase[]> rr = new ResponseResult<AllNetworkBase[]>();
		String tableReg = "^YM_AQ_ALL\\d{12}$";
		String field = "hm";
		// 根据对应的导航数据显示对应的表
		if ("2".equals(session.getAttribute("three"))) {
			tableReg = "^YM_AH_ALL\\d{10}$";
			field = "hh";
		}
		ArrayList<String> tablenames = ds.selectStandardTableName(tableReg);
		//如果是十五分钟重点则，进行重新赋值
		if (!threeName.equals("null")) {
			String tablename = tablenames.get(tablenames.size() - 1);
			String timeNumber=tablename.substring(tablename.length()-12, tablename.length());
			System.out.println(timeNumber);
			//geds.generateNetworkCellDataExcel(threeName, timeNumber);
			rr.setData(geds.generateNetworkCellDataExcel(threeName, timeNumber));
		}else {
			rr.setData(ds.selectAllData(tablenames.get(tablenames.size() - 1), field));
		}
		rr.setMessage("查询成功");
		rr.setState(1);
		return rr;
	}

	// 查询top小区
	/**
	 * 筛选Top小区数据，传递一个参数threeName 
	 * （需要查询的小区组，如：“20180612重点小区”或者“保障重点小区”vip小区有单独的接口），
	 * 返回相应的数据，如果不需要进行筛选，则传递“null”字符串
	 * @param session 获取当前导航位置
	 * @param threeName 获取小区组
	 * @return 筛选之后的top小区数据
	 */
	@RequestMapping("/selectTopCell.do")
	@ResponseBody
	public ResponseResult<TopCellworkBase[]> selectAllTopCell(HttpSession session, String threeName) {
		ResponseResult<TopCellworkBase[]> rr = new ResponseResult<TopCellworkBase[]>();
		
		//添加15分钟级表名正则
		String tableReg = "^YM_AQ_ALLCELL\\d{12}$";
		//添加15分钟级特殊字段
		String field = "hm";
		// 根据对应的导航数据显示对应的表
		if ("1".equals(session.getAttribute("three"))) {
			tableReg = "^YM_AH_ALLCELL\\d{10}$";
			field = "hh";
		}
		// 查询出符合正则的所有表名
		ArrayList<String> tablenames = ds.selectStandardTableName(tableReg);
		// 根据表名查询所有信息
		/*
		 * 如果是重点小区设置，则进行连表查询
		 * */
		if (!threeName.equals("null")) {
			//对中文参数进行编码
			try {
				threeName = URLDecoder.decode(threeName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
			System.out.println(threeName);
			rr.setData(kpcs.selectKeyPontCellInfo(tablenames.get(tablenames.size() - 1), Pinyin.getPinYinHeadChar("YM_BZH_"+threeName)));
		}else {
			System.out.println("小时级别top小区触发");
			System.out.println(field);
			System.out.println(tablenames.get(tablenames.size() - 1));
			rr.setData(tcs.selectAllTopCell(tablenames.get(tablenames.size() - 1), field));
		}
		rr.setMessage("查询成功");
		rr.setState(1);
		return rr;
	}
	
	// 小时级查询top小区
	/**
	 * 筛选Top小区数据，传递一个参数threeName 
	 * （需要查询的小区组，如：“20180612重点小区”或者“保障重点小区”）,
	 * 返回相应的数据，如果不需要进行筛选，则传递“null”字符串
	 * @return 筛选之后的top小区数据
	 */
	@RequestMapping("/selectHourTopCell.do")
	@ResponseBody
	public ResponseResult<HourTopCellworkBase[]> selectAllHourTopCell(HttpSession session,String tagName){
		ResponseResult<HourTopCellworkBase[]> rr = new ResponseResult<HourTopCellworkBase[]>();
		
		//添加小时级表名正则
		String tableReg = "^YM_AH_ALLCELL\\d{10}$";
		// 查询出符合正则的所有表名
		ArrayList<String> tablenames = ds.selectStandardTableName(tableReg);
		// 根据表名查询所有信息
		System.out.println(tablenames.get(tablenames.size() - 1));
		String tableName  = tablenames.get(tablenames.size() - 1);
		
		if("all".equals(tagName)) {
			rr.setData(tcs.selectAllHourTopCell(tableName));
		}else if("VIP小区".equals(tagName)) {
			rr.setData(tcs.selectVIPHourTopCell(tableName));
		}else{
			//Pinyin.getPinYinHeadChar("YM_BZH_"+tagName);
			System.out.println(Pinyin.getPinYinHeadChar("YM_BZH_"+tagName));
			rr.setData(tcs.selectKeyPointHourTopCell(tableName, Pinyin.getPinYinHeadChar("YM_BZH_"+tagName)));
		};
		rr.setMessage("查询成功");
		rr.setState(1);
		return rr;
	}
	//查询天级top小区数据
	@RequestMapping("/selectDayTopCell.do")
	@ResponseBody
	public ResponseResult<DayTopCellworkBase[]> selectDayTopCell(String tagName){
		//声明返回值类型
		ResponseResult<DayTopCellworkBase[]> rr = new ResponseResult<DayTopCellworkBase[]>();
		//添加天级表名正则
		String tableReg = "^YM_AD_ALLCELL\\d{8}$";
		// 查询出符合正则的所有表名
		ArrayList<String> tablenames = ds.selectStandardTableName(tableReg);
		// 根据表名查询所有信息
		System.out.println(tablenames.get(tablenames.size() - 1));
		String tableName  = tablenames.get(tablenames.size() - 1);
		if("all".equals(tagName)) {
			rr.setData(tcs.selectAllDayTopCell(tableName));
		}else if("VIP小区".equals(tagName)) {
			rr.setData(tcs.selectVIPDayTopCell(tableName));
		}else{
			//Pinyin.getPinYinHeadChar("YM_BZH_"+tagName);
			System.out.println(Pinyin.getPinYinHeadChar("YM_BZH_"+tagName));
			rr.setData(tcs.selectPointKeyDayTopCell(tableName, Pinyin.getPinYinHeadChar("YM_BZH_"+tagName)));
		};
		rr.setMessage("查询成功");
		rr.setState(1);
		return rr;
	}
	/**
	 * 查询小时级全网数据.
	 * @param tagName 当前小时级选项卡名称
	 * @return
	 */
	@RequestMapping("/selectHourAllNetCell.do")
	@ResponseBody
	public ResponseResult<HourAllNetWorkBase[]> selectAllHourALLNetworkCell(String tagName){
		ResponseResult<HourAllNetWorkBase[]> rr = new ResponseResult<HourAllNetWorkBase[]>();
		rr.setData(geds.generateHourNetworkCellDataExcel(tagName));
		rr.setMessage("查询小时级全网数据成功");
		rr.setState(1);
		return rr;
	}
	/**
	 * 查询天级全网数据.
	 * @param tagName 当前天级选项卡名称
	 * @return
	 */
	@RequestMapping("/selectDayAllNetCell.do")
	@ResponseBody
	public  ResponseResult<DayAllNetworkBase[]> selectAllDayAllNetworkCell(String tagName){
		ResponseResult<DayAllNetworkBase[]> rr = new ResponseResult<DayAllNetworkBase[]>();
		rr.setData(geds.generateDayNetworkCellDataExcel(tagName));
		rr.setMessage("查询天级全网数据成功");
		rr.setState(1);
		return rr;
	}
	// 查询链路故障
	/**
	 * 查询链路故障
	 * @return 链路故障信息
	 */
	@RequestMapping("/selectLinkFailure.do")
	@ResponseBody
	public ResponseResult<LinkFailure[]> selectLinkFailure() {
		ResponseResult<LinkFailure[]> rr = new ResponseResult<LinkFailure[]>();
		rr.setData(lfs.selectLinkFailure());
		System.out.println("链路故障：查询出"+lfs.selectLinkFailure().length+"条");
		rr.setMessage("查询成功");
		rr.setState(1);
		return rr;
	}
	/**
	 * 查询链路故障
	 * @return 链路故障信息
	 */
	@RequestMapping("/selectNewLinkFailure.do")
	@ResponseBody
	public ResponseResult<ArrayList<LinkFailure>> selectNewLinkFailure(@RequestParam("tagName") String tagName) {
		ResponseResult<ArrayList<LinkFailure>> rr = new ResponseResult<ArrayList<LinkFailure>>();
		rr.setData(lfs.selectNewLinkFailure(tagName));
		rr.setMessage("查询成功");
		rr.setState(1);
		return rr;
	}
	/**
	 * 查询vip小区信息(15分钟级)
	 * @return vip小区信息
	 */
	@RequestMapping("/getVipCellInfo.do")
	@ResponseBody
	public ResponseResult<TopCellworkBase[]> getVipCellInfo(){
		ResponseResult<TopCellworkBase[]> rr = new ResponseResult<TopCellworkBase[]>();
		//添加15分钟级表名正则
		String tableReg = "^YM_AQ_ALLCELL\\d{12}$";
		//查询出符合正则的所有表名
		ArrayList<String> tablenames = ds.selectStandardTableName(tableReg);
		//设置数据
		rr.setData(kpcs.selectVipCellInfo(tablenames.get(tablenames.size() - 1)));
		rr.setMessage("查询vip小区成功");
		rr.setState(1);
		return rr;
	}
}
