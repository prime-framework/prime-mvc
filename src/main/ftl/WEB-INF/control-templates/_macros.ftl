[#ftl/]

[#macro class attrs name input]
  [#if attrs['class']??]${attrs['class']}-${name} ${attrs['class']}-[#if input]input[#else]button[/#if] ${attrs['class']}-control [/#if]${name} [#if input]input[#else]button[/#if] control[#t/]
[/#macro]

[#macro append_attributes attributes excludes]
  [#list attributes?keys as key]
    [#if !excludes?seq_contains(key)]
 ${key}="${attributes(key)?string}"[#rt/]
    [/#if]
  [/#list]
[/#macro]

[#macro dynamic_attributes attrs name]
  [#list attrs?keys as key]
<input type="hidden" name="${name}@${key}" value="${attrs[key]}"/>
  [/#list]
[/#macro]

[#macro label text fieldMessages required=false]
  [#assign hasFieldErrors = fieldMessages?size > 0/]
  <label for="${attributes['id']}" class="label">[#t/]
    [#if hasFieldErrors]<span class="error">[/#if][#t/]
    ${text}[#t/]
    [#if hasFieldErrors] ([#list fieldMessages as message]${message?string}[#if message_has_next], [/#if][/#list])[/#if][#t/]
    [#if hasFieldErrors]</span>[/#if][#t/]
    [#if required]<span class="required">*</span>[/#if][#t/]
  </label>[#t/]
[/#macro]