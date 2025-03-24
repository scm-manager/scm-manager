---
title: DOD for UI development
---

Use this as a kind of checklist whenever you develop something in the UI of SCM-Manager, regardless whether you are developing core features or plugins.

|   | Don't forget to |
|---|-----------------|
| ☐ | use imports with `@scm-manager`, eg. `@scm-manager/ui-components` |
| ☐ | let buttons have whitespace |
| ☐ | update german translation |
| ☐ | add help icons to input components |
| ☐ | not use colors directly, but refer to `is-primary` or `is-warning` |
| ☐ | make sure your view works on mobile devices |
| ☐ | document [extension points](../plugins/extension-points) |
