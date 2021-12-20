[#ftl/]
<html>
<head><title>User details</title></head>
<body>
[@control.form action="/user/full-form" type="multipart"]
  [@control.button name="cancel" disabled=false tabindex=1/]
  [@control.checkbox name="user.active" checked=false disabled=false readonly=false required=true tabindex=2/]
  [@control.checkbox_list headerL10n="pick" headerValue="" l10nExpr="name" items=roles name="roleIds" disabled=false readonly=false required=true tabindex=3 valueExpr="id"/]
  [@control.country_select name="user.addresses['home'].country" disabled=false includeBlank=false multiple=false readonly=false required=true size=3 tabindex=4/]
  [@control.file name="image" disabled=false readonly=false required=true size=3 tabindex=5/]
  [@control.hidden name="user.id"/]
  [@control.image name="maybe" disabled=false ismap=false tabindex=6/]
  [@control.month_select name="user.favoriteMonth" disabled=false multiple=false readonly=false required=true size=3 tabindex=7/]
  [@control.password name="user.password" disabled=false maxlength=16 readonly=false required=true size=10 tabindex=8/]
  [@control.radio_list headerL10n="pick" headerValue="" items=userTypes name="user.type" disabled=false readonly=false required=true tabindex=9/]
  [@control.reset name="reset" disabled=false tabindex=10/]
  [@control.select headerL10n="pick" headerValue="" items=ages name="user.age" disabled=false multiple=false readonly=false required=false size=3 tabindex=11/]
  [@control.state_select name="user.addresses['home'].state" disabled=false includeBlank=true multiple=false readonly=false required=false size=3 tabindex=12/]
  [@control.submit name="submit" disabled=false tabindex=13/]
  [@control.text name="user.name" disabled=false maxlength=24 readonly=false required=false size=10 tabindex=14/]
  [@control.textarea name="user.lifeStory" cols=40 disabled=false readonly=false required=false rows=10 tabindex=15/]
  [@control.year_select name="user.favoriteYear" disabled=false endYear=2090 mulitple=false numberOfYears=100 readonly=false required=false size=3 startYear=1900 tabindex=16/]
[/@control.form]
</body>
</html>