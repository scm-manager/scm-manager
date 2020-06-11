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
<ExtensionPoint name="repo.avatar" props={type: "git"} />
```
