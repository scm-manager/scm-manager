# Common pitfalls ui development

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
