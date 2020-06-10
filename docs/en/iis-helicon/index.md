---
title: SCM-Manager on IIS Helicon
---
First of all this setup is not recommended, because there were a lot of problems with it and it was never tested by the development team of SCM-Manager. However there are several working installation out there. But there some pitfalls:

**Problem description:**

When moving of copying files in SVN, the commit fails with the following message:

```bash
MyWorkstation:MyRepo user$ svn mv A.cs B.cs
A         B.cs
D         A.cs
MyWorkstation:MyRepo user$ svn commit -m "Renamed A.cs to B.cs"
Deleting       A.cs
Adding         B.cs
svn: E175002: Commit failed (details follow):
svn: E175002: The COPY request returned invalid XML in the response: XML parse error at line 1: no element found (/svn/MyRepo/!svn/bc/4/A.cs)
MyWorkstation:MyRepo user$ 

```

This problem only occurs when accessing the repository via https, not http.

**Solution:**

1. Add the following rewrite rule to the web.config of the SCM application:

```xml
<system.webServer>
    <!-- <heliconZoo /> -->
    <!-- <handlers /> -->
    <rewrite>
        <rules>
            <rule name="Rewrite Destination Header" stopProcessing="true">
                <match url=".+" />
                <conditions>
                    <add input="{REQUEST_METHOD}" pattern="MOVE|COPY" />
                    <add input="{HTTP_Destination}" pattern="^https://(.+)$" />
                </conditions>
                <serverVariables>
                    <set name="HTTP_Destination" value="http://{C:1}" />
                </serverVariables>
                <action />
            </rule>
        </rules>
    </rewrite>
</system.webServer>
```

2. Add HTTP_Destination to the Allowed Server Variables using IIS:

![Helicon: Allowed Server Variables](assets/helicon-server-vars.png)

For more information, see issue [#624](https://github.com/scm-manager/scm-manager/issues/624).
