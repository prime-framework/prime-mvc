[#if errorMessages?size > 0]
Error count:${errorMessages?size}
[#list errorMessages as m]
${m}
[/#list]
[/#if]
Count:${(cookies![])?size}
[#list cookies![] as cookie]
${cookie.name}:${cookie.value}
[/#list]