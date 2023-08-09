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

import StoryRouter from "storybook-react-router";
import { ComponentMeta, StoryFn } from "@storybook/react";
import React, { ComponentProps } from "react";
import { ExtractProps } from "@scm-manager/ui-extensions";
import { Link } from "react-router-dom";
import CardTitle from "../card/CardTitle";
import CardRow from "../card/CardRow";
import { CardDetail, CardDetailLabel, CardDetails, CardDetailTag } from "./CardDetail";
import Card from "./Card";

export default {
  title: "Card",
  component: Card,
  decorators: [StoryRouter()],
} as ComponentMeta<typeof Card>;

const Template: StoryFn<ExtractProps<typeof Card>> = (args) => <Card {...args} />;

export const Default = Template.bind({});
// More on args: https://storybook.js.org/docs/react/writing-stories/args
Default.args = {
  className: "box",
  children: [
    <CardTitle>
      <Link aria-label="Edit My least liked repo" to="/cards/1">
        My least liked repo
      </Link>
    </CardTitle>,
    <CardRow>
      <CardDetails>
        <CardDetail>
          {({ labelId }) => (
            <>
              <CardDetailLabel id={labelId}>Workers</CardDetailLabel>
              <CardDetailTag aria-labelledby={labelId}>2/3</CardDetailTag>
            </>
          )}
        </CardDetail>
        <CardDetail>
          <CardDetailLabel>MyCustomDetail</CardDetailLabel>
          <a className="is-relative" href="https://scm-manager.org">
            Docs
          </a>
        </CardDetail>
      </CardDetails>
    </CardRow>,
  ],
} as ComponentProps<typeof Card>;
