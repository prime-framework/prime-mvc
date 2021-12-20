[#ftl/]
<html>
<head><title>Edit a user</title></head>
<body>
[@control.form action="/user/edit" method="POST"]
  [@control.text name="user.name"/]
  [@control.text name="user.age"/]
  [@control.text name="user.addresses['home'].street"/]
  [@control.text name="user.addresses['home'].city"/]
  [@control.text name="user.addresses['home'].state"/]
  [@control.text name="user.addresses['home'].zipcode"/]
  [@control.text name="user.addresses['home'].country"/]
  [@control.text name="user.addresses['work'].street"/]
  [@control.text name="user.addresses['work'].city"/]
  [@control.text name="user.addresses['work'].state"/]
  [@control.text name="user.addresses['work'].zipcode"/]
  [@control.text name="user.addresses['work'].country"/]
[/@control.form]
</body>
</html>