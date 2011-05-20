<#list repositories as repository>
ID:             ${repository.id}
Name:           ${repository.name}
Type:           ${repository.type}
E-Mail:         ${repository.contact}
Description:    ${repository.description}
Public:         ${repository.publicReadable?string}
Creation-Date:  ${repository.creationDate!""?string}
Last-Modified:  ${repository.lastModified!""?string}
URL:            ${repository.url}
Permissions:
<#if repository.permissions??>
<#list repository.permissions as permission>
  ${permission.type} - ${permission.name} (Group: ${permission.groupPermission?string})
</#list>
</#if>

</#list>
