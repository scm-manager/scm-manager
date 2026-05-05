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

import React, { FC, ReactNode } from "react";
import styled from "styled-components";
import { useLocation, useParams } from "react-router-dom";
import { devices, urls } from "@scm-manager/ui-components";
import CodeViewSwitcher, { SwitchViewLink } from "./CodeViewSwitcher";
import CompareSelector from "../../compare/CompareSelector";
import { useRepositoryFromParams } from "@scm-manager/ui-core";
import { CompareTypes } from "../../compare/CompareSelectBar";

const ActionBar = styled.div`
  border: 1px solid var(--scm-border-color);
  border-radius: 4px;
  font-size: 1.25em;
  font-weight: 300;
  line-height: 1.25;
  padding: 0.5em 0.75em;
  margin-bottom: 1em;
  @media screen and (max-width: ${devices.tablet.width}-1px) {
    padding: 0.5rem 0.25rem;
  }
`;

type Props = {
  onSelect: (type: CompareTypes, name: string) => void;
  switchViewLink: SwitchViewLink;
  actions?: ReactNode;
  hasContent?: boolean;
};

const CodeActionBar: FC<Props> = ({ onSelect, switchViewLink, actions, hasContent }) => {
  const location = useLocation();
  const repository = useRepositoryFromParams();
  const params = useParams<{
    sourceName: string;
    "*": string;
  }>();

  return (
    <ActionBar className="has-background-secondary-less">
      <div className="is-flex is-align-items-center is-justify-content-space-between is-flex-wrap-wrap has-gap-2">
        {hasContent && repository.type !== "svn" && (
          <CompareSelector onSelect={onSelect} selected={params?.sourceName} label={null} repository={repository} />
        )}
        <div className="is-flex is-align-items-center ml-auto">
          {actions}
          <CodeViewSwitcher currentUrl={urls.escapeUrlForRoute(location.pathname)} switchViewLink={switchViewLink} />
        </div>
      </div>
    </ActionBar>
  );
};

export default CodeActionBar;
