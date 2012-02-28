[#ftl/]
[#assign errors = field_messages/]
[#if attributes['errors']]
  [#assign errors = field_errors/]
[/#if]
[#if errors?keys?size > 0]
<ul class="field-errors">
  [#list errors?keys as key]
    [#list errors[key] as error]
  <li class="field-error">${error}</li>
    [/#list]
  [/#list]
</ul>
[/#if]
