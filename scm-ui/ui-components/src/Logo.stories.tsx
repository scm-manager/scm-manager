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
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import Logo from "./Logo";

const Wrapper = styled.div`
  padding: 2em;
  background-color: black;
  height: 100%;
`;

storiesOf("Logo", module)
  .add("Default", () => (
    <Wrapper>
      <Logo />
    </Wrapper>
  ))
  .add("WithoutText", () => (
    <Wrapper>
      <Logo withText={false} />
    </Wrapper>
  ));
