<html lang="en_US">
<body>
<div>
  Yo, nice template.
    [#--
          Use case: We have a template that:
          1. Is rendered directly from a HTTP GET (/freemarker/missing-message-template), without an action class
          2. Specifies a message key that does not exist in the action URI search path.
          3. Tries to specify a default message in case the message key does not exist.
    --]
  <span>[@control.message key="testing" default="the default message"/]</span>
</div>
</body>
</html>
