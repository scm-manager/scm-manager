//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository
}

class ProtocolInformation extends React.Component<Props> {

  render() {
    const { repository } = this.props;
    if (!repository._links.httpProtocol) {
      return null;
    }
    return (
      <div>
        <h4>Checkout the repository</h4>
        <pre>
          <code>svn checkout {repository._links.httpProtocol.href}</code>
        </pre>
      </div>
    );
  }

}

export default ProtocolInformation;
