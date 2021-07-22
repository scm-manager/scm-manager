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
import { useLocation, useParams } from "react-router-dom";
import { useSearch, useSearchCounts, useSearchTypes } from "@scm-manager/ui-api";
import Results from "./Results";

type PathParams = {
  type: string;
  page: string;
};

type CountProps = {
  isLoading: boolean;
  isSelected: boolean;
  count?: number;
};

const Count: FC<CountProps> = ({ isLoading, isSelected, count }) => {
  if (isLoading) {
    return <span className={"small-loading-spinner"} />;
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
  return {
    page,
    selectedType,
    query,
  };
};

const Search: FC = () => {
  const { query, selectedType, page } = usePageParams();
  const { data, isLoading, error } = useSearch(query, {
    type: selectedType,
    page: page - 1,
    pageSize: 25,
  });
  const types = useSearchTypes();

  const searchCounts = useSearchCounts(
    types.filter((t) => t !== selectedType),
    query
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
    <Page title="Search" subtitle={`${selectedType} results for "${query}"`} loading={isLoading} error={error}>
      {data ? (
        <CustomQueryFlexWrappedColumns>
          <PrimaryContentColumn>
            <Results result={data} query={query} page={page} type={selectedType} />
          </PrimaryContentColumn>
          <SecondaryNavigation label={"Types"} collapsible={false}>
            {types.map((type) => (
              <NavLink key={type} to={`/search/${type}/?q=${query}`} label={type} activeOnlyWhenExact={false}>
                <Level
                  left={type}
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
