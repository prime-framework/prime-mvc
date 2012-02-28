[#ftl/]
[#include "macros.ftl"/]
[#include "dynamic-attributes.ftl"/]
<div class="[@class attributes=attributes name="radio-list" input=true/]">
<div class="label-container">[#include "label.ftl"/]</div>
<div class="control-container">
[#list options?keys as key]
<div class="control-item-container">
<input type="radio"[#if options[key].selected] checked="checked"[/#if] value="${key?html}"[@append_attributes attributes=attributes list=['id']/]/><span class="radio-text">${options[key].text}</span>
</div>
[/#list]
</div>
</div>
<input type="hidden" name="__jc_rb_${attributes['name']}" value="${uncheckedValue?html}"/>
