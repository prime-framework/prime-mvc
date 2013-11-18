[#ftl/]
[#-- @ftlvariable name="action" type="org.example.action.Api.ActionType" --]
[#-- @ftlvariable name="user" type="org.example.domain.UserField" --]
{
  "user": {
    "active": ${user.active?string},
    "age": 37,
    "addresses": {
      [#list user.addresses?keys?sort as type]
      "${type}": {
        "city": "${user.addresses[type].city}",
        "state": "${user.addresses[type].state}",
        "zipcode": "${user.addresses[type].zipcode}"
      }[#if type_has_next],[/#if]
      [/#list]
    },
    "favoriteMonth": ${user.favoriteMonth?c},
    "favoriteYear": ${user.favoriteYear?c},
    "ids": {
      [#list user.ids?keys as key]
      "${key}": ${(user.ids(key))?string}[#if key_has_next],[/#if]
      [/#list]
    },
    "lifeStory": "${user.lifeStory}",
    "securityQuestions": [[#list user.securityQuestions as q]"${q}"[#if q_has_next], [/#if][/#list]],
    "siblings": [
      [#list user.siblings as sibling]
      {
        "name": "${sibling.name}"
      }[#if sibling_has_next],[/#if]
      [/#list]
    ],
    "type": "${user.type}"
  },
  "action": "${action?string}"
}