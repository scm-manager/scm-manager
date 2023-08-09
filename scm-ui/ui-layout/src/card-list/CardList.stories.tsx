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
import CardList, { CardListBox, CardListCard } from "./CardList";
import CardTitle from "../card/CardTitle";
import { Menu } from "@scm-manager/ui-overlays";
import { Icon } from "@scm-manager/ui-buttons";
import CardRow from "../card/CardRow";
import { CardDetail, CardDetailLabel, CardDetails, CardDetailTag } from "../card/CardDetail";

export default {
  title: "CardList",
  component: CardList,
  decorators: [StoryRouter()],
} as ComponentMeta<typeof CardList>;

const Template: StoryFn<ExtractProps<typeof CardListBox>> = (args) => <CardListBox {...args} />;

export const Default = Template.bind({});
// More on args: https://storybook.js.org/docs/react/writing-stories/args
Default.args = {
  children: [
    <CardListCard>
      <CardTitle>My favorite repository</CardTitle>
    </CardListCard>,
    <CardListCard
      action={
        <Menu>
          <Menu.Button>
            <Icon>trash</Icon>
            Delete
          </Menu.Button>
        </Menu>
      }
    >
      Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et
      dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita
      kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur
      sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
      voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata
      sanctus est Lorem ipsum dolor sit amet.
    </CardListCard>,
    <CardListCard
      action={
        <Menu>
          <Menu.Button>
            <Icon>trash</Icon>
            Delete
          </Menu.Button>
        </Menu>
      }
    >
      <CardTitle level={5}>
        <Link aria-label="Edit My least liked repo" to="/cards/1">
          My favorite repository with a very long title and some other things
        </Link>
      </CardTitle>
    </CardListCard>,
    <CardListCard>
      <CardTitle>
        <Link aria-label="Edit My least liked repo" to="/cards/1">
          My least liked repo
        </Link>
      </CardTitle>
    </CardListCard>,
    <CardListCard
      action={
        <Menu>
          <Menu.Button>
            <Icon>trash</Icon>
            Delete
          </Menu.Button>
        </Menu>
      }
    >
      <CardRow className="is-flex is-justify-content-space-between">
        <CardTitle>
          <Link aria-label="Edit My other favorite repository" to="/cards/1">
            My other favorite repository
          </Link>
        </CardTitle>
        (TAG)
      </CardRow>
      <CardRow className="is-size-6">
        This is a card description in the second row. Highlighting how the layout flows if there are multiple rows in
        one card while the card also has an action.
      </CardRow>
      <CardRow className="is-size-6 is-flex is-justify-content-space-between">
        <span>This is a third row, lets see how this works out.</span>(MERGED)
      </CardRow>
    </CardListCard>,
    <CardListCard>
      <CardRow className="is-flex is-align-items-center">
        <CardTitle>
          <Link
            aria-label="Edit Enhance descriptions to differentiate between dumps with and without metadata."
            to="/cards/1"
          >
            Enhance descriptions to differentiate between dumps with and without metadata.
          </Link>
        </CardTitle>
        <small>#13456</small>
      </CardRow>
      <CardRow className="is-size-6">
        Another Name requested to merge <strong>feature/asdkjertg</strong> into <strong>develop</strong> about 3 months
        ago.
      </CardRow>
      <CardRow className="is-size-6 is-flex is-justify-content-space-between">
        <div>
          <span>Tasks (3/3)</span>
          <span>Reviewers (1)</span>
          <span>Analyses (✓)</span>
          <span>Workflow (✓)</span>
        </div>
        <span>(OPEN)</span>
      </CardRow>
    </CardListCard>,
    <CardListCard>
      <CardTitle>
        <Link aria-label="Edit My least liked repo" to="/cards/1">
          My least liked repo
        </Link>
      </CardTitle>
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
      </CardRow>
    </CardListCard>,
  ],
} as ComponentProps<typeof CardListBox>;
