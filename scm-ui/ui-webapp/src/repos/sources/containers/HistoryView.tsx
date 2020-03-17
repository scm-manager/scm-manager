/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { Changeset, File, PagedCollection, Repository, Link } from "@scm-manager/ui-types";
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
    if (file) {
      this.updateHistory((file._links.history as Link).href);
    }
  }

  componentDidUpdate() {
    const { file } = this.props;
    const { currentRevision } = this.state;
    if (file?.revision !== currentRevision) {
      this.updateHistory((file._links.history as Link).href);
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
    this.updateHistory((file._links.history as Link).href + "?page=" + internalPage.toString());
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
