//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import RepositoryDetailTable from "./RepositoryDetailTable";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import Changesets from "../../changesets/components/Changesets";

type Props = {
  repository: Repository
};

class RepositoryDetails extends React.Component<Props> {
  render() {
    const { repository } = this.props;
    return (
      <div>
        <RepositoryDetailTable repository={repository} />
        <div className="content">
          <ExtensionPoint
            name="repos.repository-details.information"
            renderAll={true}
            props={{ repository }}
          />
          <Changesets repository={repository}/>
        </div>
      </div>
    );
  }
}

export default RepositoryDetails;
