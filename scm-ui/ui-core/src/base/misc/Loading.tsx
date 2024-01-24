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
import React, { HTMLAttributes } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import Image from "./Image";

const Wrapper = styled.div`
  min-height: 256px;
`;

const FixedSizedImage = styled(Image)`
  width: 128px;
  height: 128px;
`;

const Loading = React.forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement> & { message?: string }>(
  ({ message, children, className, ...props }, ref) => {
    const [t] = useTranslation("commons");
    return (
      <Wrapper
        className={classNames(
          "is-flex",
          "is-flex-direction-column",
          "is-justify-content-center",
          "is-align-items-center",
          className
        )}
        {...props}
        ref={ref}
      >
        <FixedSizedImage className="mb-3" src="/images/loading.svg" alt={t("loading.alt")} />
        <p className="has-text-centered">
          {message}
          {children}
        </p>
      </Wrapper>
    );
  }
);

export default Loading;
