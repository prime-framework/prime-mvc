[#ftl/]
Look Ma, I'm redirected.
[@control.message key="[ERROR]"/]
[@control.message key="[INFO]"/]
[@control.message key="[WARNING]"/]

<form method="POST" action="temp-redirect-form">
  <input name="text" type="text" value="textValue"/>
  <input name="disabled" type="text" value="disabledValue" disabled/>
  <input name="hidden" type="hidden" value="hiddenValue"/>
  <input name="radio" type="radio" value="radioValue1"/>
  <input name="radio" type="radio" value="radioValue2" checked/>
  <input name="checkbox" type="checkbox" value="checkboxValue1"/>
  <input name="checkbox" type="checkbox" value="checkboxValue2" checked/>
  <textarea cols="64" rows="4" name="textarea">textareaValue</textarea>
</form>