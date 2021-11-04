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
import React from "react";
import styled from "styled-components";
import usePopover from "./usePopover";
import Popover from "./Popover";

const Wrapper = styled.div`
  width: 100%;
  margin: 20rem;
`;

storiesOf("Popover", module)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () =>
    React.createElement(() => {
      const { triggerProps, popoverProps } = usePopover();

      return (
        <div>
          <Popover title={<strong>Spaceship Heart of Gold</strong>} width={512} {...popoverProps}>
            <p>
              The Heart of Gold is the sleekest, most advanced, coolest spaceship in the galaxy. Its stunning good looks
              mirror its awesome speed and power. It is powered by the revolutionary new Infinite Improbability Drive,
              which lets the ship pass through every point in every universe simultaneously.
            </p>
          </Popover>
          <button className="button" {...triggerProps}>
            Trigger
          </button>
        </div>
      );
    })
  )
  .add("Link", () =>
    React.createElement(() => {
      const { triggerProps, popoverProps } = usePopover();

      return (
        <div>
          <Popover title={<strong>Spaceship Heart of Gold</strong>} width={512} {...popoverProps}>
            <p>
              The Heart of Gold is the sleekest, most advanced, coolest spaceship in the galaxy. Its stunning good looks
              mirror its awesome speed and power. It is powered by the revolutionary new Infinite Improbability Drive,
              which lets the ship pass through every point in every universe simultaneously.
            </p>
          </Popover>
          <button {...triggerProps}>Trigger</button>
        </div>
      );
    })
  );
