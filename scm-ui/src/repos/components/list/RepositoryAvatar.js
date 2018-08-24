//@flow
import React from "react";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Repository } from "../../types/Repositories";

type Props = {
  repository: Repository
};

class RepositoryAvatar extends React.Component<Props> {
  render() {
    const { repository } = this.props;
    return (
      <p className="image is-64x64">
        <ExtensionPoint name="repos.repository-avatar" props={{ repository }}>
          <img src="/images/blib.jpg" alt="Logo" />
        </ExtensionPoint>
      </p>
    );
  }
}

export default RepositoryAvatar;
