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
