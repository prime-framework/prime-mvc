<html>
<body>
<div>
  <h1>Freemarker escape enabled</h1>
  <p>Output format: ${.output_format}</p>
  <p>Auto-escaping: ${.auto_esc?c}</p>

    [#-- This needed its own page so that it explodes all by its lonesome --]
  <h1>User messages</h1>
    ${selectionRequired?html}
    ${listSeparator?html}
    ${warning?html}
    ${welcome?html}

</div>
</body>
</html>
