<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<div class = "setPage row">
	<div class="set_pageMain">
		<div class="row" style="margin: 0">
			<ul class="nav nav-tabs" id="myTab">
			  <li class="active"><a href="#cellTable">小区表设置</a></li>
			  <li><a href="#cellGroupTable">小区组设置</a></li>
			  <li><a href="#topCellValueTable">top小区临界值设置</a></li>
			</ul>
			<div class="tab-content">
			  <div class="tab-pane" id="cellGroupTable">
			  <!-- 小区组导入页面 -->
			  	 <div class="col-md-12 col-lg-3">
					<a href="javascript:;" class="upload">
						点击选择excel文件
					    <input id="excel-file" class="change" type="file"
					    accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel" />
					</a>
					<input  type="text"  class="tagName" placeholder="请输入小区组名称(22个字符以内)">
					<button class="btn submit" >提交小区组数据</button>
					<div class="excelResult"></div>
					<table id="mappingTable" class="col-lg-6 table table-hover table-bordered table-striped table-condensed">
						<thead>
							<tr><td>小区组名称</td><td>操作</td></tr>
						</thead>
						<tbody></tbody>
					</table>
				  </div>
				  <div class="col-md-12 col-lg-9">
					  <div class = "excelTableContainer">
					  	<table id="excelTable" class="col-lg-6 table table-hover table-bordered table-striped table-condensed">
							<thead></thead>
							<tbody></tbody>
						</table>
					  </div>
				  </div>
			  </div>
			  <div class="tab-pane active" id="cellTable">
			  		<!-- 小区表导入页面 -->
					<a href="javascript:;" class="upload">
						点击选择excel文件，更新小区表
					    <input id="cell_file" class="change" type="file"
					    accept="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel" />
					</a>
			  </div>
			  <div class = "tab-pane" id = "topCellValueTable">
			  	<h5>&nbsp;&nbsp;整体指标参数设置</h5>
			  	<div class="criticalAllNetTable clearfix">
			  		<table class="table table-hover table-bordered table-striped table-condensed">
			  			<thead>
			  				<tr>
			  					<th>属性</th>
			  					<th>临界值</th>
			  					<th>符号</th>
			  				</tr>
			  				
			  			</thead>
			  			<tbody>
			  				
			  			</tbody>
			  		</table>
			  		<button id="updateAllNetBtn" type="button" class="pull-right btn btn-info">添加或删除属性</button>
			  		<button id="allNetBtn" type="button" class="pull-right btn btn-info">保存更改</button>
			  	</div>
			  	<h5>&nbsp;&nbsp;Top小区指标参数设置</h5>
			  	<div class="criticalTopCellTable clearfix">
			  		<table class="table table-hover table-bordered table-striped table-condensed">
			  			<thead>
			  				<tr>
			  					<th>属性</th>
			  					<th>临界值</th>
			  					<th>符号</th>
			  				</tr>
			  				
			  			</thead>
			  			<tbody>
			  				
			  			</tbody>
			  		</table>
			  		<button id="updateTopCellBtn" type="button" class="pull-right btn btn-info">添加或删除属性</button>
			  		<button id="topCellBtn" type="button" class="pull-right btn btn-info">保存更改</button>
			  	</div>
			  </div>
			</div>
		</div>
	</div>
</div>