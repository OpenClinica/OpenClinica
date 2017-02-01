<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<form action="/pages/schema/tenant1">
    <input type="submit" value="tenant1" method="get">
</form>
<BR>
<form action="/pages/schema/tenant2" method="get">
    <input type="submit" value="tenant2">
</form>
<BR>
<form action="/pages/schema/tenant3"  method="get">
    <input type="submit" value="tenant3">
</form>

<form action="/pages/schema/post"  method="post">
    <input type="text" name="studyOID">
    <input type="submit" value="Study OID">
</form>

