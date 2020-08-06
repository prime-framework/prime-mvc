Count:${cookies?size}
[#list cookies as cookie]
${cookie.name}:${cookie.value}
[/#list]