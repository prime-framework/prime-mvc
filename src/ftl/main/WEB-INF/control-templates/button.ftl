[#ftl/]
[#include "macros.ftl"/]
[#include "dynamic-attributes.ftl"/]
<input type="hidden" name="__jc_a_${attributes['name']}" value="${actionURI!''}"/>
<div class="[@class attributes=attributes name="button-button" input=false/]">
<div class="label-container"> </div>
<div class="control-container"><input type="button"[@append_attributes attributes=attributes list=[]/]/></div>
</div>
