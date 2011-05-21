<#list groups as group>
Name:           ${group.name}
Type:           ${group.type}
Description:    ${group.description!""}
Creation-Date:  ${group.creationDate!""?string}
Last-Modified:  ${group.lastModified!""?string}
Members:
<#if group.members??>
<#list group.members as member>
  ${member}
</#list>
</#if>

</#list>