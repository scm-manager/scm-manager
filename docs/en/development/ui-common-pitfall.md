---
title: Common pitfall occurred while developing the SCM V2 
---

## Introduction
There are quite some common pitfalls which can cause an inconsistent or broken ui. 
You can avoid most ugly ui glitches and broken surfaces or just improve your frontend code 
by asking yourself some questions while building the components.

## Design
There exists a scm styleguide which can support you build fitting and beautiful ui components.
Find the styleguide in "scm-ui/ui-styles".

- Have I used the colors from the scm styleguide? 
- Have I used familiar fonts which are already in use on this surface?
- Is my component scalable / resizeable?
- What happens if I insert very long / short content?
- Is my component mobile friendly (tablet / smartphone)?
- Does my component fit into the existing surface?
- Does the ui become confusing / overcrowded / ugly because of my component?
- Can I reduce the shown texts and use icons (tooltips) instead?
- Are there enough whitespaces in my component / around my component?
- Have I used translation keys and translated my content at least in german and english?

## Small, reuseable components
The SCM-Manager provides a storybook which tests many reuseable components with usage examples. 
You can also find some more ui components which doesn't have stories yet in "scm-ui/ui-components".
If a component which can be reused is missing, feel free to add it to ui-components.

- Have I checked ui-components before creating an entirely new component?
- If creating a new component should it be added to ui-components?
- Have I written one or more stories which tests my new component in the storybook?
- Does my component have too much code / logic? Can it be cut down in some smaller components?
- Have I created the new component as a react functional component?

## Other common errors
### React Component is loaded unexpectedly

#### Bug

A react component is updated without any changes in the props or states.

#### Fix

Use the [why-did-you-update](https://github.com/maicki/why-did-you-update) library to analyze the causes of the updates.

A common cause is the definition of [new functions in render()](https://github.com/maicki/why-did-you-update#changes-are-in-functions-only).

#### Example

```javascript
class Main extends React.Component<Props> {
  render() {
    const { authenticated, links } = this.props;
    const redirectUrlFactory = binder.getExtension("main.redirect", this.props);

    ...

    const ActivityRoute = ({ authenticated, links }: RouteProps) => {
      return (
        <ProtectedRoute
          path="/activity"
          component={() => <Activity activityUrl={links.activity.href} />}
          authenticated={authenticated && links.activity.href}
        />
      );
    };
  }
}

binder.bind("main.route", ActivityRoute);
```

the definition of the Component like this:

```javascript
component={() => <Activity activityUrl={links.activity.href} />}
```

triggers a re-render because: 

```javascript
() => <Activity activityUrl={links.activity.href} />  !== () => <Activity activityUrl={links.activity.href} />
```

You can avoid it by binding this function in advance and then reusing it on all renders

```javascript
class ActivityRoute extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  renderActivity = () => {
    const { links } = this.props;
    return <Activity activityUrl={links.activity.href} />;
  };

  render() {
    const { authenticated, links } = this.props;

    return (
      <ProtectedRoute
        path="/activity"
        component={this.renderActivity}
        authenticated={authenticated && links.activity.href}
      />
    );
  }
}

binder.bind("main.route", ActivityRoute);
```
