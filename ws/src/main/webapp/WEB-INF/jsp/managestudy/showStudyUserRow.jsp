<%@ page contentType="text/html; charset=UTF-8" %>
 <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<c:set var="count" value="${param.eblRowCount}" />
<!-- row number: <c:out value="${count}"/> -->

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.UserAccountRow" />
 <c:choose>
   <c:when test="${currRow.bean.status.id ==1}">
   <tr valign="top">  
     <input type="hidden" name="id<c:out value="${count}"/>" value="<c:out value="${currRow.bean.id}"/>">  
     <input type="hidden" name="name<c:out value="${count}"/>" value="<c:out value="${currRow.bean.name}"/>">  
     <input type="hidden" name="lastName<c:out value="${count}"/>" value="<c:out value="${currRow.bean.lastName}"/>">  
     <input type="hidden" name="firstName<c:out value="${count}"/>" value="<c:out value="${currRow.bean.firstName}"/>">  
     <input type="hidden" name="email<c:out value="${count}"/>" value="<c:out value="${currRow.bean.email}"/>">      
      <td class="table_cell_left"><c:out value="${currRow.bean.name}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.firstName}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.lastName}"/></td>  
      <td class="table_cell">
       <c:set var="role1" value="${currRow.bean.activeStudyRole.id}"/>
       <c:if test="${not empty tmpSelectedUsersMap[currRow.bean.id]}">
           <c:set var="role1" value="${tmpSelectedUsersMap[currRow.bean.id]}"/>
       </c:if>

      <!-- The Div added for the Mantis Issue 2239 -->    
      <div class="formfieldM_BG_noMargin">
       <select name="activeStudyRoleId<c:out value="${count}"/>" class="formfieldM">
         <c:forEach var="userRole" items="${roles}">
          <c:choose>
           <c:when test="${role1 == userRole.id}">
             <option value="<c:out value="${userRole.id}"/>" selected><c:out value="${userRole.description}"/>
           </c:when>
           <c:otherwise>
             <option value="<c:out value="${userRole.id}"/>"><c:out value="${userRole.description}"/>
           </c:otherwise>
          </c:choose>
         </c:forEach>
       </select>
         </div>
      </td>
      <td class="table_cell"><input type="checkbox" <c:if test="${not empty tmpSelectedUsersMap[currRow.bean.id]}">checked="checked" </c:if>
                                    name="selected<c:out value="${count}"/>" value="yes"></td>
      <td class="table_cell"><c:out value="${currRow.bean.notes}"/>&nbsp;</td> 
      <c:set var="count" value="${count+1}"/>
     </tr>
     </c:when>
     <c:otherwise>
     <tr>
      <td class="table_cell"><c:out value="${currRow.bean.name}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.firstName}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.lastName}"/></td>  
      <td class="table_cell">
       <c:out value="${currRow.bean.activeStudyRole.description}"/>      
      </td>
      <td class="table_cell"><fmt:message key="assigned" bundle="${resword}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.notes}"/>&nbsp;</td>    
     </tr>
     </c:otherwise>
     </c:choose>
