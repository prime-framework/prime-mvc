[#ftl/]
[#import "_macros.ftl" as macros/]
<div class="form">
<form[@macros.append_attributes attributes=attributes excludes=[]/]>
[#if csrfToken??]
<input type="hidden" name="primeCSRFToken" value="${csrfToken}"/>
[/#if]
