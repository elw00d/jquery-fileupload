<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="utf-8" session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="baseUrl" value="${pageContext.request.contextPath}"/>

<script type="text/javascript" src="${baseUrl}/js/jquery-1.7.2.js"></script>
<script type="text/javascript" src="${baseUrl}/js/jquery.fileupload.js"></script>
<script type="text/javascript" src="${baseUrl}/js/jquery.fileupload-ru.js"></script>

<html>
    <body>
        <h2>Hello World!</h2>

        <form id="form1" enctype="multipart/form-data" method="post">
            <input type="file" name="file1"/>
            <input type="file" name="file2"/>
            <input type="submit"/>
        </form>

        <form id="form2" enctype="multipart/form-data" method="post">
            <input type="file" name="file1"/>
            <input type="submit"/>
        </form>

    </body>
</html>


<script type="text/javascript">
    $(function() {
        $('#form1').fileupload({
                    actionUrl: '${baseUrl}/upload/{uploaderId}:{timestamp}',
                    uploaderId: null,
                    uploadProgressUrl : '${baseUrl}/upload-progress?id={uploaderId}'/*,
                    onStarted: function() {
                        alert('onstarted');
                    },
                    onFinished: function() {
                        alert('onfinished');
                    },
                    onProgress: function() {
                        console.log('onprogress called');
                    },
                    invokeDefaults: false*/
                });
         $('#form2').fileupload({
                    actionUrl: '${baseUrl}/upload/{uploaderId}:{timestamp}',
                    uploaderId: 1,
                    uploadProgressUrl : '${baseUrl}/upload-progress?id={uploaderId}'
                });
    });
</script>