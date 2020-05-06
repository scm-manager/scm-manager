---
title: Style Guide
---

Starting with version 2 of SCM-Manager we have decided to change the code style and conform to more common rules. Furthermore we abandon the rule, that everything needs to have a javadoc description. Nonetheless we have decided against a "big bang" adaption of the new rule, because this would have lead to enourmous problems for merges from 1.x to 2.x.

So whenever touching 1.x code you have to make the decision, whether it is appropriate to migrate some of the code you touch to the new style. Always keep in mind, that even slight changes may be dangerous becaus old code might not have a good test coverage.

Also it is a good guide line to adapt Postel's law: *Be conservative in what you do, be liberal in what you accept from others.* So do not be the wise guy changing everything that does not fit to the rules below just because.

## Java

Please mind the [EditorConfig](https://editorconfig.org/) file `.editorconfig` in the root of the SCM-Manager and the [configuration guide](docs/en/intellij-idea-configuration.mdation.md) for IntelliJ IDEA. There are plugins for a lot of IDEs and text editors.

- Indentation with 2 spaces and no tabs (we have kept this rule from 1.x)
- Order of members:
  - public static fields
  - private static fields
  - public instant fields
  - private instant fields
  - constructors
  - methods
- No "star imports", that is no `import java.util.*`
- One empty line between functions
- No separate lines for opening curly braces
- Though we will not define a maximum line length, you should break lines when they go beyond 120 characters or so.

## JavaScript

Take a look at our styleguide using `yarn serve` in [ui-styles](scm-ui/ui-styles) directory.
