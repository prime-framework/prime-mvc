[#ftl/]
[#import "_macros.ftl" as macros/]
[@macros.dynamic_attributes attrs=dynamicAttributes name=attributes['name']/]
[#if actionURI??]
<input type="hidden" name="__a_${attributes['name']}" value="${actionURI?html}"/>
[/#if]
<div class="[@macros.class attrs=attributes name="submit-button" input=false/]">
<div class="label-container"> </div>
<div class="control-container"><input type="submit"[@macros.append_attributes attributes=attributes list=[]/]/></div>
</div>
