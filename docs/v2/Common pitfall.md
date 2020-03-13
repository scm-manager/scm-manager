# Common pitfall occurred while developing the SCM V2 

## React Component is loaded unexpectedly

### Bug

A react component is updated without any changes in the props or states.

### Fix

Use the [why-did-you-update](Link https://github.com/maicki/why-did-you-update) library to analyze the causes of the updates.

A common cause is the definition of[ new functions in render()](Link https://github.com/maicki/why-did-you-update#changes-are-in-functions-only).

### Example


```
#!javascript

class Main extends React.Component<Props> {
  render() {
    const { authenticated, links } = this.props;
    const redirectUrlFactory = binder.getExtension("main.redirect", this.props);

....


const ActivityRoute = ({ authenticated, links }: RouteProps) => {
  return (
    <ProtectedRoute
      path="/activity"
      component={() => <Activity activityUrl={links.activity.href} />}
      authenticated={authenticated && links.activity.href}
    />
  );
};

binder.bind("main.route", ActivityRoute);
```
the definition of the Component like this:
   

```
#!javascript

component={() => <Activity activityUrl=links.activity.href} />}
```
triggers a re-render because: 

```
#!javascript


() => <Activity activityUrl=links.activity.href} />  !== () => <Activity activityUrl=links.activity.href} />
```

You can avoid it by binding this function in advance and then reusing it on all renders


```
#!javascript

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