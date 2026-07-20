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
import { useNavigate, useLocation } from "react-router-dom";
import styled from "styled-components";
import classNames from "classnames";
import { Button, urls } from "./index";
import { FilterInput, Select } from "./forms";
import { devices } from "./devices";

const StyledGroupSelector = styled.div`
  .fieldset-is-fullwidth {
    width: 100%;
  }
`;

const StyledFilterWrapper = styled.div`
  .control {
    min-width: 150px;
    width: 100%;
  }
  
  label, .label:not(:last-child){
    margin-bottom: 0;
  }
  
  @media screen and (max-width: ${devices.tablet.width}px) {
    .column{
       justify-content: center
    }
  }

  @media screen and (min-width: ${devices.tablet.width}px){
    .columns {
      margin-right: initial;
      margin-top: 0;
      margin-bottom: 0;
    }
  }

  @media screen and (min-width: ${devices.tablet.width}px) and (max-width: ${devices.fullhd.width}px) {
    .columns {
      max-width: calc(100%);
    }
    .column{
      justify-content: end;
      margin-bottom: 0 !important;
    }
    .column:last-child:not(.input-button){
      padding-right: 0 !important;
    }
  }
  
  @media screen and (max-width: ${devices.fullhd.width}px) {
    flex-direction: column;
    margin-left: 0 !important;
    max-width: 100%;
  }
`;

type Props = {
  showCreateButton: boolean;
  currentGroup?: string;
  groups?: string[];
  link: string;
  createLink?: string;
  groupSelected?: (namespace: string) => void;
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
  groupAriaLabelledby,
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [filterValue, setFilterValue] = useState(urls.getQueryStringFromLocation(location) || "");
  const link = createAbsoluteLink(inputLink);

  const groupSelector = groups && (
    <StyledGroupSelector className="column is-flex is-align-items-center">
      <Select
        ariaLabelledby={groupAriaLabelledby}
        className="is-fullwidth"
        fieldsetClassName="fieldset-is-fullwidth"
        options={groups.map((g) => ({ value: g, label: g }))}
        value={currentGroup}
        onChange={groupSelected}
      />
    </StyledGroupSelector>
  );

  const renderCreateButton = () => {
    if (showCreateButton) {
      return (
        <div className={classNames("input-button", "control", "column", "is-flex", "is-align-items-center")}>
          <Button label={label} link={createLink || `${link}create/`} color="primary" />
        </div>
      );
    }
    return null;
  };

  const filter = (q: string) => {
    if (q !== filterValue) {
      setFilterValue(q);
      navigate(`${link}?q=${q}`);
    }
  };

  return (
    <StyledFilterWrapper className="columns is-widescreen">
      <div className="columns is-flex-widescreen is-align-items-center">
        {groupSelector}
        <div className="column is-flex is-align-items-center">
          <FilterInput
            placeholder={searchPlaceholder}
            value={urls.getQueryStringFromLocation(location)}
            filter={filter}
            testId={testId + "-filter"}
          />
        </div>
      </div>
      {renderCreateButton()}
    </StyledFilterWrapper>
  );
};

export default OverviewPageActions;
