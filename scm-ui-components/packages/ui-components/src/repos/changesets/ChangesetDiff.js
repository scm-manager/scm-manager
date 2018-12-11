//@flow
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import { apiClient } from "../../apiclient";
import ErrorNotification from "../../ErrorNotification";
import Loading from "../../Loading";
import Diff from "../Diff";

type Props = {
  changeset: Changeset
};

type State = {
  diff?: string,
  loading: boolean,
  error?: Error
};

class ChangesetDiff extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      loading: false
    };
  }

  isDiffSupported(changeset: Changeset) {
    return !!changeset._links.diff;
  }

  createUrl(changeset: Changeset) {
    return changeset._links.diff.href + "?format=GIT";
  }

  loadDiff(changeset: Changeset) {
    this.setState({
      loading: true
    });
    const url = this.createUrl(changeset);
    apiClient
      .get(url)
      .then(response => response.text())
      .then(text => {
        this.setState({
          loading: false,
          diff: text
        });
      })
      .catch(error => {
        this.setState({
          loading: false,
          error
        });
      });
  }

  componentDidMount() {
    const { changeset } = this.props;
    if (!this.isDiffSupported(changeset)) {
      this.setState({
        error: new Error("diff is not supported")
      });
    } else {
      this.loadDiff(changeset);
    }
  }


  render() {
    const { diff, loading, error } = this.state;
    if (error) {
      return <ErrorNotification error={error} />;
    } else if (loading || !diff) {
      return <Loading />;
    } else {
      return <Diff diff={diff} />;
    }
  }

}

export default ChangesetDiff;
