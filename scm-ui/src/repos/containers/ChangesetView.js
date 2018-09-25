//@flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import type { Changeset } from "@scm-manager/ui-types";
import { fetchChangesetIfNeeded } from "../modules/changesets";

type Props = {
  id: string,
  changeset: Changeset,
  repository: Repository,
  repositories: Repository[],
  fetchChangesetIfNeeded: (
    namespace: string,
    repoName: string,
    id: string
  ) => void
};

class ChangesetView extends React.Component<State, Props> {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const { fetchChangesetIfNeeded, repository } = this.props;
    const id = this.props.match.params.id;
    //state macht keinen Sinn?! repositories holen!
    fetchChangesetIfNeeded(repository.namespace, repository.name, id);
  }

  render() {
    const id = this.props.match.params.id;

    return <div>Hallo! Changesets here! {id}</div>;
  }
}

const mapStateToProps = (state, ownProps: Props) => {
  return null;
};

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetIfNeeded: (
      namespace: string,
      repoName: string,
      id: string
    ) => {
      dispatch(fetchChangesetIfNeeded(namespace, repoName, id));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(ChangesetView)
);
