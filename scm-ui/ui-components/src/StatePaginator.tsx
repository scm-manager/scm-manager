/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { PagedCollection } from "@scm-manager/ui-types";
import { Button } from "./index";

type Props = WithTranslation & {
  collection: PagedCollection;
  page: number;
  updatePage: (p: number) => void;
};

class StatePaginator extends React.Component<Props> {
  renderFirstButton() {
    return <Button className="pagination-link" label={"1"} disabled={false} action={() => this.updateCurrentPage(1)} />;
  }

  updateCurrentPage = (newPage: number) => {
    this.props.updatePage(newPage);
  };

  renderPreviousButton(label?: string) {
    const { page } = this.props;
    const previousPage = page - 1;

    return (
      <Button
        className="pagination-previous"
        label={label ? label : previousPage.toString()}
        disabled={!this.hasLink("prev")}
        action={() => this.updateCurrentPage(previousPage)}
      />
    );
  }

  hasLink(name: string) {
    const { collection } = this.props;
    return collection._links[name];
  }

  renderNextButton(label?: string) {
    const { page } = this.props;
    const nextPage = page + 1;
    return (
      <Button
        className="pagination-next"
        label={label ? label : nextPage.toString()}
        disabled={!this.hasLink("next")}
        action={() => this.updateCurrentPage(nextPage)}
      />
    );
  }

  renderLastButton() {
    const { collection } = this.props;
    return (
      <Button
        className="pagination-link"
        label={`${collection.pageTotal}`}
        disabled={false}
        action={() => this.updateCurrentPage(collection.pageTotal)}
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
        label={"" + page}
        disabled={true}
        action={() => this.updateCurrentPage(page)}
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
    if (page + 2 < pageTotal) links.push(this.separator());
    //if there exists pages between next and last
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
export default withTranslation("commons")(StatePaginator);
