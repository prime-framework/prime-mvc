[#ftl/]
<html>
<head><title>Edit a user</title></head>
<body>
[@prime.form action="/user/edit" method="POST"]
  [@prime.text name="user.name"/]
  [@prime.text name="user.age"/]
  [@prime.text name="user.addresses['home'].street"/]
  [@prime.text name="user.addresses['home'].city"/]
  [@prime.text name="user.addresses['home'].state"/]
  [@prime.text name="user.addresses['home'].zipcode"/]
  [@prime.text name="user.addresses['home'].country"/]
  [@prime.text name="user.addresses['work'].street"/]
  [@prime.text name="user.addresses['work'].city"/]
  [@prime.text name="user.addresses['work'].state"/]
  [@prime.text name="user.addresses['work'].zipcode"/]
  [@prime.text name="user.addresses['work'].country"/]
[/@prime.form]
</body>
</html>