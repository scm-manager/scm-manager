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
