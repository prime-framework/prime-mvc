[#ftl/]
[#include "macros.ftl"/]
[#include "dynamic-attributes.ftl"/]
<div class="[@class attributes=attributes name="select" input=true/]">
<div class="label-container">[#include "label.ftl"/]</div>
<div class="control-container">
<select[@append_attributes attributes=attributes list=[]/]>
[#list options?keys as key]
<option value="${key?html}"[#if options[key].selected] selected="selected"[/#if]>${options[key].text}</option>
[/#list]
</select>
</div>
</div>
