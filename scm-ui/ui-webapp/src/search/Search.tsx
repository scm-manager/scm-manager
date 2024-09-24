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

import React, { FC, useEffect, useState } from "react";
import {
  CustomQueryFlexWrappedColumns,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  Tag,
  urls,
} from "@scm-manager/ui-components";
import { Notification, Level } from "@scm-manager/ui-core";
import { Link, useLocation, useParams } from "react-router-dom";
import { useIndex, useNamespaceAndNameContext, useSearch, useSearchCounts, useSearchTypes } from "@scm-manager/ui-api";
import Results from "./Results";
import { Trans, useTranslation } from "react-i18next";
import SearchErrorNotification from "./SearchErrorNotification";
import SyntaxModal from "./SyntaxModal";
import type { TFunction } from "i18next";
import styled from "styled-components";
import OmniSearch from "../containers/OmniSearch";
import { Links, QueryResult } from "@scm-manager/ui-types";

const DisabledNavLink = styled.div`
  opacity: 0.4;
  cursor: not-allowed;
`;

const OmniSearchWrapper = styled.div`
  .omni-search-bar {
    width: 100% !important;
    font-size: 1rem !important;
  }

  .icon {
    font-size: 1rem !important;
  }

  .navbar-item {
    padding: 0 !important;
  }

  .dropdown {
    width: 100% !important;
  }

  .dropdown-trigger {
    width: 100% !important;
  }

  .control {
    width: 100% !important;
  }

  .dropdown-menu {
    width: 100% !important;
    max-width: none !important;
  }
`;

type PathParams = {
  type: string;
  page: string;
  namespace: string;
  name: string;
};

type CountProps = {
  isLoading: boolean;
  isSelected: boolean;
  count?: number;
};

const Count: FC<CountProps> = ({ isLoading, isSelected, count }) => {
  if (isLoading) {
    return <span className="small-loading-spinner" />;
  }
  return (
    <Tag rounded={true} color={isSelected ? "info" : "light"}>
      {count}
    </Tag>
  );
};

const usePageParams = () => {
  const location = useLocation();
  const { type: selectedType, ...params } = useParams<PathParams>();
  const page = urls.getPageFromMatch({ params });
  const query = urls.getQueryStringFromLocation(location) || "";
  const namespace = urls.getValueStringFromLocationByKey(location, "namespace");
  const name = urls.getValueStringFromLocationByKey(location, "name");
  return {
    page,
    selectedType,
    query,
    namespace,
    name,
  };
};

export const orderTypes = (t: TFunction) => (a: string, b: string) => {
  if (!a || !b) {
    return 0;
  }
  if (a === "repository" && b !== "repository") {
    return -1;
  } else if (a !== "repository" && b === "repository") {
    return 1;
  }
  return t(`plugins:search.types.${a}.navItem`, a)?.localeCompare(t(`plugins:search.types.${b}.navItem`, b)) ?? 0;
};

type Props = {
  selectedType: string;
  queryResult?: QueryResult;
  links: Links;
};

const SyntaxHelpLink: FC = ({ children }) => <Link to="/help/search-syntax">{children}</Link>;

const SearchSubTitle: FC<Props> = ({ selectedType, queryResult, links }) => {
  const [t] = useTranslation("commons");
  const context = useNamespaceAndNameContext();
  return (
    <>
      {context.namespace
        ? t("search.subtitleWithContext", {
            type: t(`plugins:search.types.${selectedType}.subtitle`, selectedType),
            context: `${context.namespace}/${context.name ?? ""}`,
          })
        : t("search.subtitle", {
            type: t(`plugins:search.types.${selectedType}.subtitle`, selectedType),
          })}
      {queryResult && t("search.withQueryType", { queryType: t(`search.queryTypes.${queryResult.queryType}`) })}
      <br />
      <Trans i18nKey="search.syntaxHelp" components={[<SyntaxHelpLink key="syntaxHelpLink" />]} />
      <OmniSearchWrapper className={"mt-4 mb-2"}>
        <OmniSearch links={links} shouldClear={false} ariaId={"searchPage"} selectedType={selectedType} />
      </OmniSearchWrapper>
    </>
  );
};

const InvalidSearch: FC = () => {
  const [t] = useTranslation("commons");
  return <Notification type="warning">{t("search.invalid")}</Notification>;
};

const Search: FC = () => {
  const { data: index } = useIndex();
  const [t] = useTranslation(["commons", "plugins"]);
  const [showHelp, setShowHelp] = useState(false);
  const { query, selectedType, page, namespace, name } = usePageParams();
  const context = useNamespaceAndNameContext();
  useEffect(() => {
    context.setNamespace(namespace || "");
    context.setName(name || "");

    return () => {
      context.setNamespace("");
      context.setName("");
    };
  }, [namespace, name, context]);
  const searchOptions = {
    type: selectedType,
    page: page - 1,
    pageSize: 25,
    namespaceContext: namespace,
    repositoryNameContext: name,
  };
  const { data, isLoading, error } = useSearch(query, searchOptions);
  const types = useSearchTypes(searchOptions);
  types.sort(orderTypes(t));

  const searchCounts = useSearchCounts(
    types.filter((type) => type !== selectedType),
    query,
    searchOptions
  );
  const counts = {
    [selectedType]: {
      isLoading,
      error,
      data: data?.totalHits,
    },
    ...searchCounts,
  };

  const contextQuery = `${encodeURIComponent(query)}${namespace ? "&namespace=" + namespace : ""}${
    name ? "&name=" + name : ""
  }`;

  return (
    <Page
      title={t("search.title")}
      subtitle={<SearchSubTitle queryResult={data} selectedType={selectedType} links={index?._links || {}} />}
      loading={isLoading}
    >
      {showHelp ? <SyntaxModal close={() => setShowHelp(false)} /> : null}
      <SearchErrorNotification error={error} showHelp={() => setShowHelp(true)} />
      {data ? (
        <CustomQueryFlexWrappedColumns>
          <PrimaryContentColumn>
            <Results result={data} query={contextQuery} page={page} type={selectedType} />
          </PrimaryContentColumn>
          <SecondaryNavigation label={t("search.types")} collapsible={false}>
            {types.map((type) =>
              type !== selectedType && (counts[type].isLoading || counts[type].data === 0) ? (
                <li key={type}>
                  <DisabledNavLink className="p-4 is-unselectable">
                    <Level
                      left={t(`plugins:search.types.${type}.navItem`, type)}
                      right={
                        <Count
                          isLoading={counts[type].isLoading}
                          isSelected={type === selectedType}
                          count={counts[type].data}
                        />
                      }
                    />
                  </DisabledNavLink>
                </li>
              ) : (
                <NavLink key={type} to={`/search/${type}/?q=${contextQuery}`} label={type} activeOnlyWhenExact={false}>
                  <Level
                    left={t(`plugins:search.types.${type}.navItem`, type)}
                    right={
                      <Count
                        isLoading={counts[type].isLoading}
                        isSelected={type === selectedType}
                        count={counts[type].data}
                      />
                    }
                  />
                </NavLink>
              )
            )}
          </SecondaryNavigation>
        </CustomQueryFlexWrappedColumns>
      ) : (
        <InvalidSearch />
      )}
    </Page>
  );
};

export default Search;
