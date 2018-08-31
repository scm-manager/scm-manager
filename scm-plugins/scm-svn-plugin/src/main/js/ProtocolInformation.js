//@flow
import React from 'react';

// TODO flow types ???
type Props = {
  repository: Object
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
