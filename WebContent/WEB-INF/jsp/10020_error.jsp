<%@ page pageEncoding="UTF-8"%>

          <% if ("1000101".equals(columnMap.get("MHTMLPARTS_ID"))
                 && authUtil.hasReadAuth("1000101", authList)) { // エラー表示領域
               String errMsg = (String) request.getAttribute("errMsg");
               String stackTrace = (String) request.getAttribute("stackTrace");
               var errPageMsg = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("errPageMsg"); %>
            <div><h3><span><%=errPageMsg.get(0).get("GNR_VAL")%></span></h3></div>
            <div><span><pre><%=stackTrace%></pre></span></div>
          <% } %>
