[#ftl/]
<html>
<head><title>User details</title></head>
<body>
Name: ${user.name}
Age: ${user.age}
Security questions:
[#list user.securityQuestions as question]
  ${question}
[/#list]
Street: ${user.addresses['home'].street}
City: ${user.addresses['home'].city}
State: ${user.addresses['home'].state}
Zipcode: ${user.addresses['home'].zipcode}
</body>
</html>