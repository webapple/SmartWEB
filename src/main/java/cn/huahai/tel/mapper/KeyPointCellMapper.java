package cn.huahai.tel.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import cn.huahai.tel.bean.TopCellworkBase;

/**
 * 重点小区设置相关
 * @author lizhuodong
 *
 */
public interface KeyPointCellMapper {
	
	/**
	 * 根据tagname查找tablename
	 * @param tagname 组名称
	 * @return 表名称
	 */
	String selectTablenameByTagname(@Param("tagname") String tagname);
	/**
	 * 根据tagname 删除对应关系
	 * @param tagname 组名称
	 */
	void deleteMapping(@Param("tagname") String tagname);
	/**
	 * 插入表名称 和 组名称 的对应关系
	 * @param tablename 表名称
	 * @param tagname 组名称
	 */
	void insertKeyPointCell(@Param("tablename") String tablename,@Param("tagname") String tagname);
	/**
	 * 查询所有的组名称
	 * @return 组名称集合
	 */
	List<String> selectAllMapping();
	/**
	 * 创建重点小区表
	 * @param tablename 表名称
	 */
	void createKeyPointCell(@Param("tablename") String tablename);
	/**
	 * 删除重点小区表
	 * @param tablename 表名称
	 */
	void deletekeypointTable(@Param("tablename") String tablename);
	/**
	 * 插入小区数据
	 * @param tablename  表名称
	 * @param cellList 小区id集合
	 */
	void insertKeyPointCellName(@Param("tablename") String tablename,
			@Param("cellList") List<String> cellList);
	/**
	 * 查询tagname是否存在（重点小区组名称）
	 * @param tagname  tag名称
	 * @return 组的数量
	 */
	Integer selectTagName(@Param("tagname") String tagname);
	/**
	 * 根据范围查找top小区信息
	 * @param cellInfoTablename 全部小区信息
	 * @param regTablename 查找范围
	 * @return 规定范围内的小区top信息
	 */
	TopCellworkBase[] selectKeyPontCellInfo(@Param("cellInfoTablename") String cellInfoTablename,
			@Param("regTablename") String regTablename);
	
	/**
	 * 查询VIP小区信息
	 * @param tablename  表名称
	 * @return vip小区信息
	 */
	TopCellworkBase[] selectVIPCellInfo(@Param("tablename") String tablename);
	
	
}
