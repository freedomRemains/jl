<%@ page pageEncoding="UTF-8"%>

          <% if ("1000601".equals(columnMap.get("MHTMLPARTS_ID"))
                 && authUtil.hasAuth("1000601", authList)) { // レコード一覧領域 %>
            <%@ include file="common/20070_commonRecordList.jsp"%>
          <% } %>
