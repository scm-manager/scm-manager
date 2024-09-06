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

export default `# Images

Show case for different possibilities to render image src links.
Please note that some of the images do not work in storybook,
the story is mostly for checking if the src links are rendered correct.

## External

External images should be rendered with the unaltered link:

![external image](https://github.com/scm-manager/scm-manager/blob/develop/docs/en/logo/scm-manager_logo.png)

## Images from repository

Images from the repository should be resolved to an api url:

![relative path](some_image.jpg)

![path starting with a '.'](./some_image.jpg)

![absolute image path](/path/with/some_image.jpg)
`;
