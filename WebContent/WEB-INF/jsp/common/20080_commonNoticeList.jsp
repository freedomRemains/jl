<%@ page pageEncoding="UTF-8"%>

            <div class="<%=columnMap.get("MHTMLPARTS_ID")%>">
              <div class="partsArea">
                <% var noticeList = (ArrayList<LinkedHashMap<String, String>>) request.getAttribute("noticeList");
                   if (noticeList != null && noticeList.size() > 0) {
                     for (LinkedHashMap<String, String> notice : noticeList) { %>
                  <div><h3><span><%=notice.get("GNR_VAL")%></span></h3></div>
                <%   } %>
                <% } %>
              </div>
            </div>
