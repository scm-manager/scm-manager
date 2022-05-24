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
import React from "react";
import styled from "styled-components";
import { Repository, Link } from "@scm-manager/ui-types";
import { ButtonAddons, Button } from "@scm-manager/ui-components";
import CloneInformation from "./CloneInformation";

const Switcher = styled(ButtonAddons)`
  position: absolute;
  top: 0;
  right: 0;
`;

const SmallButton = styled(Button).attrs((props) => ({
  className: "is-small",
}))`
  height: inherit;
`;

type Props = {
  repository: Repository;
};

type State = {
  selected?: Link;
};

function selectHttpOrFirst(repository: Repository) {
  const protocols = (repository._links["protocol"] as Link[]) || [];

  for (const protocol of protocols) {
    if (protocol.name === "http") {
      return protocol;
    }
  }

  if (protocols.length > 0) {
    return protocols[0];
  }
  return undefined;
}

export default class ProtocolInformation extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      selected: selectHttpOrFirst(props.repository),
    };
  }

  selectProtocol = (protocol: Link) => {
    this.setState({
      selected: protocol,
    });
  };

  renderProtocolButton = (protocol: Link) => {
    const name = protocol.name || "unknown";

    let color;

    const { selected } = this.state;
    if (selected && protocol.name === selected.name) {
      color = "link is-selected";
    }

    return (
      <SmallButton color={color} action={() => this.selectProtocol(protocol)}>
        {name.toUpperCase()}
      </SmallButton>
    );
  };

  render() {
    const { repository } = this.props;

    const protocols = repository._links["protocol"] as Link[];
    if (!protocols || protocols.length === 0) {
      return null;
    }

    if (protocols.length === 1) {
      return <CloneInformation url={protocols[0].href} repository={repository} />;
    }

    const { selected } = this.state;
    let cloneInformation = null;
    if (selected) {
      cloneInformation = <CloneInformation repository={repository} url={selected.href} />;
    }

    return (
      <div className="content is-relative">
        <Switcher>{protocols.map(this.renderProtocolButton)}</Switcher>
        {cloneInformation}
      </div>
    );
  }
}
