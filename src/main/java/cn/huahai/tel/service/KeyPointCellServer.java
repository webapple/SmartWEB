package cn.huahai.tel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.huahai.tel.bean.TopCellworkBase;
import cn.huahai.tel.mapper.KeyPointCellMapper;
import cn.huahai.tel.util.Pinyin;

@Service
public class KeyPointCellServer implements IkeyPointCellServer {
@Resource
KeyPointCellMapper kpcm;
	/**
	 * 查看组名称是否存在
	 */
	public boolean selectTagNameExists(String tagname) {
		return kpcm.selectTagName(tagname) == 0;
	}
	public void insertKeyPointCell(String tablename, String tagname) {
		kpcm.insertKeyPointCell(tablename, tagname);
		
	}
	public void createKeyPointCell(String tablename) {
		kpcm.createKeyPointCell(tablename);
		
	}
	public void insertKeyPointCellName(String tablename, List<String> cellList) {
		kpcm.insertKeyPointCellName(tablename, cellList);
	}
	public List<String> selectAllMapping() {
		return kpcm.selectAllMapping();
	}
	public void deleteTagname(String tagname) {
		kpcm.deleteMapping(tagname);
		kpcm.deletekeypointTable(Pinyin.getPinYinHeadChar("YM_BZH_"+tagname));
	}
	public void addCellMapping(String tablename, String tagname, List<String> cellList) {
		kpcm.insertKeyPointCell(tablename, tagname);
		kpcm.createKeyPointCell(tablename);
		kpcm.insertKeyPointCellName(tablename, cellList);
	}
	public TopCellworkBase[] selectKeyPontCellInfo(String cellInfoTablename, String regTablename) {
		return kpcm.selectKeyPontCellInfo(cellInfoTablename, regTablename);
	}
	public TopCellworkBase[] selectVipCellInfo(String tablename) {
		return kpcm.selectVIPCellInfo(tablename);
	}

}
