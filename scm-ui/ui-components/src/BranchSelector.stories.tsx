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

import { storiesOf } from "@storybook/react";
import { BranchSelector } from "./index";
import { Branch } from "@scm-manager/ui-types";
import * as React from "react";
import styled from "styled-components";

const master = { name: "master", revision: "1", defaultBranch: true, _links: {} };
const develop = { name: "develop", revision: "2", defaultBranch: false, _links: {} };

const branchSelected = (branch?: Branch) => null;

const branches = [master, develop];

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

storiesOf("BranchSelector", module)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => <BranchSelector branches={branches} onSelectBranch={branchSelected} label="Select Branch" />);
