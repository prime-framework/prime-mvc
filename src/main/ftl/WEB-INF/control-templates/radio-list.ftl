[#ftl/]
[#import "_macros.ftl" as macros/]
[@macros.dynamic_attributes attrs=dynamicAttributes name=attributes['name']/]
<div class="[@macros.class attrs=attributes name="radio-list" input=true/]">
<div class="label-container">[@macros.label text=label fieldMessages=fieldMessages required=required/]</div>
<div class="control-container">
[#list options?keys as key]
<div class="control-item-container">
<input type="radio"[#if options[key].selected] checked="checked"[/#if] value="${key?html}"[@macros.append_attributes attributes=attributes excludes=['id']/]/><span class="radio-text">${options[key].text}</span>
</div>
[/#list]
</div>
</div>
<input type="hidden" name="__rb_${attributes['name']}" value="${uncheckedValue?html}"/>
