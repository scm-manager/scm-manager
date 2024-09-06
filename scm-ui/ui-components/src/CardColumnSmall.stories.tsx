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

import React from "react";
import { MemoryRouter } from "react-router-dom";
import { storiesOf } from "@storybook/react";
import CardColumnSmall from "./CardColumnSmall";
import Icon from "./Icon";
import styled from "styled-components";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const link = "/foo/bar";
const avatar = <Icon name="icons fa-2x fa-fw" alt="avatar" />;
const contentLeft = <strong className="m-0">main content</strong>;
const contentRight = <small>more text</small>;

storiesOf("CardColumnSmall", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => (
    <CardColumnSmall link={link} avatar={avatar} contentLeft={contentLeft} contentRight={contentRight} />
  ))
  .add("Minimal", () => <CardColumnSmall link={link} contentLeft={contentLeft} contentRight={contentRight} />)
  .add("Task", () => (
    <CardColumnSmall
      link={link}
      avatar={<Icon name="exchange-alt" className="fa-fw fa-lg" color="inherit" alt="avatar" />}
      contentLeft={<strong>Repository created</strong>}
      contentRight={<small>over 42 years ago</small>}
      footer="New: scmadmin/spaceship"
    />
  ))
  .add("Linkless", () => (
    <CardColumnSmall
      avatar={<Icon name="eraser" className="fa-fw fa-lg" color="inherit" alt="avatar" />}
      contentLeft={<strong>Repository deleted</strong>}
      contentRight={<small>over 1337 minutes ago</small>}
      footer="Deleted: scmadmin/spaceship"
    />
  ));
