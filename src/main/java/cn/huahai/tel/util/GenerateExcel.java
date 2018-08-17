package cn.huahai.tel.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import cn.huahai.tel.bean.CriticalValueBase;
import cn.huahai.tel.service.ISetValueServer;

/**
 * 生成excel
 * 
 * @author lizhuodong
 *
 */
@Component
public class GenerateExcel {
	private static final String EXCEL_XLS = "xls";
	private static final String EXCEL_XLSX = "xlsx";


	public static Boolean setCellColor(List<CriticalValueBase> setValue,
			String attribute, String data) {
		for (CriticalValueBase criticalValueBase : setValue) {
			if (criticalValueBase.getAttribute().toLowerCase().equals(attribute.toLowerCase())) {
				Boolean flag = criticalValueBase.getCompare() > Double.parseDouble(data);
				if (1==criticalValueBase.getSymbol()) {
					flag = !flag;
				}
				if (flag) {
					return true;
				}
				return false;
			}
		}
		return null;
	}

	/**
	 * 将数据写入excel
	 * 
	 * @param dataList
	 *            需要写入excel的数据
	 * @param modelPath
	 *            模板路径
	 * @param fields
	 *            写入字段
	 * @param session
	 *            session对象
	 * @param setValue
	 *            临界值
	 * @return excel的二进制数据
	 */
	public static byte[] writeExcel(List<Map<String, String>> dataList, String modelPath, List<String> fields,
			HttpSession session, List<CriticalValueBase> setValue) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			// 文件路径
			String finalXlsxPath = modelPath;
			// 读取Excel文档
			File finalXlsxFile = new File(finalXlsxPath);
			Workbook workBook = getWorkbok(finalXlsxFile);
			// sheet 对应一个工作页
			Sheet sheet = workBook.getSheetAt(0);
			/**
			 * 删除原有数据，除了属性列
			 */
			int rowNumber = sheet.getLastRowNum(); // 第一行从0开始算
			System.out.println("原始数据总行数，除属性列：" + rowNumber);
			for (int i = 1; i <= rowNumber; i++) {
				Row row = sheet.getRow(i);
				sheet.removeRow(row);
			}
			/**
			 * 往Excel中写新数据
			 */
			// 获取map中的字段
			// List<String> fields = new ArrayList<String>();
			// for (String key : dataList.get(0).keySet()) {
			// fields.add(key);
			// }
			// 生成单元格样式
			CellStyle styleRed = workBook.createCellStyle(); // 填充色 
			CellStyle styleBlue = workBook.createCellStyle();
			// 填充色
			styleBlue.setFillForegroundColor(HSSFColorPredefined.BLUE.getIndex());
			styleRed.setFillForegroundColor(HSSFColorPredefined.RED.getIndex());
			styleBlue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			styleRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			// 字体颜色
			Font font = workBook.createFont();
			font.setColor(IndexedColors.WHITE.getIndex());
			styleBlue.setFont(font);
			styleRed.setFont(font);
			System.out.println(setValue.get(0).getAttribute());
			for (int j = 0; j < dataList.size(); j++) {
				// 创建一行：从第二行开始，跳过属性列
				Row row = sheet.createRow(j + 1);
				// 得到要插入的每一条记录
				Map<String, String> dataMap = dataList.get(j);

				for (int i = 0; i < dataMap.size() - 1; i++) {
					Cell cell = row.createCell(i);
					String datatype = fields.get(i);
					String data = dataMap.get(datatype);
					Boolean flag = setCellColor(setValue, datatype, data);
					if (flag!= null) {
						if(flag) {
							cell.setCellStyle(styleRed);
						}else {
							cell.setCellStyle(styleBlue);
						}
					}
					cell.setCellValue(data);
				}
			}
			workBook.write(out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("数据导出成功");

		return out.toByteArray();
	}

	/**
	 * 根据文件流返回工作簿对象
	 * 
	 * @param file
	 *            文件对象
	 * @return 工作簿对象
	 * @throws IOException
	 *             读取excel发生异常
	 */
	public static Workbook getWorkbok(File file) throws IOException {
		Workbook wb = null;
		FileInputStream in = new FileInputStream(file);
		if (file.getName().endsWith(EXCEL_XLS)) { // Excel 2003
			wb = new HSSFWorkbook(in);
		} else if (file.getName().endsWith(EXCEL_XLSX)) { // Excel 2007/2010
			wb = new XSSFWorkbook(in);
		}
		return wb;
	}

	/**
	 * 转换bean为map
	 * 
	 * @param source
	 *            要转换的bean
	 * @return map数据
	 * @throws IllegalAccessException
	 *             转化异常
	 */
	public static <T> Map<String, String> bean2Map(T source) throws IllegalAccessException {
		Map<String, String> result = new HashMap<>();

		Class<?> sourceClass = source.getClass();
		// 拿到所有的字段,不包括继承的字段
		Field[] sourceFiled = sourceClass.getDeclaredFields();
		for (Field field : sourceFiled) {
			field.setAccessible(true);// 设置可访问,不然拿不到private
			// 配置了注解的话则使用注解名称,作为header字段
			FieldName fieldName = field.getAnnotation(FieldName.class);
			if (fieldName == null) {
				result.put(field.getName(), (String) field.get(source));
			} else {
				if (fieldName.Ignore())
					continue;
				result.put(fieldName.value(), (String) field.get(source));
			}
		}
		return result;
	}

	/**
	 * map转bean
	 * 
	 * @param source
	 *            map属性
	 * @param instance
	 *            要转换成的备案
	 * @return 该bean
	 */
	public static <T> T map2Bean(Map<String, Object> source, Class<T> instance) {
		try {
			T object = instance.newInstance();
			Field[] fields = object.getClass().getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				FieldName fieldName = field.getAnnotation(FieldName.class);
				if (fieldName != null) {
					field.set(object, source.get(fieldName.value()));
				} else {
					field.set(object, source.get(field.getName()));
				}
			}
			return object;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
