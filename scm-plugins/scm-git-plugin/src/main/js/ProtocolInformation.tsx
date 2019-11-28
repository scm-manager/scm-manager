import React from "react";
import styled from "styled-components";
import { Repository, Link } from "@scm-manager/ui-types";
import { ButtonAddons, Button } from "@scm-manager/ui-components";
import CloneInformation from "./CloneInformation";

const Wrapper = styled.div`
  position: relative;
`;

const Switcher = styled(ButtonAddons)`
  position: absolute;
  top: 0;
  right: 0;
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
      selected: selectHttpOrFirst(props.repository)
    };
  }

  selectProtocol = (protocol: Link) => {
    this.setState({
      selected: protocol
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
      <Button color={color} action={() => this.selectProtocol(protocol)}>
        {name.toUpperCase()}
      </Button>
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
      <Wrapper>
        <Switcher>{protocols.map(this.renderProtocolButton)}</Switcher>
        {cloneInformation}
      </Wrapper>
    );
  }
}
