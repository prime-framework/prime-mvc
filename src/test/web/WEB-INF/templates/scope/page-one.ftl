[#ftl/]
<html>
<head><title>Page One</title></head>
<body>
  [@control.form action="/scope/page-two" method="POST"]
    [@control.text name="searchText"/]
    [@control.text name="searchType"/]
    postParameterMethodCalled:${postParameterMethodCalled!"null"}
    formPrepareMethodCalled:${formPrepareMethodCalled!"null"}
  [/@control.form]
</body>
</html>