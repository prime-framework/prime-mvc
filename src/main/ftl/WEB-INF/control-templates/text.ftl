[#ftl/]
[#import "_macros.ftl" as macros/]
[@macros.dynamic_attributes attrs=dynamicAttributes name=attributes['name']/]
<div class="[@macros.class attrs=attributes name="text" input=true/]">
<div class="label-container">[@macros.label text=label fieldMessages=fieldMessages required=required/]</div>
<div class="control-container"><input type="text"[@macros.append_attributes attributes=attributes excludes=[]/]/></div>
</div>
