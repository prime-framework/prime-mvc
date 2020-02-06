<html>
<body>
<div>
  <h1>Freemarker escape enabled</h1>
  <p>Output format: ${.output_format}</p>
  <p>Auto-escaping: ${.auto_esc?c}</p>
    [#switch mode]
        [#case "message"]
        [#-- We expect these to be unescaped --]
          <h1>Properties file based messages (using control)</h1>
            [@control.message key="selection-required"/]
            [@control.message key="listSeparator"/]
            [@control.message key="warning"/]
            [@control.message key="welcome"/]

            [#break/]

        [#case "function"]
        [#-- We expect these to be unescaped --]
          <h1>Properties file based messages (using function call)</h1>
            ${function.message("selection-required")}
            ${function.message("listSeparator")}
            ${function.message("warning")}
            ${function.message("welcome")}

            [#break/]

        [#case "functionUnescaped"]
        [#-- We expect these to be unescaped --]
          <h1>Properties file based messages (using function call)</h1>
            [#noautoesc]
                ${function.message("selection-required")}
                ${function.message("listSeparator")}
                ${function.message("warning")}
                ${function.message("welcome")}
            [/#noautoesc]

            [#break/]

        [#case "directProperties"]
        [#-- We expect these to be escaped --]
          <h1>User messages</h1>
            ${selectionRequired}
            ${listSeparator}
            ${warning}
            ${welcome}

            [#break/]

        [#case "indirectProperties"]
        [#-- We expect these to be escaped --]
        [#-- This happens in our properties.display macro/function and effectively boils down to the following --]
          <h1>User messages 2</h1>
            ${"selectionRequired"?eval}
            ${"listSeparator"?eval}
            ${"warning"?eval}
            ${"welcome"?eval}

            [#break/]

    [/#switch]

    [#-- An example taken from CleanSpeak where the hash strings are getting converted to TemplateHTMLOutput objects --]
    [@control.radio_list name="listTest" items={"dismissed": function.message("dismiss"), "none": function.message("ignore")}/]
    [@control.checkbox_list name="listTest2" items={"dismissed": function.message("dismiss"), "none": function.message("ignore")}/]

</div>
</body>
</html>
