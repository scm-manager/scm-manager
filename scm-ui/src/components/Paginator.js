//@flow
import React from "react";
import { translate } from "react-i18next";
import type { PagedCollection } from "../types/Collection";
import { Button } from "./buttons";

type Props = {
  collection: PagedCollection,
  onPageChange: string => void,
  t: string => string
};

class Paginator extends React.Component<Props> {
  isLinkUnavailable(linkType: string) {
    return !this.props.collection || !this.props.collection._links[linkType];
  }

  createAction = (linkType: string) => () => {
    const { collection, onPageChange } = this.props;
    const link = collection._links[linkType].href;
    onPageChange(link);
  };

  renderFirstButton() {
    return this.renderPageButton(1, "first");
  }

  renderPreviousButton() {
    const { t } = this.props;
    return this.renderButton(
      "pagination-previous",
      t("paginator.previous"),
      "prev"
    );
  }

  renderNextButton() {
    const { t } = this.props;
    return this.renderButton("pagination-next", t("paginator.next"), "next");
  }

  renderLastButton() {
    const { collection } = this.props;
    return this.renderPageButton(collection.pageTotal, "last");
  }

  renderPageButton(page: number, linkType: string) {
    return this.renderButton("pagination-link", page.toString(), linkType);
  }

  renderButton(className: string, label: string, linkType: string) {
    return (
      <Button
        className={className}
        label={label}
        disabled={this.isLinkUnavailable(linkType)}
        action={this.createAction(linkType)}
      />
    );
  }

  seperator() {
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
      links.push(this.seperator());
    }
    if (page > 2) {
      links.push(this.renderPageButton(page - 1, "prev"));
    }

    links.push(this.currentPage(page));

    if (page + 1 < pageTotal) {
      links.push(this.renderPageButton(page + 1, "next"));
      links.push(this.seperator());
    }
    if (page < pageTotal) {
      links.push(this.renderLastButton());
    }

    return links;
  }

  render() {
    return (
      <nav className="pagination is-centered" aria-label="pagination">
        {this.renderPreviousButton()}
        {this.renderNextButton()}
        <ul className="pagination-list">
          {this.pageLinks().map((link, index) => {
            return <li key={index}>{link}</li>;
          })}
        </ul>
      </nav>
    );
  }
}

export default translate("commons")(Paginator);
