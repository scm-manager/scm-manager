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

import styled from "styled-components";
import { devices } from "./devices";

export const DangerZone = styled.div`
  border: 1px solid #ff6a88;
  border-radius: 5px;

  > .level {
    flex-flow: wrap;

    .level-left {
      max-width: 100%;
    }

    .level-right {
      margin-top: 0.75rem;
    }
  }

  // TODO ersetzen?
  > *:not(:last-child) {
    padding-bottom: 1.5rem;
    border-bottom: solid 2px whitesmoke;
  }
  @media screen and (max-width: ${devices.tablet.width}px) {
    .button {
      height: 100%;
      white-space: break-spaces;
    }
  }
`;

export default DangerZone;
