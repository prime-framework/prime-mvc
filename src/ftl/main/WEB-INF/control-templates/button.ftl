[#ftl/]
[#import "_macros.ftl" as macros/]
[@macros.dynamic_attributes attrs=dynamicAttributes name=attributes['name']/]
<input type="hidden" name="__a_${attributes['name']}" value="${actionURI!''}"/>
<div class="[@macros.class attrs=attributes name="button-button" input=false/]">
<div class="label-container"> </div>
<div class="control-container"><input type="button"[@macros.append_attributes attributes=attributes list=[]/]/></div>
</div>
