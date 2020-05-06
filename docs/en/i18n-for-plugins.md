---
title: i18n for Plugins
---
How to internationalize your own plugin

### Create the plugins.json file

The translation file for plugins should be stored in the resources path
locales/*{lang}*/plugins.json

All translation keys are parts of a **unique root key**. It is
recommended to **use the maven artifactId of the plugin as root
key** to avoid conflicts with other plugin. All translation files
would be collected and merged to a single file containing all
translations. Therefore it is **necessary to use a unique root key**
for the translations.

***Example:***

the translation file of the svn plugin is stored in
locales/en/plugins.json
```json
{
 "scm-svn-plugin": {
   "information": {
     "checkout" : "Checkout repository"
   }
 }
}
```

### Usage in the own React components

SCM-Manager use react-i18next to render translations.

#### Functional components

The following steps are needed to use react-i18next in the own
functional components:

-   import react-i18next
```javascript
import { useTranslation } from "react-i18next";
```

-   use the translation keys like this:
```javascript
 const [t] = useTranslation("plugins");
 ...
 <h3>{t("scm-svn-plugin.information.checkout")}</h3>
```

#### Class components

The following steps are needed to use react-i18next in the own
class components:

-   import react-i18next
```javascript
import { WithTranslation, withTranslation } from "react-i18next";
```

-   declare the translation method `t: string => string` as property
```javascript
type Props = WithTranslation & {
***your props***
}
```

-   wrap the react component with the translate method and give the json
    translation file name \"plugins\"
```javascript
export default withTranslation("plugins")(MyPluginComponent);
```

-   use the translation keys like this:
```javascript
 const { t } = this.props;
 <h3>{t("scm-svn-plugin.information.checkout")}</h3>
```
