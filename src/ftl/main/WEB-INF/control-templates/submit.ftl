[#ftl/]
[#include "macros.ftl"/]
[#include "dynamic-attributes.ftl"/]
<input type="hidden" name="__a_${attributes['name']}" value="${(actionURI!'')?html}"/>
<div class="[@class attributes=attributes name="submit-button" input=false/]">
<div class="label-container"> </div>
<div class="control-container"><input type="submit"[@append_attributes attributes=attributes list=[]/]/></div>
</div>
