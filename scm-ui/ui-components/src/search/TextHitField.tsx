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

import React, { FC, Fragment } from "react";
import { HighlightedHitField, Hit } from "@scm-manager/ui-types";
import HighlightedFragment from "./HighlightedFragment";
import { isHighlightedHitField } from "./fields";
import { SyntaxHighlighter } from "@scm-manager/ui-syntaxhighlighting";

const MarkerWrapper: FC = ({ children }) => <mark>{children}</mark>;

const MARKER_CONFIG = {
  start: "<|[[--",
  end: "--]]|>",
  wrapper: MarkerWrapper,
};

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
          {field.matchesContentStart ? null : separator}
          <FieldFragment fragment={fragment} syntaxHighlightingLanguage={syntaxHighlightingLanguage} />
          {i + 1 >= field.fragments.length && !field.matchesContentEnd ? separator : null}
        </React.Fragment>
      ))}
    </>
  );
};

type FieldFragmentProps = {
  fragment: string;
  syntaxHighlightingLanguage?: string;
};

const FieldFragment: FC<FieldFragmentProps> = ({ fragment, syntaxHighlightingLanguage }) => {
  if (syntaxHighlightingLanguage) {
    return (
      <SyntaxHighlighter
        value={fragment}
        language={syntaxHighlightingLanguage}
        markerConfig={MARKER_CONFIG}
        nodeLimit={600}
        as={Fragment}
      />
    );
  }
  return <HighlightedFragment value={fragment} />;
};

type Props = {
  hit: Hit;
  field: string;
  truncateValueAt?: number;
  syntaxHighlightingLanguage?: string;
};

function truncate(value: string, truncateValueAt = 0, syntaxHighlightingLanguage?: string): string {
  if (truncateValueAt > 0 && value.length > truncateValueAt) {
    if (syntaxHighlightingLanguage) {
      const nextLineBreak = value.indexOf("\n", truncateValueAt);
      if (nextLineBreak >= 0 && nextLineBreak < value.length - 1) {
        value = value.substring(0, nextLineBreak) + "\n...";
      }
    } else {
      value = value.substring(0, truncateValueAt) + "...";
    }
  }
  return value;
}

const TextHitField: FC<Props> = ({
  hit,
  field: fieldName,
  children,
  syntaxHighlightingLanguage,
  truncateValueAt = 0,
}) => {
  const field = hit.fields[fieldName];
  if (!field) {
    return <>{children}</>;
  } else if (isHighlightedHitField(field)) {
    return <HighlightedTextField field={field} syntaxHighlightingLanguage={syntaxHighlightingLanguage} />;
  } else {
    const value = field.value;
    if (value === "") {
      return <>{children}</>;
    } else if (typeof value === "string") {
      const v = truncate(value, truncateValueAt, syntaxHighlightingLanguage);
      return <FieldFragment fragment={v} syntaxHighlightingLanguage={syntaxHighlightingLanguage} />;
    }
    return <>{value}</>;
  }
};

export default TextHitField;
