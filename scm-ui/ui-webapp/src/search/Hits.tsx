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
