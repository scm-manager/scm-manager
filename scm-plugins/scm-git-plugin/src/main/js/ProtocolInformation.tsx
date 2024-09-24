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
import classNames from "classnames";
import styled from "styled-components";
import { Repository, Link } from "@scm-manager/ui-types";
import { Button } from "@scm-manager/ui-buttons";
import CloneInformation from "./CloneInformation";

const TopRightContainer = styled.div`
  position: absolute;
  top: 0;
  right: 0;
`;

const SmallButton = styled(Button)`
  height: inherit;
`;

type Props = {
  repository: Repository;
};

type Protocol = Link & {
  profile: string;
};

function selectPreferredProtocolFirst(repository: Repository) {
  const protocols = (repository._links["protocol"] as Protocol[]) || [];

  if (protocols.length === 0) {
    return undefined;
  }

  protocols.sort((a, b) => Number(b.profile) - Number(a.profile));
  return protocols[0];
}

const ProtocolInformation: FC<Props> = ({ repository }) => {
  const [selected, setSelected] = useState<Protocol | undefined>(selectPreferredProtocolFirst(repository));

  const protocols = repository._links["protocol"] as Protocol[];
  if (!protocols || protocols.length === 0) {
    return null;
  }

  if (protocols.length === 1) {
    return <CloneInformation url={protocols[0].href} repository={repository} />;
  }

  let cloneInformation = null;
  if (selected) {
    cloneInformation = <CloneInformation url={selected?.href} repository={repository} />;
  }

  return (
    <div className="content is-relative">
      <TopRightContainer className="field has-addons">
        {protocols.map((protocol, index) => {
          return (
            <div className="control" key={protocol.name || index}>
              <SmallButton
                className={classNames("is-small", {
                  "is-link is-selected": selected && protocol.name === selected.name,
                })}
                onClick={() => setSelected(protocol)}
              >
                {(protocol.name || "unknown").toUpperCase()}
              </SmallButton>
            </div>
          );
        })}
      </TopRightContainer>
      {cloneInformation}
    </div>
  );
};

export default ProtocolInformation;
