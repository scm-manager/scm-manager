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
import { WithTranslation, withTranslation } from "react-i18next";
import { PagedCollection } from "@scm-manager/ui-types";
import { Button } from "./buttons";

type Props = WithTranslation & {
  collection: PagedCollection;
  page: number;
  filter?: string;
};

class LinkPaginator extends React.Component<Props> {
  addFilterToLink(link: string) {
    const { filter } = this.props;
    if (filter) {
      return `${link}?q=${filter}`;
    }
    return link;
  }

  renderFirstButton() {
    return <Button className="pagination-link" label={"1"} disabled={false} link={this.addFilterToLink("1")} />;
  }

  renderPreviousButton(className: string, label?: string) {
    const { page } = this.props;
    const previousPage = page - 1;

    return (
      <Button
        className={className}
        label={label ? label : previousPage.toString()}
        disabled={!this.hasLink("prev")}
        link={this.addFilterToLink(`${previousPage}`)}
      />
    );
  }

  hasLink(name: string) {
    const { collection } = this.props;
    return collection._links[name];
  }

  renderNextButton(className: string, label?: string) {
    const { page } = this.props;
    const nextPage = page + 1;
    return (
      <Button
        className={className}
        label={label ? label : nextPage.toString()}
        disabled={!this.hasLink("next")}
        link={this.addFilterToLink(`${nextPage}`)}
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
        link={this.addFilterToLink(`${collection.pageTotal}`)}
      />
    );
  }

  separator() {
    return <span className="pagination-ellipsis">&hellip;</span>;
  }

  currentPage(page: number) {
    return <Button className="pagination-link is-current" label={"" + page} disabled={true} />;
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
      links.push(this.renderPreviousButton("pagination-link"));
    }

    links.push(this.currentPage(page));

    if (page + 1 < pageTotal) {
      links.push(this.renderNextButton("pagination-link"));
    }
    if (page + 2 < pageTotal) links.push(this.separator());
    //if there exists pages between next and last
    if (page < pageTotal) {
      links.push(this.renderLastButton());
    }
    return links;
  }
  render() {
    const { collection, t } = this.props;
    if (collection) {
      return (
        <nav className="pagination is-centered" aria-label="pagination">
          {this.renderPreviousButton("pagination-previous", t("paginator.previous"))}
          <ul className="pagination-list">
            {this.pageLinks().map((link, index) => (
              <li key={index}>{link}</li>
            ))}
          </ul>
          {this.renderNextButton("pagination-next", t("paginator.next"))}
        </nav>
      );
    }
    return null;
  }
}
export default withTranslation("commons")(LinkPaginator);
