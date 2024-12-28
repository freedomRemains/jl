<%@ page pageEncoding="UTF-8"%>

          <% if ("1000801".equals(columnMap.get("MHTMLPARTS_ID"))
                 && authUtil.hasEditAuth("1000801", authList)) { // レコード詳細領域 %>
            <div class="<%=columnMap.get("MHTMLPARTS_ID")%>">
              <div class="partsArea">
                <% var tableDefListEdit = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableDefList"); %>
                <label><span class="genericLabel"><%=tableDefListEdit.get(0).get("TABLE_LOGICAL_NAME") + "(" + tableDefListEdit.get(0).get("TABLE_NAME") + ")"%></span><br /></label>
                <table>
                  <tbody>
                    <% var columnList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("columnList");
                       var targetRecordEdit = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("targetRecord");
                       var tableNameEdit = tableDefListEdit.get(0).get("TABLE_NAME");
                       if (columnList != null && columnList.size() > 0) { // 新規レコード追加の場合
                         for (LinkedHashMap<String, String> column : columnList) {
                           if (column.get("FIELD_NAME").equals(tableNameEdit + "_ID")
                               || "VERSION".equals(column.get("FIELD_NAME")) || "DEL_FLG".equals(column.get("FIELD_NAME"))
                               || "CREATE_USER".equals(column.get("FIELD_NAME")) || "CREATE_DATE".equals(column.get("FIELD_NAME"))
                               || "UPDATE_USER".equals(column.get("FIELD_NAME")) || "UPDATE_DATE".equals(column.get("FIELD_NAME"))) {
                             continue;
                           }
                           String fieldName = column.get("FIELD_NAME");
                           String fieldLogicalName = column.get("FIELD_LOGICAL_NAME"); %>
                      <tr>
                        <td><%=fieldLogicalName%></td>
                        <td><input id="<%=fieldName%>" name="<%=fieldName%>" size="50"></td>
                      </tr>
                    <%   } %>
                    <% } else { // 削除もしくは編集の場合
                         for (Map.Entry<String, String> entry : targetRecordEdit.get(0).entrySet()) {
                           String disabled = "";
                           if (entry.getKey().equals(tableNameEdit + "_ID")
                               || "VERSION".equals(entry.getKey()) || "DEL_FLG".equals(entry.getKey())
                               || "CREATE_USER".equals(entry.getKey()) || "CREATE_DATE".equals(entry.getKey())
                               || "UPDATE_USER".equals(entry.getKey()) || "UPDATE_DATE".equals(entry.getKey())) {
                             disabled = "disabled";
                           }
                           String fieldName = entry.getKey();
                           String fieldLogicalName = "";
                           for (LinkedHashMap<String, String> tableDef : tableDefListEdit) {
                             if (fieldName.equals(tableDef.get("FIELD_NAME"))) {
                               fieldLogicalName = tableDef.get("FIELD_LOGICAL_NAME");
                             }
                           } %>
                      <tr>
                        <td><%=fieldLogicalName%></td>
                        <td><input id="<%=fieldName%>" name="<%=fieldName%>" value="<%=entry.getValue()%>" size="50" <%=disabled%>></td>
                      </tr>
                    <%   } %>
                    <% } %>
                  </tbody>
                </table>
              </div>
            </div>
            <% if (columnList == null || columnList.size() == 0) { // 削除もしくは編集の場合
                 for (Map.Entry<String, String> entry : targetRecordEdit.get(0).entrySet()) {
                   if (entry.getKey().equals(tableNameEdit + "_ID")
                       || "VERSION".equals(entry.getKey()) || "DEL_FLG".equals(entry.getKey())
                       || "CREATE_USER".equals(entry.getKey()) || "CREATE_DATE".equals(entry.getKey())
                       || "UPDATE_USER".equals(entry.getKey()) || "UPDATE_DATE".equals(entry.getKey())) {
                     String fieldName = entry.getKey();
                     String fieldValue = entry.getValue(); %>
              <input type="hidden" name="<%=fieldName%>" value="<%=fieldValue%>">
            <%     } %>
            <%   } %>
            <% } %>
            <input type="hidden" name="tableName" value="<%=tableNameEdit%>">
          <% } %>
