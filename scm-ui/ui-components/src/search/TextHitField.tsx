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
import { HighlightedHitField, Hit } from "@scm-manager/ui-types";
import HighlightedFragment from "./HighlightedFragment";
import { isHighlightedHitField } from "./fields";
import SyntaxHighlightedFragment from "./SyntaxHighlightedFragment";

type HighlightedTextFieldProps = {
  field: HighlightedHitField;
  syntaxHighlightingLanguage?: string;
};

const HighlightedTextField: FC<HighlightedTextFieldProps> = ({ field, syntaxHighlightingLanguage }) => {
  const separator = syntaxHighlightingLanguage ? "...\n" : " ... ";
  return (
    <>
      {field.fragments.map((fragment, i) => (
        <React.Fragment key={fragment}>
          {separator}
          {syntaxHighlightingLanguage ? (
            <SyntaxHighlightedFragment value={fragment} language={syntaxHighlightingLanguage} />
          ) : (
            <HighlightedFragment value={fragment} />
          )}
          {i + 1 >= field.fragments.length ? separator : null}
        </React.Fragment>
      ))}
    </>
  );
};

type Props = {
  hit: Hit;
  field: string;
  truncateValueAt?: number;
  syntaxHighlightingLanguage?: string;
};

const TextHitField: FC<Props> = ({
  hit,
  field: fieldName,
  children,
  syntaxHighlightingLanguage,
  truncateValueAt = 0
}) => {
  const field = hit.fields[fieldName];
  if (!field) {
    return <>{children}</>;
  } else if (isHighlightedHitField(field)) {
    return <HighlightedTextField field={field} syntaxHighlightingLanguage={syntaxHighlightingLanguage} />;
  } else {
    let value = field.value;
    if (value === "") {
      return <>{children}</>;
    } else if (typeof value === "string" && truncateValueAt > 0 && value.length > truncateValueAt) {
      value = value.substring(0, truncateValueAt) + "...";
    }
    return <>{value}</>;
  }
};

export default TextHitField;
