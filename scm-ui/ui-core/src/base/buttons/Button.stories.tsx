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

import React, { ComponentProps } from "react";

import {
  Button as ButtonComponent,
  ButtonVariantList,
  ButtonVariants,
  ExternalLinkButton as ExternalLinkButtonComponent,
  LinkButton as LinkButtonComponent,
  IconButton as IconButtonComponent,
} from "./Button";
import Icon from "./Icon";
import StoryRouter from "storybook-react-router";
import { StoryFn } from "@storybook/react";

// More on default export: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
export default {
  title: "Components",
  component: null,
  subcomponents: {
    Button: ButtonComponent,
    LinkButton: LinkButtonComponent,
    ExternalLinkButton: ExternalLinkButtonComponent,
    IconButton: IconButtonComponent,
  },
  argTypes: {
    variant: {
      options: ButtonVariantList,
      control: { type: "select" },
    },
  },
  decorators: [StoryRouter()],
  parameters: {
    storyshots: { disable: true },
  },
};

// More on component templates: https://storybook.js.org/docs/react/writing-stories/introduction#using-args
const ButtonTemplate: StoryFn<ComponentProps<typeof ButtonComponent>> = (args) => <ButtonComponent {...args} />;
const LinkButtonTemplate: StoryFn<ComponentProps<typeof LinkButtonComponent>> = (args) => (
  <LinkButtonComponent {...args} />
);
const ExternalLinkButtonTemplate: StoryFn<ComponentProps<typeof ExternalLinkButtonComponent>> = (args) => (
  <ExternalLinkButtonComponent {...args} />
);

const IconButtonTemplate: StoryFn<ComponentProps<typeof IconButtonComponent>> = (args) => (
  <IconButtonComponent {...args} />
);

export const Button = ButtonTemplate.bind({});
// More on args: https://storybook.js.org/docs/react/writing-stories/args
Button.args = {
  children: "Button",
  variant: ButtonVariants.PRIMARY,
  disabled: false,
};

export const LinkButton = LinkButtonTemplate.bind({});
// More on args: https://storybook.js.org/docs/react/writing-stories/args
LinkButton.args = {
  children: "Link Button",
  to: "/repos",
  variant: ButtonVariants.PRIMARY,
};

export const ExternalLinkButton = ExternalLinkButtonTemplate.bind({});
// More on args: https://storybook.js.org/docs/react/writing-stories/args
ExternalLinkButton.args = {
  children: "External Link Button",
  href: "https://scm-manager.org",
  variant: ButtonVariants.PRIMARY,
};

const smallIcon = <Icon className="is-small">trash</Icon>;
const mediumIcon = <Icon className="is-medium">trash</Icon>;

  /*

    Variant and size are defaulted to medium and colored and do not have to be explicitly added as parameters.
    However for the sake of documentation here they are still passed in

   */
export const IconButtonBorder = IconButtonTemplate.bind({});
IconButtonBorder.args = {
  children: mediumIcon,
  variant: "colored",
  size: "medium",
};


export const IconButtonBorderDefault = IconButtonTemplate.bind({});
IconButtonBorderDefault.args = {
  children: mediumIcon,
  variant: "default",
  size: "medium",
};

export const IconButtonBorderlessSmall = IconButtonTemplate.bind({});
IconButtonBorderlessSmall.args = {
  children: smallIcon,
  variant: "colored",
  size: "small",
};

export const IconButtonBorderlessSmallDefault = IconButtonTemplate.bind({});
IconButtonBorderlessSmallDefault.args = {
  children: smallIcon,
  variant: "default",
  size: "small",
};
