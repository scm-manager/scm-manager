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

import React, { FC, useEffect, useState } from "react";
import {
  CustomQueryFlexWrappedColumns,
  Level,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  Tag,
  urls,
} from "@scm-manager/ui-components";
import { Link, useLocation, useParams } from "react-router-dom";
import { useNamespaceAndNameContext, useSearch, useSearchCounts, useSearchTypes } from "@scm-manager/ui-api";
import Results from "./Results";
import { Trans, useTranslation } from "react-i18next";
import SearchErrorNotification from "./SearchErrorNotification";
import SyntaxModal from "./SyntaxModal";
import type { TFunction } from "i18next";

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
  const query = urls.getQueryStringFromLocation(location);
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
  query: string;
};

const SyntaxHelpLink: FC = ({ children }) => <Link to="/help/search-syntax">{children}</Link>;

const SearchSubTitle: FC<Props> = ({ selectedType, query }) => {
  const [t] = useTranslation("commons");
  const context = useNamespaceAndNameContext();
  return (
    <>
      {context.namespace
        ? t("search.subtitleWithContext", {
            query,
            type: t(`plugins:search.types.${selectedType}.subtitle`, selectedType),
            context: `${context.namespace}${context.name ? `/${context.name}` : ""}`,
          })
        : t("search.subtitle", {
            query,
            type: t(`plugins:search.types.${selectedType}.subtitle`, selectedType),
          })}
      <br />
      <Trans i18nKey="search.syntaxHelp" components={[<SyntaxHelpLink />]} />
    </>
  );
};

const Search: FC = () => {
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

  return (
    <Page
      title={t("search.title")}
      subtitle={<SearchSubTitle query={query} selectedType={selectedType} />}
      loading={isLoading}
    >
      {showHelp ? <SyntaxModal close={() => setShowHelp(false)} /> : null}
      <SearchErrorNotification error={error} showHelp={() => setShowHelp(true)} />
      {data ? (
        <CustomQueryFlexWrappedColumns>
          <PrimaryContentColumn>
            <Results result={data} query={query} page={page} type={selectedType} />
          </PrimaryContentColumn>
          <SecondaryNavigation label={t("search.types")} collapsible={false}>
            {types.map((type) => (
              <NavLink
                key={type}
                to={`/search/${type}/?q=${query}${namespace ? "&namespace=" + namespace : ""}${
                  name ? "&name=" + name : ""
                }`}
                label={type}
                activeOnlyWhenExact={false}
              >
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
            ))}
          </SecondaryNavigation>
        </CustomQueryFlexWrappedColumns>
      ) : null}
    </Page>
  );
};

export default Search;
