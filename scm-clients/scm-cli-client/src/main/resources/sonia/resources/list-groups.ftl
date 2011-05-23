<#list groups as group>
Name:           ${group.name}
Type:           ${group.type}
Description:    ${group.description!""}
Creation-Date:  <#if group.creationDate??>${group.creationDate?string("yyyy-MM-dd HH:mm:ss")}</#if>
Last-Modified:  <#if group.lastModified??>${group.lastModified?string("yyyy-MM-dd HH:mm:ss")}</#if>
Members:
<#if group.members??>
<#list group.members as member>
  ${member}
</#list>
</#if>

</#list>