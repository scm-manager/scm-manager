// @flow
import React from "react";
import type {
  File,
  Changeset,
  Repository,
  PagedCollection
} from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import { getHistory } from "./history";
import ChangesetList from "../../components/changesets/ChangesetList";
import StatePaginator from "../components/content/StatePaginator";

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

    this.updateHistory(file._links.history.href + "?page=" + page.toString());
  }

  showHistory() {
    const { repository } = this.props;
    const { changesets, page, pageCollection } = this.state;
    const currentPage = page == 0 ? 1 : page;
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
    console.log(this.state);
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
