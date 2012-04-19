ID:             ${repository.id}
Name:           ${repository.name}
Type:           ${repository.type}
E-Mail:         ${repository.contact!""}
Description:    ${repository.description!""}
Public:         ${repository.publicReadable?string}
Archived:       ${repository.archived?string}
Creation-Date:  <#if repository.creationDate??>${repository.creationDate?string("yyyy-MM-dd HH:mm:ss")}</#if>
Last-Modified:  <#if repository.lastModified??>${repository.lastModified?string("yyyy-MM-dd HH:mm:ss")}</#if>
URL:            ${repository.url}
Permissions:
<#if repository.permissions??>
<#list repository.permissions as permission>
  ${permission.type} - ${permission.name} (Group: ${permission.groupPermission?string})
</#list>
</#if>
