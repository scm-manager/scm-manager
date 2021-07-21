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

import React, { FC } from "react";
import { Hit as HitType } from "@scm-manager/ui-types";

export type HitProps = {
  hit: HitType;
};

type SearchResultType = FC & {
  Title: FC;
  Left: FC;
  Content: FC;
  Right: FC;
};

const Hit: SearchResultType = ({ children }) => {
  return <article className="media p-1">{children}</article>;
};

Hit.Title = ({ children }) => <h3 className="has-text-weight-bold is-ellipsis-overflow">{children}</h3>;

Hit.Left = ({ children }) => <div className="media-left">{children}</div>;

Hit.Right = ({ children }) => <div className="media-right is-size-7 has-text-right">{children}</div>;

Hit.Content = ({ children }) => <div className="media-content">{children}</div>;

export default Hit;
