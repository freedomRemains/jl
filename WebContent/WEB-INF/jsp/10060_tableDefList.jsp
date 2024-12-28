<%@ page pageEncoding="UTF-8"%>

          <% if ("1000501".equals(columnMap.get("MHTMLPARTS_ID"))
                 && authUtil.hasEditAuth("1000501", authList)) { // テーブル情報入力領域 %>
            <div class="<%=columnMap.get("MHTMLPARTS_ID")%>">
              <div class="partsArea">
                <% var tableDefList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("tableDefList");
                   var tableName = tableDefList.get(0).get("TABLE_NAME"); %>
                <div class="littlePadding">
                  <label><span class="genericLabel"><%=tableDefList.get(0).get("TABLE_LOGICAL_NAME") + "(" + tableDefList.get(0).get("TABLE_NAME") + ")"%></span><br /></label>
                </div>
                <table class="dbColumnTable">
                  <thead>
                    <tr>
                      <th>DB項目論理名</th>
                      <th>DB項目物理名</th>
                      <th>型</th>
                    </tr>
                  </thead>
                  <tbody>
                    <% var columnList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("columnList");
                       for (LinkedHashMap<String, String> column : columnList) {
                         String fieldName = column.get("FIELD_NAME");
                         String fieldLogicalName = column.get("FIELD_LOGICAL_NAME");
                         String typeName = column.get("TYPE_NAME"); %>
                      <tr>
                        <td><%=fieldLogicalName%></td>
                        <td><%=fieldName%></td>
                        <td><%=typeName%></td>
                      </tr>
                    <% } %>
                  </tbody>
                </table>
              </div>
            </div>
            <input type="hidden" name="tableName" value="<%=tableName%>">
          <% } %>
