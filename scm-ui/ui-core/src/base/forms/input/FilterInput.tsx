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

import React, { ChangeEvent, FC, FormEvent, useEffect, useRef, useState } from "react";
import classNames from "classnames";
import { useTranslation } from "react-i18next";
import Label from "../base/label/Label";
import Input from "./Input";
import { useAriaId } from "../../helpers";

const FILTER_DELAY_MS = 1000;

type Props = {
  /**
   * Called with the query a second after the last change, or immediately if the input is cleared or submitted.
   */
  filter: (query: string) => void;
  /**
   * The initial query.
   */
  value?: string;
  label?: string;
  testId?: string;
  autoFocus?: boolean;
  className?: string;
};

/**
 * @beta
 * @since 4.0.0
 */
const FilterInput: FC<Props> = ({ filter, value, label, testId, autoFocus, className }) => {
  const [t] = useTranslation("commons");
  const [query, setQuery] = useState(value ?? "");
  const inputId = useAriaId();
  const timeoutRef = useRef<ReturnType<typeof setTimeout>>(undefined);
  // the delayed call uses the latest callback, so callers do not have to memoize it
  const filterRef = useRef(filter);

  useEffect(() => {
    filterRef.current = filter;
  }, [filter]);

  useEffect(() => () => clearTimeout(timeoutRef.current), []);

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const newQuery = event.target.value;
    setQuery(newQuery);
    clearTimeout(timeoutRef.current);
    if (newQuery) {
      timeoutRef.current = setTimeout(() => filterRef.current(newQuery), FILTER_DELAY_MS);
    } else {
      filter(newQuery);
    }
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    clearTimeout(timeoutRef.current);
    filter(query);
  };

  return (
    <form className={classNames("input-field is-flex is-align-items-center", className)} onSubmit={handleSubmit}>
      <Label htmlFor={inputId} className="mr-2">
        {label || t("filterEntries")}
      </Label>
      <div className="control has-icons-left">
        <Input
          id={inputId}
          type="search"
          value={query}
          onChange={handleChange}
          autoFocus={autoFocus}
          testId={testId}
        />
        <span className="icon is-small is-left">
          <i className="fas fa-filter" aria-hidden />
        </span>
      </div>
    </form>
  );
};

export default FilterInput;
