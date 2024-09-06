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

import styled from "styled-components";
import { storiesOf } from "@storybook/react";
import React, { ReactNode } from "react";
import Tag from "./Tag";
import { MemoryRouter } from "react-router-dom";
import { Color, colors, sizes } from "./styleConstants";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const Spacing = styled.div`
  padding: 0.5rem;
`;

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("Tag", module)
  .addDecorator(RoutingDecorator)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => <Tag label="Default tag" />)
  .add("Rounded", () => <Tag label="Rounded tag" color="dark" rounded={true} />)
  .add("With Icon", () => <Tag label="System" icon="bolt" />)
  .add("Colors", () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Tag color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ))
  .add("Outlined", () => (
    <div>
      {(["success", "black", "danger"] as Color[]).map((color) => (
        <Spacing key={color}>
          <Tag color={color} label={color} outlined={true} />
        </Spacing>
      ))}
    </div>
  ))
  .add("With title", () => <Tag label="hover me" title="good job" />)
  .add("Clickable", () => <Tag label="Click here" onClick={() => alert("Not so fast")} />)
  .add("Sizes", () => (
    <div>
      {sizes.map((size) => (
        <Spacing key={size}>
          <Tag size={size} label={size} />
        </Spacing>
      ))}
    </div>
  ));
