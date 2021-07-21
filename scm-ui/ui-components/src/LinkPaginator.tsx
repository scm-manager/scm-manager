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
import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { PagedCollection } from "@scm-manager/ui-types";
import { Button } from "./buttons";

type Props = {
  collection: PagedCollection;
  page: number;
  filter?: string;
};

const LinkPaginator: FC<Props> = ({ collection, page, filter }) => {
  const [t] = useTranslation("commons");
  const addFilterToLink = (link: string) => {
    if (filter) {
      return `${link}?q=${filter}`;
    }
    return link;
  };

  const renderFirstButton = () => {
    return <Button className="pagination-link" label={"1"} disabled={false} link={addFilterToLink("1")} />;
  };

  const renderPreviousButton = (className: string, label?: string) => {
    const previousPage = page - 1;

    return (
      <Button
        className={className}
        label={label ? label : previousPage.toString()}
        disabled={!hasLink("prev")}
        link={addFilterToLink(`${previousPage}`)}
      />
    );
  };

  const hasLink = (name: string) => {
    return collection._links[name];
  };

  const renderNextButton = (className: string, label?: string) => {
    const nextPage = page + 1;
    return (
      <Button
        className={className}
        label={label ? label : nextPage.toString()}
        disabled={!hasLink("next")}
        link={addFilterToLink(`${nextPage}`)}
      />
    );
  };

  const renderLastButton = () => {
    return (
      <Button
        className="pagination-link"
        label={`${collection.pageTotal}`}
        disabled={false}
        link={addFilterToLink(`${collection.pageTotal}`)}
      />
    );
  };

  const separator = () => {
    return <span className="pagination-ellipsis">&hellip;</span>;
  };

  const currentPage = (page: number) => {
    return <Button className="pagination-link is-current" label={"" + page} disabled={true} />;
  };

  const pageLinks = () => {
    const links = [];
    const page = collection.page + 1;
    const pageTotal = collection.pageTotal;
    if (page > 1) {
      links.push(renderFirstButton());
    }
    if (page > 3) {
      links.push(separator());
    }
    if (page > 2) {
      links.push(renderPreviousButton("pagination-link"));
    }

    links.push(currentPage(page));

    if (page + 1 < pageTotal) {
      links.push(renderNextButton("pagination-link"));
    }
    if (page + 2 < pageTotal) links.push(separator());
    //if there exists pages between next and last
    if (page < pageTotal) {
      links.push(renderLastButton());
    }
    return links;
  };

  if (!collection) {
    return null;
  }

  return (
    <nav className="pagination is-centered" aria-label="pagination">
      {renderPreviousButton("pagination-previous", t("paginator.previous"))}
      <ul className="pagination-list">
        {pageLinks().map((link, index) => (
          <li key={index}>{link}</li>
        ))}
      </ul>
      {renderNextButton("pagination-next", t("paginator.next"))}
    </nav>
  );
};

export default LinkPaginator;
