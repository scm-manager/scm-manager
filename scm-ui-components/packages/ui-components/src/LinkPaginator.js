//@flow
import React from "react";
import {translate} from "react-i18next";
import type {PagedCollection} from "@scm-manager/ui-types";
import {Button} from "./buttons";
import {withRouter} from "react-router-dom";

type Props = {
  collection: PagedCollection,
  t: string => string,
  match: any
};

class LinkPaginator extends React.Component<Props> {
  //TODO: HATEOAS-Links verwenden

  renderFirstButton() {
    return (
      <Button
        className={"pagination-link"}
        label={"1"}
        disabled={false}
        link={"1"}
      />
    );
  }

  renderPreviousButton(label?: string) {
    const { match } = this.props;
    const page = parseInt(match.params.page) || 1;
    const previousPage = page - 1;

    return (
      <Button
        className={"pagination-previous"}
        label={label ? label : previousPage.toString()}
        disabled={previousPage < 1}
        link={`${previousPage}`}
      />
    );
  }

  renderNextButton(label?: string) {
    const { match, collection } = this.props;
    let page = parseInt(match.params.page) || 1;

    const nextPage = page + 1;
    return (
      <Button
        className={"pagination-next"}
        label={label ? label : nextPage.toString()}
        disabled={nextPage >= collection.pageTotal + 1}
        link={`${nextPage}`}
      />
    );
  }

  renderLastButton() {
    const { collection } = this.props;
    return (
      <Button
        className={"pagination-link"}
        label={`${collection.pageTotal}`}
        disabled={false}
        link={`${collection.pageTotal}`}
      />
    );
  }

  separator() {
    return <span className="pagination-ellipsis">&hellip;</span>;
  }

  currentPage(page: number) {
    return (
      <Button
        className="pagination-link is-current"
        label={page}
        disabled={true}
      />
    );
  }

  pageLinks() {
    const { collection } = this.props;

    const links = [];
    const page = collection.page + 1;
    const pageTotal = collection.pageTotal;
    if (page > 1) {
      links.push(this.renderFirstButton());
    }
    if (page > 3) {
      links.push(this.separator());
    }
    if (page > 2) {
      links.push(this.renderPreviousButton());
    }

    links.push(this.currentPage(page));

    if (page + 1 < pageTotal) {
      links.push(this.renderNextButton());
    }
    if (page + 2 < pageTotal)
      //if there exists pages between next and last
      links.push(this.separator());
    if (page < pageTotal) {
      links.push(this.renderLastButton());
    }

    return links;
  }

  render() {
    const { t } = this.props;
    return (
      <nav className="pagination is-centered" aria-label="pagination">
        {this.renderPreviousButton(t("paginator.previous"))}
        <ul className="pagination-list">
          {this.pageLinks().map((link, index) => {
            return <li key={index}>{link}</li>;
          })}
        </ul>
        {this.renderNextButton(t("paginator.next"))}
      </nav>
    );
  }
}

export default withRouter(translate("commons")(LinkPaginator));
