---
title: UI-Extensions
subtitle: How to extend the SCM-Manager UI with plugins
---

UI-Extensions contains the building blocks for the [SCM-Manager](https://scm-manager.org) ui extension system.

## Extensions and ExtensionPoints

Extension points are spots in the ui, where the ui could be extended or modified. 
An extension point requires a unique name and is represented as [React](https://reactjs.org/) component.

Example:

```xml
<div>
  <h2>Repository</h2>
  <ExtensionPoint name="repo.details" />
</div>
```

We can register an extension, in the form of a [React](https://reactjs.org/) component, to the "repo.details" extension point, by using the binder:

```javascript
import { binder } from "@scm-manager/ui-extensions";

const Rtfm = () => {
  return <strong>Read the f*** manual</strong>;
};

binder.bind("repo.details", Rtfm);
```

The ExtensionPoint will now find and render the Rtfm component.

### Render multiple extensions

An extension point can render multiple extensions at one. This can be done with the renderAll parameter:


```javascript
<div>
  <h2>Repository</h2>
  <ExtensionPoint name="repo.details" renderAll={true} />
</div>
```

Now we can bind multiple components to the same extension point.

```javascript
const Rtfm = () => {
  return <strong>Read the f*** manual</strong>;
};

const RealyRtfm = () => {
  return <h1>Read the f*** manual</h1>;
};

binder.bind("repo.details", Rtfm);
binder.bind("repo.details", RealyRtfm);
```

### Passing props to extensions

An extension point author can pass React properties to the extensions. This can be done with the `props` property:

```javascript
<div>
  <ExtensionPoint name="repo.title" props={{name: "myrepo"}} />
</div>
```

The extension becomes now the defined react properties as input:

```javascript
const Title = (props) => {
  return <h1>Repository {props.name}</h1>;
};

binder.bind("repo.title", Title);
```

### Defaults

An ExtensionPoint is able to render a default, if no extension is bound to the ExtensionPoint.
The default can be passed as React children:

```javascript
<ExtensionPoint name="repo.title">
  <h1>Default Title</h1>
</ExtensionPoint>
```

### Conditional rendering

An extension can specify a predicate function to the binder. 
This function becomes the props of the ExtensionPoint as input and only if the predicate returns true the extension will be rendered:

```javascript
const GitAvatar = () => {
  return <img src="/git/avatar.png" alt="git avatar" />;
};

binder.bind("repo.avatar", GitAvatar, (props) => props.type === "git");
```

```javascript
<ExtensionPoint name="repo.avatar" props={{type: "git"}} />
```

### Typings

Both extension points and extensions can share a common typescript type to define the contract between them.
This includes the `name`, the type of `props` passed to the predicate and what `type` the extensions themselves can be.

Example:
```typescript
    type CalculatorExtensionPoint = ExtensionPointDefinition<"extension.calculator", (input: number[]) => number, undefined>;
    
    const sum = (a: number, b: number) => a + b;
    binder.bind<CalculatorExtensionPoint>("extension.calculator", (input: number[]) => input.reduce(sum, 0));
    const calculator = binder.getExtension<CalculatorExtensionPoint>("extension.calculator");
    const result = calculator([1, 2, 3]);
```

In this example, we use the base type `ExtensionPointDefinition<name, type, props>` to declare a new extension point.

As we do not need a predicate, we can define the `props` type parameter as `undefined`. This allows us to skip the `props` parameter in the
`getExtension` method and the `predicate` parameter in the `bind` method.

When using `bind` to define an extension or `getExtension` to retrieve an extension, we can pass the new type as a type parameter.
By doing this, we allow typescript to help us with type-checks and offer us type-completion.

Negative Example:
```typescript
    type CalculatorExtensionPoint = ExtensionPointDefinition<"extension.calculator", (input: number[]) => number, undefined>;
    
    const sum = (a: number, b: number) => a + b;
    binder.bind<CalculatorExtensionPoint>("extension.calculato", (input: number[]) => input.reduce(sum, 0));
```

This code for example, would lead to a compile time type error because we made a typo in the `name` of the extension when binding it.
If we had used the `bind` method without the type parameter, we would not have gotten an error but run into problems at runtime.

### Children

If an extension point defines children those children are propagated to the extensions as children prop e.g:

```tsx
const MyExtension:FC = ({children}) => (
  <div className="fancy-box">{children}</div>
)
const App = () => {
  binder.bind("box", MyExtension);
  return (
    <ExtensionPoint name="box">
      <p>Box Content</p>
    </ExtensionPoint>
  );
}
```

The example above renders the following html code:

```html
<div class="fancy-box">
  <p>Box Content</p>
</div>
```

An exception is when the extension already has a children property, this could be the case if jsx is directly bind.
This exception applies not only to the children property it applies to every property.
The example below renders `Ahoi`, because the property of the jsx overwrites the one from the extension point.  

```tsx
type Props = {
  greeting: string;
}

const GreetingExtension:FC<Props> = ({greeting}) => (
  <>{greeting}</>
);

const App = () => {
  binder.bind("greet", <GreetingExtension greeting="Ahoi" />);
  return <ExtensionPoint name="greet" props={{greeting: "Moin"}} />;
};
```

### Wrapper

Sometimes it can be useful to allow plugin developers to wrap an existing component.
The `wrapper` property is exactly for this case, it allows to wrap an existing component with multiple extensions e.g.:

```tsx
const Outer: FC = ({ children }) => (
  <>Outer -> {children}</>
);

const Inner: FC = ({ children }) => (
  <>Outer -> {children}</>
);

const App = () => {
  binder.bind("wrapped", Outer);
  binder.bind("wrapped", Inner);
  return (
    <ExtensionPoint name="wrapped" renderAll={true} wrapper={true}>
      Children
    </ExtensionPoint>
  );
}
```

The example above renders `Outer -> Inner -> Children`, because each extension is passed as children to the parent extension.

### Sorting

Extensions are automatically sorted on retrieval based on either their `extensionName` (ASC) and/or their `priority` (DESC),
which can be passed upon binding an extension.

Example:

```tsx
    binder.bind("extension.point.example", <div>Hello World the fourth</div>, { priority: 10, extensionName: "ignore" });
    binder.bind("extension.point.example", <div>Hello World the third</div>, { priority: 50 });
    binder.bind("extension.point.example", <div>Hello World the first</div>, { priority: 100, extensionName: "me" });
    binder.bind("extension.point.example", <div>Hello World the second</div>, { priority: 75 });

    const extensions = binder.getExtensions("extension.point.example");
    
    /**
     * Output =>
     * 
     * Hello World the first
     * Hello World the second
     * Hello World the third
     * Hello World the fourth
     */
```
