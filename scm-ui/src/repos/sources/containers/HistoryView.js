// @flow
import React from "react";
import type {
  File,
  Changeset,
  Repository,
  PagedCollection
} from "@scm-manager/ui-types";
import {
  ErrorNotification,
  Loading,
  LinkPaginator
} from "@scm-manager/ui-components";
import { getHistory } from "./history";
import ChangesetList from "../../components/changesets/ChangesetList";

type Props = {
  file: File,
  repository: Repository
};

type State = {
  loaded: boolean,
  changesets: Changeset[],
  page: number,
  pageCollection?: PagedCollection,
  error?: Error
};

class HistoryView extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      loaded: false,
      page: 0,
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
            changesets: result.changesets,
            pageCollection: result.pageCollection
          });
        }
      })
      .catch(err => {});
  }

  showHistory() {
    const { repository } = this.props;
    const { changesets, page, pageCollection } = this.state;
    return (
      <>
        <ChangesetList repository={repository} changesets={changesets} />
        <LinkPaginator page={page} collection={pageCollection} />
      </>
    );
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

export default HistoryView;
