[#ftl/]
<html>
<head><title>Edit a user</title></head>
<body>
[@jc.form action="/user/edit" method="POST"]
  [@jc.text name="user.name"/]
  [@jc.text name="user.age"/]
  [@jc.text name="user.addresses['home'].street"/]
  [@jc.text name="user.addresses['home'].city"/]
  [@jc.text name="user.addresses['home'].state"/]
  [@jc.text name="user.addresses['home'].zipcode"/]
  [@jc.text name="user.addresses['home'].country"/]
  [@jc.text name="user.addresses['work'].street"/]
  [@jc.text name="user.addresses['work'].city"/]
  [@jc.text name="user.addresses['work'].state"/]
  [@jc.text name="user.addresses['work'].zipcode"/]
  [@jc.text name="user.addresses['work'].country"/]
[/@jc.form]
</body>
</html>