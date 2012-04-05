[#ftl/]
[#import "_macros.ftl" as macros/]
[@macros.dynamic_attributes attrs=dynamicAttributes name=attributes['name']/]
<div class="[@macros.class attrs=attributes name="select" input=true/]">
<div class="label-container">[@macros.label text=label fieldMessages=fieldMessages required=required/]</div>
<div class="control-container">
<select[@macros.append_attributes attributes=attributes excludes=[]/]>
[#list options?keys as key]
<option value="${key?html}"[#if options[key].selected] selected="selected"[/#if]>${options[key].text}</option>
[/#list]
</select>
</div>
</div>
