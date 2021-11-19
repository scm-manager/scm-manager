---
title: Extension Points
---

The following extension points are provided for the frontend:

# Deprecated


### changeset.avatar-factory
- Location: At every changeset (detailed view as well as changeset overview)
- can be used to add avatar (such as gravatar) for each changeset
- expects a function: `(Changeset) => void`

### repos.sources.view
- Location: At sources viewer
- can  be used to render a special source that is not an image or a source code

### main.redirect
- Extension Point for a link factory that provide the Redirect Link 
- Actually used from the activity plugin: binder.bind("main.redirect", () => "/activity");

### markdown-renderer-factory
- A Factory function to create markdown [renderer](https://github.com/rexxars/react-markdown#node-types)
- The factory function will be called with a renderContext parameter of type Object. this parameter is given as a prop for the MarkdownView component.

**example:**


```javascript
let MarkdownFactory = (renderContext) => {
 
  let Heading= (props) => {
    return React.createElement(`h${props.level}`,
      props['data-sourcepos'] ? {'data-sourcepos': props['data-sourcepos']} : {},
      props.children);
  };
    return {heading : Heading};
};

binder.bind("markdown-renderer-factory", MarkdownFactory);
```

```javascript
<MarkdownView
    renderContext={{pullRequest, repository}}
    className="content"
    content={pullRequest.description}
/>
```
