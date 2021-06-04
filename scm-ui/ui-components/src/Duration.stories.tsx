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
import Duration from "./Duration";
import React from "react";

storiesOf("Duration", module).add("Duration", () => (
  <div className="m-5 p-5">
    <p>
      <Duration duration={500} />
    </p>
    <p>
      <Duration duration={2000} />
    </p>
    <p>
      <Duration duration={42 * 1000 * 60} />
    </p>
    <p>
      <Duration duration={21 * 1000 * 60 * 60} />
    </p>
    <p>
      <Duration duration={5 * 1000 * 60 * 60 * 24} />
    </p>
    <p>
      <Duration duration={3 * 1000 * 60 * 60 * 24 * 7} />
    </p>
    <p>
      <Duration duration={12 * 1000 * 60 * 60 * 24} />
    </p>
  </div>
));
