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

import React, { FC } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Repository } from "@scm-manager/ui-types";
import { devices, Icon } from "@scm-manager/ui-components";
import { useDocumentTitle } from "@scm-manager/ui-core";
import CompareSelector from "./CompareSelector";

type Props = {
  repository: Repository;
  baseUrl: string;
};

export type CompareTypes = "branch" | "tag" | "revision";

export type CompareFunction = (type: CompareTypes, name: string) => void;
export type CompareProps = {
  type: CompareTypes;
  name: string;
};
const ResponsiveIcon = styled(Icon)`
  transform: rotate(90deg);
  @media screen and (min-width: ${devices.tablet.width}px) {
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
  const navigate = useNavigate();

  const params = useParams<{
    sourceType: CompareTypes;
    sourceName: string;
    targetType: CompareTypes;
    targetName: string;
    "*": string;
  }>();
  const source: CompareProps = {
    type: params?.sourceType,
    name: params?.sourceName,
  };
  const target: CompareProps = {
    type: params.targetType,
    name: params.targetName || "",
  };

  const trailingPath = params["*"] || "diff/";

  useDocumentTitle(
    t("compare.compareSourceAndTargetWithNamespaceName", {
      sourceType: t(`compare.selector.type.${source.type}`),
      source: source.name,
      targetType: t(`compare.selector.type.${target.type}`),
      target: target.name,
      namespace: repository.namespace,
      name: repository.name,
    }),
  );

  const handleSourceSelect = (type: CompareTypes, name: string) => {
    if (target.type && target.name) {
      const newPath = `/${baseUrl}/${type}/${encodeURIComponent(name)}/${target.type}/${encodeURIComponent(
        target.name,
      )}/${trailingPath}`;
      navigate(newPath);
    }
  };

  const handleTargetSelect = (type: CompareTypes, name: string) => {
    if (source.type && source.name) {
      const newPath = `/${baseUrl}/${source.type}/${encodeURIComponent(source.name)}/${type}/${encodeURIComponent(
        name,
      )}/${trailingPath}`;
      navigate(newPath);
    }
  };

  return (
    <ResponsiveBar className="is-align-items-center has-gap-2">
      <CompareSelector
        repository={repository}
        label={t("compare.selector.source")}
        onSelect={handleSourceSelect}
        selected={source.name}
      />
      <ResponsiveIcon name="arrow-right" className="fa-lg" title={t("compare.selector.with")} />
      <CompareSelector
        repository={repository}
        label={t("compare.selector.target")}
        onSelect={handleTargetSelect}
        selected={target.name}
      />
    </ResponsiveBar>
  );
};

export default CompareSelectBar;
