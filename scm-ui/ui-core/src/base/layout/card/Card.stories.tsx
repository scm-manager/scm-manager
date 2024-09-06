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

import StoryRouter from "storybook-react-router";
import { ComponentMeta, ComponentStory } from "@storybook/react";
import React from "react";
import { Link } from "react-router-dom";
import CardTitle from "../card/CardTitle";
import CardRow, { SecondaryRow, TertiaryRow } from "../card/CardRow";
import {
  CardButtonDetail,
  CardDetail,
  CardDetailIcon,
  CardDetailLabel,
  CardDetails,
  CardDetailTag,
  CardLinkDetail,
} from "./CardDetail";
import Card from "./Card";
import { Popover } from "../../overlays";
import { Icon } from "../../buttons";

export default {
  title: "Card",
  component: Card,
  decorators: [StoryRouter()],
} as ComponentMeta<typeof Card>;

export const Default: ComponentStory<typeof Card> = () => (
  <Card
    className="box"
    avatar={<Icon className="is-large">user</Icon>}
    action={<Icon className="is-medium">ellipsis-v</Icon>}
  >
    <CardRow>
      <CardTitle>
        <Link aria-label="Edit My least liked repo" to="/cards/1">
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
        </Link>
      </CardTitle>
    </CardRow>
    <SecondaryRow className="is-ellipsis-overflow">
      7cab5486ab8cd946af71a77d37c84bac c05156ef54a1f0bfd5d4fa12f774148c 4f2964e1895470b6313e3264fef276d8
      8c57fdf7fde5fc227ea0c59a0f359122 3bc64067ff6fb9c64f4ae5ac15e4375d 91943ad0c020859ad6cc3723fe9bd325
      07bb6d93d9faf2df68d02949ec10e58e 0bee2b579b7ab5777683f3d5b5975960 4a3009269d971f555524374e7da745ad
      a693ffb57da89f191249de6480c2387b
    </SecondaryRow>
    <TertiaryRow>This is information is not important</TertiaryRow>
    <CardRow>
      <CardDetails>
        <Popover
          trigger={
            <CardButtonDetail>
              <CardDetailLabel>Popover Detail with Tag</CardDetailLabel>
              <CardDetailTag>3</CardDetailTag>
            </CardButtonDetail>
          }
          title="My Popover Details"
        >
          <>This is some additional detail for my Popover.</>
        </Popover>
        <CardDetail>
          <CardDetailLabel>Normal Detail with Tag</CardDetailLabel>
          <CardDetailTag>2/3</CardDetailTag>
        </CardDetail>
        <CardLinkDetail to="/workers">
          <CardDetailLabel>Link Detail</CardDetailLabel>
        </CardLinkDetail>
        <Popover
          trigger={
            <CardButtonDetail>
              <CardDetailLabel>Popover Detail with Icon</CardDetailLabel>
              <CardDetailIcon className="has-text-success">check-circle</CardDetailIcon>
            </CardButtonDetail>
          }
          title="My Popover Details"
        >
          <>This is some additional detail for my Popover.</>
        </Popover>
      </CardDetails>
    </CardRow>
  </Card>
);
