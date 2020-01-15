import React from "react";
import { Changeset, File, PagedCollection, Repository } from "@scm-manager/ui-types";
import { ChangesetList, ErrorNotification, Loading, StatePaginator } from "@scm-manager/ui-components";
import { getHistory } from "./history";

type Props = {
  file: File;
  repository: Repository;
};

type State = {
  loaded: boolean;
  changesets: Changeset[];
  page: number;
  pageCollection?: PagedCollection;
  error?: Error;
  currentRevision: string;
};

class HistoryView extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      loaded: false,
      page: 1,
      changesets: [],
      currentRevision: ""
    };
  }

  componentDidMount() {
    const { file } = this.props;
    file && this.updateHistory(file._links.history.href);
  }

  componentDidUpdate() {
    const { file } = this.props;
    const { currentRevision } = this.state;
    if (file?.revision !== currentRevision) {
      this.updateHistory(file._links.history.href);
    }
  }

  updateHistory(link: string) {
    const { file } = this.props;
    getHistory(link)
      .then(result => {
        this.setState({
          ...this.state,
          loaded: true,
          changesets: result.changesets,
          pageCollection: result.pageCollection,
          page: result.pageCollection.page,
          currentRevision: file.revision
        });
      })
      .catch(error =>
        this.setState({
          ...this.state,
          error,
          loaded: true
        })
      );
  }

  updatePage(page: number) {
    const { file } = this.props;
    const internalPage = page - 1;
    this.updateHistory(file._links.history.href + "?page=" + internalPage.toString());
  }

  showHistory() {
    const { repository } = this.props;
    const { changesets, page, pageCollection } = this.state;
    const currentPage = page + 1;
    return (
      <>
        <div className="panel-block">
          <ChangesetList repository={repository} changesets={changesets} />
        </div>
        <div className="panel-footer">
          <StatePaginator
            page={currentPage}
            collection={pageCollection}
            updatePage={(newPage: number) => this.updatePage(newPage)}
          />
        </div>
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
