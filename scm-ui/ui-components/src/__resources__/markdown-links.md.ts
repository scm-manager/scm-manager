/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

export default `# Links

Show case for different style of markdown links. 
Please note that some of the links may not work in storybook, 
the story is mostly for checking if the links are rendered correct.

## External

External Links should be opened in a new tab: [external link](https://scm-manager.org)

## Anchor

Anchor Links should be rendered a simple a tag with an href: [anchor link](#sample)

## Protocol

Links with a protocol other than http should be rendered a simple a tag with an href e.g.: [mail link](mailto:marvin@hitchhiker.com)

## Custom Protocol

Renderers for custom protocols can be added via the "markdown-renderer.link.protocol" extension point: [description of scw link](scw:marvin@hitchhiker.com)

## Internal

Internal links should be rendered by react-router: [internal link](/buttons)
`;
