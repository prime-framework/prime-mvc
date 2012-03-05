[#ftl/]
[#include "macros.ftl"/]
[#include "dynamic-attributes.ftl"/]
<input type="hidden" name="__a_${attributes['name']}" value="${(actionURI!'')?html}"/>
<div class="[@class attributes=attributes name="image-button" input=false/]">
<div class="label-container"> </div>
<div class="control-container"><input type="image"[@append_attributes attributes=attributes list=[]/]/></div>
</div>
