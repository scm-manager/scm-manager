//@flow
import React from "react";
import { repositories } from "@scm-manager/ui-components";
import type { Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository
}

class ProtocolInformation extends React.Component<Props> {

  render() {
    const { repository } = this.props;
    const href = repositories.getProtocolLinkByType(repository, "http");
    if (!href) {
      return null;
    }
    return (
      <div>
        <h4>Checkout the repository</h4>
        <pre>
          <code>svn checkout {href}</code>
        </pre>
      </div>
    );
  }

}

export default ProtocolInformation;
