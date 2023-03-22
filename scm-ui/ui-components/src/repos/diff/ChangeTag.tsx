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

import { FileDiff } from "@scm-manager/ui-types";
import Tag from "../../Tag";
import classNames from "classnames";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";

type Props = { file: FileDiff };

const ChangeTag: FC<Props> = ({ file }) => {
  const [t] = useTranslation("repos");
  if (!file.type) {
    return null;
  }
  const key = "diff.changes." + file.type;
  let value = t(key);
  if (key === value) {
    value = file.type;
  }

  const color = value === "added" ? "success" : value === "deleted" ? "danger" : "info";
  return (
    <Tag
      className={classNames("has-text-weight-normal", "ml-3")}
      rounded={true}
      outlined={true}
      color={color}
      label={value}
    />
  );
};

export default ChangeTag;
