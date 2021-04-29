/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
