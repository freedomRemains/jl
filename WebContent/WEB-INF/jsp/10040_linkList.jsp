<%@ page pageEncoding="UTF-8"%>

          <% if ("1000301".equals(columnMap.get("MHTMLPARTS_ID"))
                 && authUtil.hasEditAuth("1000301", authList)) { // リンク一覧領域 %>
            <%@ include file="common/20040_commonLinkList.jsp"%>
          <% } %>
