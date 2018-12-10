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
  StatePaginator,
  ChangesetList
} from "@scm-manager/ui-components";
import { getHistory } from "./history";

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
      page: 1,
      changesets: []
    };
  }

  componentDidMount() {
    const { file } = this.props;
    this.updateHistory(file._links.history.href);
  }

  updateHistory(link: string) {
    getHistory(link)
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
            pageCollection: result.pageCollection,
            page: result.pageCollection.page
          });
        }
      })
      .catch(err => {});
  }

  updatePage(page: number) {
    const { file } = this.props;
    const internalPage = page - 1;
    this.updateHistory(
      file._links.history.href + "?page=" + internalPage.toString()
    );
  }

  showHistory() {
    const { repository } = this.props;
    const { changesets, page, pageCollection } = this.state;
    const currentPage = page + 1;
    return (
      <>
        <ChangesetList repository={repository} changesets={changesets} />
        <StatePaginator
          page={currentPage}
          collection={pageCollection}
          updatePage={(newPage: number) => this.updatePage(newPage)}
        />
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
