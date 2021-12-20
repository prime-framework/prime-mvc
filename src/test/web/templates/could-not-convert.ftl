[#ftl/]
[@control.form action="could-not-convert" method="POST"]
    [@control.text name="integerMap1['foo']" /]
    [@control.text name="integerMap1['bar']" /]
    [@control.text name="integerMap2['baz']" /]

    [@control.text name="integerMap1[foo2]" /]
    [@control.text name="integerMap1[bar2]" /]
    [@control.text name="integerMap2[baz2]" /]

    [@control.text name="integerList1[0]" /]
    [@control.text name="integerList1[1]" /]

    [@control.text name="integerList2[0]" /]
[/@control.form]