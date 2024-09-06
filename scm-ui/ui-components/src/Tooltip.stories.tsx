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
import * as React from "react";
import { ReactNode } from "react";
import { MemoryRouter } from "react-router-dom";
import { storiesOf } from "@storybook/react";
import Tooltip, { TooltipLocation } from "./Tooltip";
import Button from "./buttons/Button";

const Wrapper = styled.div`
  margin: 2rem;
  width: 100%;
`;

const Spacing = styled.div`
  padding: 2em 6em;
  width: 50%;
  margin: 0 auto;
`;

const positions: TooltipLocation[] = ["right", "top", "left", "bottom"];

const message = "Heart of Gold";

const mutltiline = `Characters:

- Arthur Dent
- Ford Prefect
- Zaphod Beeblebrox
- Marvin the Paranoid Android
- Trillian
- Slartibartfast`;

const shortMultiline = `* a
* b
* c`;

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("Tooltip", module)
  .addDecorator(RoutingDecorator)
  .add("Default", () => (
    <Wrapper>
      {positions.map((position) => (
        <Spacing>
          <Tooltip message={message} location={position}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </Wrapper>
  ))
  .add("Multiline", () => (
    <Wrapper>
      {positions.map((position) => (
        <Spacing>
          <Tooltip message={mutltiline} location={position}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </Wrapper>
  ))
  .add("Short Multiline", () => (
    <Wrapper>
      {positions.map((position) => (
        <Spacing>
          <Tooltip message={shortMultiline} location={position}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </Wrapper>
  ))
  .add("Styled", () => (
    <Wrapper>
      {positions.map((position) => (
        <Spacing>
          <Tooltip message={message} location={position} className={"has-text-warning"}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </Wrapper>
  ));
