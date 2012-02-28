[#ftl/]
[#include "macros.ftl"/]
[#include "dynamic-attributes.ftl"/]
<div class="[@class attributes=attributes name="textarea" input=true/]">
<div class="label-container">[#include "label.ftl"/]</div>
<div class="control-container"><textarea[@append_attributes attributes=attributes list=['value']/]>${(attributes['value']!'')?html}</textarea></div>
</div>
