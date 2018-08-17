package cn.huahai.tel.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import cn.huahai.tel.bean.AllNetworkBase;
import cn.huahai.tel.bean.CellInfoBase;
import cn.huahai.tel.bean.CriticalValueBase;
import cn.huahai.tel.bean.DayAllNetworkBase;
import cn.huahai.tel.bean.DayTopCellworkBase;
import cn.huahai.tel.bean.HourAllNetWorkBase;
import cn.huahai.tel.bean.HourTopCellworkBase;
import cn.huahai.tel.bean.TopCellworkBase;
import cn.huahai.tel.bean.User;
import cn.huahai.tel.mapper.GenerateExcelDataMapper;
import cn.huahai.tel.mapper.KeyPointCellMapper;
import cn.huahai.tel.mapper.TopCellworkBaseMapper;
import cn.huahai.tel.util.GenerateExcel;

@Service
public class GenerateExcelDataServer implements IGenerateExcelDataServer {
	@Resource
	IDataServer ds;
	@Resource
	KeyPointCellMapper kpcm;
	@Resource
	GenerateExcelDataMapper gedm;
	@Resource
	TopCellworkBaseMapper tcbm;

	@Resource
	private ISetValueServer svs;
	
	
	@Override
	public List<CriticalValueBase> getSetValue(Map<String,String> data,HttpSession session){
		User user = (User) session.getAttribute("user");
		String flag = "";
		if(data.containsKey("ENODEBID_CELLID")) {
			System.out.println("小区级的excel");
			flag = "topCell";
		}else {
			System.out.println("全网级的excel");
			flag = "allNet";
		}
		return svs.getJsonValueBean(user.getUname(), flag);
	}
	
	@Override
	public CellInfoBase[] generatePointKeyCellDataExcel(String tagName, String timeNumber) {
		CellInfoBase[] data = null;
		String tablename = "YM_AQ_ALLCELL" + timeNumber;
		// 查询出符合正则的所有表名
		// ArrayList<String> tablenames = ds.selectStandardTableName(tablename);
		if ("VIP小区".equals(tagName)) {
			// 添加15分钟级表名正则
			// 调用联表查询的方法 ，对data进行赋值
			System.out.println("VIP小区");
			data = gedm.generateVIPCellDataExcel(tablename);
		} else {
			// 如果是小区组,查询小区表
			String cellTable = kpcm.selectTablenameByTagname(tagName);
			// 两表联查，得出数据
			System.out.println("重点小区组");
			data = gedm.generateCellDataExcel(tablename, cellTable);
		}
		return data;
	}
	
	
	@Override
	public byte[] generateExcel(String tagName, String timeNumber, HttpSession session) {
		CellInfoBase[] data = generatePointKeyCellDataExcel(tagName, timeNumber);
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		for (int i = 0; i < data.length; i++) {
			try {
				dataList.add(GenerateExcel.bean2Map(data[i]));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 获取服务器真实路径
		String path = session.getServletContext().getRealPath("/");
		// 拼接文件路径
		String finalXlsxPath = path + "/web/model/modelCell.xlsx";
		File file = new File(finalXlsxPath);
		List<String> datatitle = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			// 获取excel头部信息
			datatitle = cn.huahai.tel.util.ReadExcel.getTitleArr(fis, file.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GenerateExcel.writeExcel(dataList, finalXlsxPath, datatitle, session,getSetValue(dataList.get(0),session));
	}

	@Override
	public AllNetworkBase[] generateNetworkCellDataExcel(String tagName, String timeNumber) {
		// TODO Auto-generated method stub
		AllNetworkBase[] datas = new AllNetworkBase[4];
		AllNetworkBase data = null;
		String tablename = "^YM_AQ_ALLCELL\\d{12}$";
		// 查询出符合正则的所有表名
		ArrayList<String> tablenames = ds.selectStandardTableName(tablename);
		System.out.println("元素位置" + tablenames.indexOf("YM_AQ_ALLCELL" + timeNumber));
		int start = tablenames.indexOf("YM_AQ_ALLCELL" + timeNumber);
		int end = 0;
		if (start - 4 > 0) {
			end = start - 4;
		}
		int index = 0;
		for (int i = start; i > end; i--) {
			if ("VIP小区".equals(tagName)) {
				// 添加15分钟级表名正则
				// 调用联表查询的方法 ，对data进行赋值
				data = gedm.selectVIPAllNetWork(tablenames.get(i));
			} else {
				// 如果是小区组,查询小区表
				String cellTable = kpcm.selectTablenameByTagname(tagName);
				// 两表联查，得出数据
				data = gedm.selectKeyPonitAllNetWork(tablenames.get(i), cellTable);
			}
			System.out.println(tablenames.get(i));
			datas[index] = data;
			index++;
		}
		System.out.println("已经触发");
		return datas;
	}

	@Override
	public byte[] generateNetWorkExcel(String tagName, String timeNumber, HttpSession session) {
		// TODO Auto-generated method stub
		AllNetworkBase[] data = generateNetworkCellDataExcel(tagName, timeNumber);
		System.out.println("获取数据长度为：" + data.length);
		System.out.println(data[0]);
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		for (int i = 0; i < data.length; i++) {
			try {
				System.out.println(data[i]);
				dataList.add(GenerateExcel.bean2Map(data[i]));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 获取服务器真实路径
		String path = session.getServletContext().getRealPath("/");
		// 拼接文件路径（模板excel）
		String finalXlsxPath = path + "/web/model/modelAllNetWork.xlsx";
		File file = new File(finalXlsxPath);
		List<String> datatitle = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			// 获取excel头部信息
			datatitle = cn.huahai.tel.util.ReadExcel.getTitleArr(fis, file.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GenerateExcel.writeExcel(dataList, finalXlsxPath, datatitle, session,getSetValue(dataList.get(0),session));
	}

	@Override
	public HourAllNetWorkBase[] generateHourNetworkCellDataExcel(String tagName) {

		HourAllNetWorkBase[] datas = null;
		HourAllNetWorkBase data = null;
		String tablename = "^YM_AH_ALLCELL\\d{10}$";
		// 查询出符合正则的所有表名
		ArrayList<String> tablenames = ds.selectStandardTableName(tablename);

		// 查看当前小时级数据是否超过24个
		if (tablenames.size() > 24) {
			datas = new HourAllNetWorkBase[24];
		} else {
			datas = new HourAllNetWorkBase[tablenames.size()];
		}
		Comparator<String> c = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				int timeNumber1 = Integer.parseInt(o1.split("ALLCELL")[1]);
				int timeNumber2 = Integer.parseInt(o1.split("ALLCELL")[1]);
				if (timeNumber1 < timeNumber2)
					return 1;
				// 注意！！返回值必须是一对相反数，否则无效。jdk1.7以后就是这样。
				// else return 0; //无效
				else
					return -1;
			}
		};
		Collections.sort(tablenames, c);
		int i = 0;
		for (String string : tablenames) {
			if (i > 23) {
				break;
			};
			if ("VIP小区".equals(tagName)) {
				// 添加15分钟级表名正则
				// 调用联表查询的方法 ，对data进行赋值
				// data = gedm.generateVIPCellDataExcel(tablename);
				data = gedm.selectVIPHourAllNetWork(string);
			} else if ("all".equals(tagName)) {
				data = gedm.selectALLHourAllNetWork(string);
			} else {
				// 如果是小区组,查询小区表
				String cellTable = kpcm.selectTablenameByTagname(tagName);
				// 两表联查,得出数据
				data = gedm.selectKeyPonitHourAllNetWork(string, cellTable);
			}
			datas[i] = data;
			i++;
		}
		return datas;
	}

	@Override
	public DayAllNetworkBase[] generateDayNetworkCellDataExcel(String tagName) {
		// TODO Auto-generated method stub
		DayAllNetworkBase[] datas = null;
		DayAllNetworkBase data = null;
		//YM_AD_ALLCELL20180724
		String tablename = "^YM_AD_ALLCELL\\d{8}$";
		// 查询出符合正则的所有表名
		ArrayList<String> tablenames = ds.selectStandardTableName(tablename);
		// 查看当前天级数据是否超过30个
		if (tablenames.size() > 30) {
			datas = new DayAllNetworkBase[24];
		} else {
			datas = new DayAllNetworkBase[tablenames.size()];
		}
		Comparator<String> c = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				int timeNumber1 = Integer.parseInt(o1.split("ALLCELL")[1]);
				int timeNumber2 = Integer.parseInt(o1.split("ALLCELL")[1]);
				if (timeNumber1 < timeNumber2)
					return 1;
				// 注意！！返回值必须是一对相反数，否则无效。jdk1.7以后就是这样。
				// else return 0; //无效
				else
					return -1;
			}
		};
		Collections.sort(tablenames, c);
		int i = 0;
		for (String string : tablenames) {
			if (i > 29) {
				break;
			};
			if ("VIP小区".equals(tagName)) {
				// 添加15分钟级表名正则
				// 调用联表查询的方法 ，对data进行赋值
				// data = gedm.generateVIPCellDataExcel(tablename);
				data = gedm.selectVIPDayAllNetWork(string);
			} else if ("all".equals(tagName)) {
				data = gedm.selectALLDayAllNetWork(string);
			} else {
				// 如果是小区组,查询小区表
				String cellTable = kpcm.selectTablenameByTagname(tagName);
				// 两表联查,得出数据
				data = gedm.selectKeyPonitDayAllNetWork(string, cellTable);
			}
			datas[i] = data;
			i++;
		}
		return datas;
	}
	
	@Override
	public byte[] generateDayCellExcel(String tagName, String tableName, HttpSession session) {
		
		DayTopCellworkBase[] dataCell = null;
		if ("VIP小区".equals(tagName)) {
			
			//dataCell = tcbm.selectVIPDayTopCell(tableName);
			dataCell = tcbm.selectAllVIPDayTopCell(tableName);
		} else if ("all".equals(tagName)) {
			//dataCell = tcbm.selectAllDayTopCell(tableName);
			dataCell = tcbm.selectAllAllNetDayTopCell(tableName);
		} else {
			// 如果是小区组,查询小区表
			String cellTable = kpcm.selectTablenameByTagname(tagName);
			// 两表联查,得出数据
			//dataCell = tcbm.selectPointKeyDayTopCell(tableName, cellTable);
			dataCell = tcbm.selectAllPointKeyDayTopCell(tableName, cellTable);
		}
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		System.out.println("查询出数据长度：" + dataCell.length);
		for (int i = 0; i < dataCell.length; i++) {
			try {
				dataList.add(GenerateExcel.bean2Map(dataCell[i]));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 获取服务器真实路径
		String path = session.getServletContext().getRealPath("/");
		// 拼接文件路径（模板excel）
		String finalXlsxPath = path + "/web/model/modelDayCell.xlsx";
		File file = new File(finalXlsxPath);
		List<String> datatitle = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			// 获取excel头部信息
			datatitle = cn.huahai.tel.util.ReadExcel.getTitleArr(fis, file.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GenerateExcel.writeExcel(dataList, finalXlsxPath, datatitle, session,getSetValue(dataList.get(0),session));
	}
	@Override
	public byte[] generateHourNetWorkExcel(String tagName, String tableName, HttpSession session) {
		HourAllNetWorkBase dataAll = null;

		if ("VIP小区".equals(tagName)) {
			// 添加15分钟级表名正则
			// 调用联表查询的方法 ，对data进行赋值
			// data = gedm.generateVIPCellDataExcel(tablename);
			dataAll = gedm.selectVIPHourAllNetWork(tableName);
		} else if ("all".equals(tagName)) {
			dataAll = gedm.selectALLHourAllNetWork(tableName);
		} else {
			// 如果是小区组,查询小区表
			String cellTable = kpcm.selectTablenameByTagname(tagName);
			// 两表联查,得出数据
			dataAll = gedm.selectKeyPonitHourAllNetWork(tableName, cellTable);
		}
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		try {
			dataList.add(GenerateExcel.bean2Map(dataAll));
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 获取服务器真实路径
		String path = session.getServletContext().getRealPath("/");
		// 拼接文件路径（模板excel）
		String finalXlsxPath = path + "/web/model/modelHourAllNetWork.xlsx";
		File file = new File(finalXlsxPath);
		List<String> datatitle = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			// 获取excel头部信息
			datatitle = cn.huahai.tel.util.ReadExcel.getTitleArr(fis, file.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GenerateExcel.writeExcel(dataList, finalXlsxPath, datatitle, session,getSetValue(dataList.get(0),session));
	}

	@Override
	public byte[] generateHourCellExcel(String tagName, String tableName, HttpSession session) {
		HourTopCellworkBase[] dataCell = null;
		if ("VIP小区".equals(tagName)) {
			// 添加15分钟级表名正则
			// 调用联表查询的方法 ，对data进行赋值
			// data = gedm.generateVIPCellDataExcel(tablename);
			//dataCell = tcbm.selectVIPHourTopCell(tableName);
			System.out.println("已经出发");
			dataCell = tcbm.selectAllVIPHourTopCell(tableName);
		} else if ("all".equals(tagName)) {
			dataCell = tcbm.selectAllAllNetHourTopCell(tableName);
		} else {
			// 如果是小区组,查询小区表
			String cellTable = kpcm.selectTablenameByTagname(tagName);
			// 两表联查,得出数据
			//dataCell = tcbm.selectPointKeyHourTopCell(tableName, cellTable);
			dataCell = tcbm.selectAllPointKeyHourTopCell(tableName, cellTable);
		}
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		System.out.println("查询出数据长度：" + dataCell.length);
		for (int i = 0; i < dataCell.length; i++) {
			try {
				dataList.add(GenerateExcel.bean2Map(dataCell[i]));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 获取服务器真实路径
		String path = session.getServletContext().getRealPath("/");
		// 拼接文件路径（模板excel）
		String finalXlsxPath = path + "/web/model/modelHourCell.xlsx";
		File file = new File(finalXlsxPath);
		List<String> datatitle = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			// 获取excel头部信息
			datatitle = cn.huahai.tel.util.ReadExcel.getTitleArr(fis, file.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GenerateExcel.writeExcel(dataList, finalXlsxPath, datatitle, session,getSetValue(dataList.get(0),session));
	}

	@Override
	public byte[] generateHourNetWorkTableExcel(String tagName, HttpSession session) {
		// TODO Auto-generated method stub
		HourAllNetWorkBase[] data = generateHourNetworkCellDataExcel(tagName);
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		
		for (int i = 0; i < data.length; i++) {
			try {
				System.out.println(data[i]);
				dataList.add(GenerateExcel.bean2Map(data[i]));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 获取服务器真实路径
		String path = session.getServletContext().getRealPath("/");
		// 拼接文件路径（模板excel）
		String finalXlsxPath = path + "/web/model/modelHourAllNetWork.xlsx";
		File file = new File(finalXlsxPath);
		List<String> datatitle = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			// 获取excel头部信息
			datatitle = cn.huahai.tel.util.ReadExcel.getTitleArr(fis, file.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GenerateExcel.writeExcel(dataList, finalXlsxPath, datatitle, session,getSetValue(dataList.get(0),session));
	}
	@Override
	public byte[] generateDayNetWorkTableExcel(String tagName, HttpSession session) {
		// TODO Auto-generated method stub
		DayAllNetworkBase[] data = generateDayNetworkCellDataExcel(tagName);
		List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
		
		for (int i = 0; i < data.length; i++) {
			try {
				System.out.println(data[i]);
				dataList.add(GenerateExcel.bean2Map(data[i]));
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 获取服务器真实路径
		String path = session.getServletContext().getRealPath("/");
		// 拼接文件路径（模板excel）
		String finalXlsxPath = path + "/web/model/modelDayAllNetWork.xlsx";
		File file = new File(finalXlsxPath);
		List<String> datatitle = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			// 获取excel头部信息
			datatitle = cn.huahai.tel.util.ReadExcel.getTitleArr(fis, file.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GenerateExcel.writeExcel(dataList, finalXlsxPath, datatitle, session,getSetValue(dataList.get(0),session));
	}
}
