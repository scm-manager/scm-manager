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

import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { createAttributesForTesting } from "../devBuild";
import classNames from "classnames";
import { createA11yId } from "../createA11yId";

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
  const labeldId = createA11yId("input");

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
    <form className={classNames("input-field is-flex is-align-items-center", className)} onSubmit={handleSubmit}>
      <label className="mr-2 label" id={labeldId}>
        {placeholder || t("filterEntries")}
      </label>
      <div className="control has-icons-left">
        <FixedHeightInput
          className="input"
          type="search"
          value={stateValue}
          onChange={(event) => setStateValue(event.target.value)}
          autoFocus={autoFocus || false}
          aria-describedby={id}
          aria-labelledby={labeldId}
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
