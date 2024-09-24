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
import { Hit as HitType } from "@scm-manager/ui-types";
import RepositoryHit from "./RepositoryHit";
import GenericHit from "./GenericHit";
import UserHit from "./UserHit";
import GroupHit from "./GroupHit";
import { HitProps } from "@scm-manager/ui-components";
import { Notification } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";

type Props = {
  type: string;
  hits?: HitType[];
};

const hitComponents: { [name: string]: FC<HitProps> } = {
  repository: RepositoryHit,
  user: UserHit,
  group: GroupHit,
};

const findComponent = (type: string) => {
  const cmp = hitComponents[type];
  if (!cmp) {
    return GenericHit;
  }
  return cmp;
};

type HitComponentProps = {
  type: string;
  hit: HitType;
};

const InternalHitRenderer: FC<HitComponentProps> = ({ type, hit }) => {
  const Cmp = findComponent(type);
  return <Cmp hit={hit} />;
};

const HitComponent: FC<HitComponentProps> = ({ hit, type }) => (
  <ExtensionPoint<extensionPoints.SearchHitRenderer> name={`search.hit.${type}.renderer`} props={{ hit }}>
    <InternalHitRenderer type={type} hit={hit} />
  </ExtensionPoint>
);

const NoHits: FC = () => {
  const [t] = useTranslation("commons");
  return <Notification type="info">{t("search.noHits")}</Notification>;
};

const Hits: FC<Props> = ({ type, hits }) => (
  <>
    {hits && hits.length > 0 ? (
      hits.map((hit, c) => <HitComponent key={`${type}_${c}_${hit.score}`} hit={hit} type={type} />)
    ) : (
      <NoHits />
    )}
  </>
);

export default Hits;
