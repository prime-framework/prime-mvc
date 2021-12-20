[#ftl/]
${string!['__empty1__']?join(",")}
${strings!['__empty2__', '__empty3__']?join(",")}

[@control.form action="collection-converter" method="POST"]
 [@control.text name="string"/]
  [#list strings as s]
    [@control.text name="strings" value=s/]
  [/#list]
[/@control.form]