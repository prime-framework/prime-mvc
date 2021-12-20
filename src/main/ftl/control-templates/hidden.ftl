[#ftl/]
[#import "_macros.ftl" as macros/]
[@macros.dynamic_attributes attrs=dynamicAttributes name=attributes['name']/]
<input type="hidden"[@macros.append_attributes attributes=attributes excludes=[]/]/>