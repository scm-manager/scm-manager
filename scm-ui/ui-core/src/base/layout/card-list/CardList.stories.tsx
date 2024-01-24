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
import { ComponentMeta, ComponentStory } from "@storybook/react";
import React from "react";
import { Link } from "react-router-dom";
import CardList, { CardListBox, CardListCard } from "./CardList";
import CardTitle from "../card/CardTitle";
import { Menu } from "../../overlays";
import { Icon } from "../../buttons";
import CardRow, { SecondaryRow } from "../card/CardRow";
import { CardDetail, CardDetailLabel, CardDetails, CardDetailTag, CardLinkDetail } from "../card/CardDetail";

export default {
  title: "CardList",
  component: CardList,
  decorators: [StoryRouter()],
} as ComponentMeta<typeof CardList>;

export const Default: ComponentStory<typeof CardListBox> = () => (
  <CardListBox>
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
      <CardRow>
        Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et
        dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet
        clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet,
        consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed
        diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea
        takimata sanctus est Lorem ipsum dolor sit amet.
      </CardRow>
    </CardListCard>
    <CardListCard>
      <CardRow>
        <CardTitle>My favorite repository</CardTitle>
      </CardRow>
    </CardListCard>
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
      <CardRow>
        <CardTitle level={5}>
          <Link aria-label="Edit My least liked repo" to="/cards/1">
            My favorite repository with a very long title and some other things
          </Link>
        </CardTitle>
      </CardRow>
    </CardListCard>
    <CardListCard>
      <CardRow>
        <CardTitle>
          <Link aria-label="Edit My least liked repo" to="/cards/1">
            My least liked repo
          </Link>
        </CardTitle>
      </CardRow>
    </CardListCard>
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
    </CardListCard>
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
    </CardListCard>
    <CardListCard>
      <CardRow>
        <CardTitle>
          <Link aria-label="Edit My least liked repo" to="/cards/1">
            My least liked repo
          </Link>
        </CardTitle>
      </CardRow>
      <CardRow>
        <CardDetails>
          <CardDetail>
            <CardDetailLabel>Workers</CardDetailLabel>
            <CardDetailTag>2/3</CardDetailTag>
          </CardDetail>
          <CardLinkDetail to="/my/detail">
            <CardDetailLabel>Go to docs</CardDetailLabel>
          </CardLinkDetail>
        </CardDetails>
      </CardRow>
    </CardListCard>
    <CardListCard avatar={<Icon>user</Icon>} action={<Icon>ellipsis-v</Icon>}>
      <CardRow>
        <CardTitle>
          3c991eec687444630c0929e4e23d8a1a2565011d4bea28d4338dd1d024bb74c8
          c076ca5eea66545ee227c8854acc8b9bf075676a6848d54fa0bc1fa291f78887
          72d7e2ef64c9575dd8ceeed8ed6b24f185646deb6595b13fd51c5705c61a2c46
          9127daa67e066bc49f39cca9670b92de3d576ac1fb9c916e9b44692923e12a9d
          14d07434e8c5b3d0ba2e752bc580888a30963d4e8021be573392bb625f6150da
          60fc6f2e7503b1ca5963afb627ef560f4e2191c0da4c9328ae4ab088e177fb41
          749e63a6af1731d5c599e960a2f6c8cb9a15d6cf6a82493f419d417829f7b2a8
          0ca9334aeda2e5dab101e4af13c9610839afc3b9dd2ec56ffb067d6914ce9b67
          b708983948a1750f79fbb91875399fcce453410dad6191c5dc5059f4b28aee1d
          0d13a4349270947bc79cfc59c7c2aa59960d847a49b40feccd3388fa9a600a68
        </CardTitle>
      </CardRow>
      <SecondaryRow className="is-ellipsis-overflow">
        7cab5486ab8cd946af71a77d37c84bac c05156ef54a1f0bfd5d4fa12f774148c 4f2964e1895470b6313e3264fef276d8
        8c57fdf7fde5fc227ea0c59a0f359122 3bc64067ff6fb9c64f4ae5ac15e4375d 91943ad0c020859ad6cc3723fe9bd325
        07bb6d93d9faf2df68d02949ec10e58e 0bee2b579b7ab5777683f3d5b5975960 4a3009269d971f555524374e7da745ad
        a693ffb57da89f191249de6480c2387b
      </SecondaryRow>
      <CardRow>
        <CardDetails>
          <CardDetail>
            <CardDetailLabel>Workers</CardDetailLabel>
            <CardDetailTag>2/3</CardDetailTag>
          </CardDetail>
          <CardLinkDetail to="/to/docs">
            <CardDetailLabel>Go to docs</CardDetailLabel>
          </CardLinkDetail>
        </CardDetails>
      </CardRow>
    </CardListCard>
  </CardListBox>
);
