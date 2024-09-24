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
import Icon from "./Icon";

const Wrapper = styled.div`
  * {
    margin: 0.5rem;
  }
`;

const colors = ["primary", "link", "info", "success", "warning", "danger", "white", "light", "dark", "black", "text"];
const sizing = ["xs", "sm", "lg", "2x", "3x", "5x", "7x", "10x"];

storiesOf("Icon", module)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => (
    <>
      <Icon name="cat" />
      <Icon title="Download" name="download" color="info" />
      <Icon title="Pull Request" name="code-branch fa-rotate-180" color="warning" />
      <Icon title="Star" iconStyle="far" name="star" color="inherit" />
    </>
  ))
  .add("Colors", () => (
    <>
      <Icon title="default color" name="cat" />
      {colors.map((color) => (
        <Icon key={color} title={color} name="cat" color={color} />
      ))}
    </>
  ))
  .add("Sizing", () => (
    <>
      <Icon title="default size" name="cat" />
      {sizing.map((size) => (
        <Icon key={size} title={"fa-" + size} name={"cat fa-" + size} />
      ))}
    </>
  ))
  .add("Icon styles", () => (
    <>
      <Icon title="solid style" name="star" color="inherit" />
      <Icon title="regular style" iconStyle="far" name="star" color="inherit" />
      <Icon title="brand style" iconStyle="fab" name="react" color="inherit" />
    </>
  ))
  .add("More options", () => (
    <>
      <Icon title="rotate-270" name="snowboarding fa-rotate-270" />
      <Icon title="spin" name="spinner fa-spin" />
      <Icon title="custom sizing" name="cat" className="is-size-4" />
      <Icon title="custom background" name="hand-sparkles" className="has-background-primary-25" />
    </>
  ));
