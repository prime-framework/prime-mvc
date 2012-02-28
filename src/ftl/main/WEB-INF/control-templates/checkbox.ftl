[#ftl/]
[#include "macros.ftl"/]
[#include "dynamic-attributes.ftl"/]
<div class="[@class attributes=attributes name="checkbox" input=true/]">
<div class="label-container">[#include "label.ftl"/]</div>
<div class="control-container"><input type="checkbox"[@append_attributes attributes=attributes list=[]/]/><input type="hidden" name="__jc_cb_${attributes['name']}" value="${uncheckedValue?html}"/></div>
</div>
