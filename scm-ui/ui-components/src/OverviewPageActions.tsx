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

import React, { FC, useState } from "react";
import { useHistory, useLocation } from "react-router-dom";
import classNames from "classnames";
import { Button, urls } from "./index";
import { FilterInput, Select } from "./forms";

type Props = {
  showCreateButton: boolean;
  currentGroup: string;
  groups?: string[];
  link: string;
  createLink?: string;
  groupSelected: (namespace: string) => void;
  label?: string;
  testId?: string;
  searchPlaceholder?: string;
  groupAriaLabelledby?: string;
};

const createAbsoluteLink = (url: string) => {
  return urls.withStartingSlash(urls.withEndingSlash(url));
};

const OverviewPageActions: FC<Props> = ({
  groups,
  currentGroup,
  showCreateButton,
  link: inputLink,
  createLink,
  groupSelected,
  label,
  testId,
  searchPlaceholder,
  groupAriaLabelledby
}) => {
  const history = useHistory();
  const location = useLocation();
  const [filterValue, setFilterValue] = useState(urls.getQueryStringFromLocation(location) || "");
  const link = createAbsoluteLink(inputLink);

  const groupSelector = groups && (
    <div className="column is-flex">
      <Select
        ariaLabelledby={groupAriaLabelledby}
        className="is-fullwidth"
        options={groups.map(g => ({ value: g, label: g }))}
        value={currentGroup}
        onChange={groupSelected}
      />
    </div>
  );

  const renderCreateButton = () => {
    if (showCreateButton) {
      return (
        <div className={classNames("input-button", "control", "column")}>
          <Button label={label} link={createLink || `${link}create/`} color="primary" />
        </div>
      );
    }
    return null;
  };

  const filter = (q: string) => {
    if (q !== filterValue) {
      setFilterValue(q);
      history.push(`${link}?q=${q}`);
    }
  };

  return (
    <div className="columns is-tablet">
      {groupSelector}
      <div className="column">
        <FilterInput
          placeholder={searchPlaceholder}
          value={urls.getQueryStringFromLocation(location)}
          filter={filter}
          testId={testId + "-filter"}
        />
      </div>
      {renderCreateButton()}
    </div>
  );
};

export default OverviewPageActions;
