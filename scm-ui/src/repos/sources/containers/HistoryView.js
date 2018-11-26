// @flow
import React from "react";
import type { File, Changeset, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import { getHistory } from "./history";
import ChangesetList from "../../components/changesets/ChangesetList";

type Props = {
  file: File,
  repository: Repository
};

type State = {
  loaded: boolean,
  changesets: Changeset[],
  error?: Error
};

class HistoryView extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      loaded: false,
      changesets: []
    };
  }

  componentDidMount() {
    const { file } = this.props;
    getHistory(file._links.history.href)
      .then(result => {
        if (result.error) {
          this.setState({
            ...this.state,
            error: result.error,
            loaded: true
          });
        } else {
          this.setState({
            ...this.state,
            loaded: true,
            changesets: result.changesets
          });
        }
      })
      .catch(err => {});
  }

  showHistory() {
    const { repository } = this.props;
    const { changesets } = this.state;
    return <ChangesetList repository={repository} changesets={changesets} />;
  }

  render() {
    const { file } = this.props;
    const { loaded, error } = this.state;

    if (!file || !loaded) {
      return <Loading />;
    }
    if (error) {
      return <ErrorNotification error={error} />;
    }

    const history = this.showHistory();

    return <>{history}</>;
  }
}

export default (HistoryView);
