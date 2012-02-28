[#ftl/]
[#include "macros.ftl"/]
[#include "dynamic-attributes.ftl"/]
<div class="[@class attributes=attributes name="password" input=true/]">
<div class="label-container">[#include "label.ftl"/]</div>
<div class="control-container"><input type="password"[@append_attributes attributes=attributes list=[]/]/></div>
</div>
