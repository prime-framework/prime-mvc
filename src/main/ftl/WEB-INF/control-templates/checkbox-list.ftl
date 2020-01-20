[#ftl/]
[#import "_macros.ftl" as macros/]
[@macros.dynamic_attributes attrs=dynamicAttributes name=attributes['name']/]
<div class="[@macros.class attrs=attributes name="checkbox-list" input=true/]">
<div class="label-container">[@macros.label text=label fieldMessages=fieldMessages required=required/]</div>
<div class="control-container">
[#list options?keys as key]
<div class="control-item-container">
<input type="checkbox"[#if options[key].selected] checked="checked"[/#if] value="${key}"[@macros.append_attributes attributes=attributes excludes=['id']/]/><span class="checkbox-text">${options[key].text}</span>
</div>
[/#list]
</div>
</div>
