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
