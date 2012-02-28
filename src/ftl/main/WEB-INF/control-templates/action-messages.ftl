[#ftl/]
[#if attributes['errors']]
  [#assign errors = action_errors/]
  [#assign class = "error"/]
[#else]
  [#assign errors = action_messages/]
  [#assign class = "message"/]
[/#if]
[#if errors?size > 0]
<ul class="action-${class}s">
  [#list errors as error]
  <li class="action-${class}">${error}</li>
  [/#list]
</ul>
[/#if]
