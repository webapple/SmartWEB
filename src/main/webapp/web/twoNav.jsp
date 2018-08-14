<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<div class="row twoNav">
	<ul class="nav nav-tabs">
	  <li role="presentation" class="active twoNav_a">
	  	<a href="${pageContext.request.contextPath}/main/showIndex.do?two=0">
	  	  15分钟 
	  	</a>
  	  </li>
	  <li role="presentation" class="keyPoint15 twoNav_a">
	  	<a href="${pageContext.request.contextPath}/main/showIndex.do?two=1">
	  	  <!-- 15分钟重点 -->
	  	  小时
	  	</a>
	  </li>
	  <li role="presentation" class="twoNav_a keyPointHour">
	  	<a href="${pageContext.request.contextPath}/main/showIndex.do?two=2">
	  		<!-- 小时 -->
	  		天
	  	</a>
	  </li>
	 <%--  <li role="presentation">
	  	<a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false">
      	<span class="caret"></span>
   		</a>
	  </li>
	  <li role="presentation"  class="twoNav_a">
	  	<a href="${pageContext.request.contextPath}/main/showIndex.do?two=3">天</a>
  	  </li> --%>
	  <p class="showBtn">
		<span class="glyphicon glyphicon-menu-down showmoreIcon" id="showmore"></span>
		<span class="showmoreText">显示更多</span>
	  </p>
	  <p class = "selectRange">
	  	<select class="form-control" id = "selectRange">
		</select>
	  </p>
	</ul>
</div>