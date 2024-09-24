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

import React, { FC, useEffect, useState } from "react";
import { useHistory, useLocation, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Repository } from "@scm-manager/ui-types";
import { devices, Icon } from "@scm-manager/ui-components";
import CompareSelector from "./CompareSelector";
import { CompareBranchesParams } from "./CompareView";

type Props = {
  repository: Repository;
  baseUrl: string;
};

export type CompareTypes = "b" | "t" | "r";

export type CompareFunction = (type: CompareTypes, name: string) => void;
export type CompareProps = {
  type: CompareTypes;
  name: string;
};
const ResponsiveIcon = styled(Icon)`
  margin: 1rem 0.5rem;
  transform: rotate(90deg);
  @media screen and (min-width: ${devices.tablet.width}px) {
    margin: 1.5rem 1rem 0;
    transform: rotate(0);
  }
`;

const ResponsiveBar = styled.div`
  @media screen and (min-width: ${devices.tablet.width}px) {
    display: flex;
    justify-content: space-between;
    flex-direction: row;
  }
`;

const CompareSelectBar: FC<Props> = ({ repository, baseUrl }) => {
  const [t] = useTranslation("repos");
  const params = useParams<CompareBranchesParams>();
  const location = useLocation();
  const history = useHistory();
  const [source, setSource] = useState<CompareProps>({
    type: params?.sourceType,
    name: decodeURIComponent(params?.sourceName)
  });
  const [target, setTarget] = useState<CompareProps>({
    type: params?.targetType,
    name: decodeURIComponent(params?.targetName)
  });

  useEffect(() => {
    const tabUriComponent = location.pathname.split("/")[9];
    if (source && target && tabUriComponent) {
      history.push(
        baseUrl +
          "/" +
          source.type +
          "/" +
          encodeURIComponent(source.name) +
          "/" +
          target.type +
          "/" +
          encodeURIComponent(target.name) +
          "/" +
          tabUriComponent +
          "/" +
          location.pathname.split("/")[10] || ""
      );
    }
  }, [history, baseUrl, source, target, location.pathname]);

  return (
    <ResponsiveBar className="is-align-items-center">
      <CompareSelector
        repository={repository}
        label={t("compare.selector.source")}
        onSelect={(type, name) => setSource({ type, name })}
        selected={source}
      />
      <ResponsiveIcon name="arrow-right" className="fa-lg" title={t("compare.selector.with")} />
      <CompareSelector
        repository={repository}
        label={t("compare.selector.target")}
        onSelect={(type, name) => setTarget({ type, name })}
        selected={target}
      />
    </ResponsiveBar>
  );
};

export default CompareSelectBar;
