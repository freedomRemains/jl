<%@ page pageEncoding="UTF-8"%>

          <% if ("1000901".equals(columnMap.get("MHTMLPARTS_ID"))
                 && authUtil.hasReadAuth("1000901", authList)) { // レコード詳細領域(参照) %>
            <%@ include file="common/20100_commonRecordRef.jsp"%>
          <% } %>
