[#ftl/]
[#import "_macros.ftl" as macros/]
[@macros.dynamic_attributes attrs=dynamicAttributes name=attributes['name']/]
<div class="[@macros.class attrs=attributes name="checkbox" input=true/]">
<div class="label-container">[@macros.label text=label fieldMessages=fieldMessages required=required/]</div>
<div class="control-container"><input type="checkbox"[@macros.append_attributes attributes=attributes excludes=[]/]/><input type="hidden" name="__cb_${attributes['name']}" value="${uncheckedValue}"/></div>
</div>
