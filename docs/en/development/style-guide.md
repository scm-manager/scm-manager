---
title: Style Guide
---

Starting with version 2 of SCM-Manager we have decided to change the code style and conform to more common rules. Furthermore we abandon the rule, that everything needs to have a javadoc description. Nonetheless we have decided against a "big bang" adaption of the new rule, because this would have lead to enourmous problems for merges from 1.x to 2.x.

So whenever touching 1.x code you have to make the decision, whether it is appropriate to migrate some of the code you touch to the new style. Always keep in mind, that even slight changes may be dangerous becaus old code might not have a good test coverage.

Also it is a good guide line to adapt Postel's law: *Be conservative in what you do, be liberal in what you accept from others.* So do not be the wise guy changing everything that does not fit to the rules below just because.

## Java

Please mind the [EditorConfig](https://editorconfig.org/) file `.editorconfig` in the root of the SCM-Manager and the [configuration guide](docs/en/intellij-idea-configuration.mdation.md) for IntelliJ IDEA. There are plugins for a lot of IDEs and text editors.

In the following, there are some rules we have come to value and that we would like to follow. Some are simple, others might need
more skill. If you feel overwhelmed by them, do not let them deter you. We love to support you in keeping these rules.

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
- A space after keywords like `if`, `else`, `for`, `while`, `try`, and `catch`, between closing and opening parens after
  conditions and after semicolons, like this
  ```java
  if (someCondition()) {
    thisShouldBeDone();
  } else {
    somethingOtherCanHappen();
  }
  ```
  or this
  ```java
  for (int i = 0; i < some.length(); ++i) {
    doSomethingWith(i);
  }
  ```
- No separate lines for opening curly braces
- Though we will not define a maximum line length, you should break lines when they go beyond 120 characters or so
  - Long parameter lists in method or constructor definitions or calls should be consistently broken apart after each parameter
  - There should not be two statements on a single line (apart from for or while loops)
  - Closing brackets should be put on extra lines, when there is a line break after the matching opening braket
- Names (for classes, constants, fields, methods, ...) should be pronounceable
- Try to enforce encapsulation
  - If possible, collections as return values should be immutable
  - When modifying a collection in another object, do not "get" the collection and modify it but add an explicit method
    in the object holding the collection for this.
- Use Lombock annotations where appropriate. That is
  - Getter, setter and constructor annotations in data classes (do not use this in classes used for business logic)
  - Data or value annotations to reveal intent
  - Slf4j annotation can be used as a replacement for explicit logger constant creation
- We do not have strict logging rules, yet. Just some guidelines:
  - Use Slf4j's `LoggerFactory` with the class name you are in to create the `Logger` as a `static final` constant and
    named `LOG` or simply use the `Slf4j` annotation by Lombok (but not both in one class)
  - The log levels are
    - `error` for failures an administrator should take care of immediately
    - `warn` for failed processes that might lead to inconsistencies or unexpected behaviour like errors accessing external
      systems or other services
    - `info` for events that might be of interest in a daily business (like a user that has been blocked, a new access token
      that has been created, a long running task that has been started or finished)
    - `debug` for coarse information about internal processes (a pull request that has been closed, an event that has been
      triggered, a mail that has been sent)
    - `trace` to reproduce workflows
  - Think about using Micrometer metrics if possible
- Longer call chains (like when using Java's stream API or builder patterns) should be formatted in a way, that the "flow"
  is recognizable. For this there should be a line break before each new invocation like this:
  ```java
  myCollection.stream()
    .filter(item::isApplicable)
    .map(this::transform)
    .forEach(
      item -> processThisItem(item, some, more, input)
    );
  ```
- When defining APIs, evolvability is key. Often it is a good practice to use classes to enclose parameters for or return values
  of methods, so that parameters can be set independently. For example, if an interface can be implemented in (another) plugin,
  use a construct like this:
  ```java
  interface MyReceiver {
    ComputationResult compute(ComputationInput input);
  }
  
  @Data
  class ComputationInput {
    private String textInput;
    private int numberInput;
  }
  
  @Value
  class ComputationResult {
    private String textOutput;
  }
  ```
  This way it is possible, to add further optional input and output parameters without getting build breakers in implementing
  classes (that can lead to runtime errors when implemented in other plugins).
- Scope (visibility) should be as narrow as possible. Try to use private or package protected (default) scope as often as possible.
  This does not only apply for methods and fields inside of classes, but for classes, interfaces and enums, too.
- The qualifier `this` should only be used if necessary (that is, if the field has to be distinguished from another variable
  in the current scope with the same name, which should be avoided apart from constructors)
- Normally, variables and parameters should not explicitly be set `final` (mind, this does not account to fields; these should
  be marked final if it is not explicitly intended otherwise)

## JavaScript

Take a look at our styleguide using `yarn serve` in [ui-styles](scm-ui/ui-styles) directory.
