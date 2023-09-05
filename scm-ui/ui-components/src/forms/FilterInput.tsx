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
import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { createAttributesForTesting } from "../devBuild";
import classNames from "classnames";

type Props = {
  filter: (p: string) => void;
  value?: string;
  testId?: string;
  placeholder?: string;
  autoFocus?: boolean;
  className?: string;
  id?: string;
};

const FixedHeightInput = styled.input`
  height: 2.5rem;
`;

/**
 * @deprecated
 */
const FilterInput: FC<Props> = ({ filter, value, testId, placeholder, autoFocus, className, id }) => {
  const [stateValue, setStateValue] = useState(value || "");
  const [timeoutId, setTimeoutId] = useState<ReturnType<typeof setTimeout>>();
  const [t] = useTranslation("commons");

  // TODO check dependencies
  useEffect(() => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    if (!stateValue) {
      // no delay if filter input was deleted
      filter(stateValue);
    } else {
      // with delay while typing
      const id = setTimeout(() => filter(stateValue), 1000);
      setTimeoutId(id);
    }
  }, [stateValue]);

  const handleSubmit = (event: FormEvent) => {
    filter(stateValue);
    event.preventDefault();
  };

  return (
    <form className={classNames("input-field", className)} onSubmit={handleSubmit}>
      <div className="control has-icons-left">
        <FixedHeightInput
          className="input"
          type="search"
          placeholder={placeholder || t("filterEntries")}
          value={stateValue}
          onChange={(event) => setStateValue(event.target.value)}
          autoFocus={autoFocus || false}
          aria-describedby={id}
          aria-label={t("filterEntries")}
          {...createAttributesForTesting(testId)}
        />
        <span className="icon is-small is-left">
          <i className="fas fa-filter" />
        </span>
      </div>
    </form>
  );
};

export default FilterInput;
