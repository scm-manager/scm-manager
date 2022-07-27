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

import React from "react";

import {
  Button as ButtonComponent,
  ButtonVariantList,
  ButtonVariants,
  ExternalLinkButton as ExternalLinkButtonComponent,
  LinkButton as LinkButtonComponent,
} from "./button";
// @ts-ignore
import StoryRouter from "storybook-react-router";
import { StoryFn } from "@storybook/react";

type ExtractProps<T> = T extends React.ComponentType<infer U> ? U : never;

// More on default export: https://storybook.js.org/docs/react/writing-stories/introduction#default-export
export default {
  title: "Components",
  component: null,
  subcomponents: {
    Button: ButtonComponent,
    LinkButton: LinkButtonComponent,
    ExternalLinkButton: ExternalLinkButtonComponent,
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
const ButtonTemplate: StoryFn<ExtractProps<typeof ButtonComponent>> = (args) => <ButtonComponent {...args} />;
const LinkButtonTemplate: StoryFn<ExtractProps<typeof LinkButtonComponent>> = (args) => (
  <LinkButtonComponent {...args} />
);
const ExternalLinkButtonTemplate: StoryFn<ExtractProps<typeof ExternalLinkButtonComponent>> = (args) => (
  <ExternalLinkButtonComponent {...args} />
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
