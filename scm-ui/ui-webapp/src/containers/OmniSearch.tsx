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
import React, { FC, Fragment, RefObject, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Hit, Links, Repository, ValueHitField, Option } from "@scm-manager/ui-types";
import styled from "styled-components";
import { useNamespaceAndNameContext, useOmniSearch, useSearchTypes } from "@scm-manager/ui-api";
import classNames from "classnames";
import { useHistory, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { RepositoryAvatar, Icon } from "@scm-manager/ui-components";
import SyntaxModal from "../search/SyntaxModal";
import queryString from "query-string";
import { orderTypes } from "../search/Search";
import { useShortcut } from "@scm-manager/ui-shortcuts";
import { Label, Combobox } from "@scm-manager/ui-forms";
import { Combobox as HeadlessCombobox } from "@headlessui/react";

const ResultHeading = styled.div`
  border-top: 1px solid lightgray;
`;

type Props = {
  shouldClear: boolean;
  ariaId: string;
  nextFocusRef: RefObject<HTMLElement>;
};

type GuardProps = Props & {
  links: Links;
};

const namespaceAndName = (hit: Hit) => {
  const namespace = (hit.fields["namespace"] as ValueHitField).value as string;
  const name = (hit.fields["name"] as ValueHitField).value as string;
  return `${namespace}/${name}`;
};

const AvatarSection: FC<{ repository: Repository }> = ({ repository }) => {
  if (!repository) {
    return null;
  }

  return (
    <span className="mr-2">
      <RepositoryAvatar repository={repository} size={24} />
    </span>
  );
};

const HitEntry: FC<{
  link: string;
  label: string;
  repository?: Repository;
  query: string;
}> = ({ link, label, repository, query }) => {
  const history = useHistory();
  return (
    <HeadlessCombobox.Option
      value={{ label: query, value: () => history.push(link), displayValue: label }}
      key={label}
      as={Fragment}
    >
      {({ active }) => (
        <Combobox.Option isActive={active}>
          <div className="is-flex">
            {repository ? <AvatarSection repository={repository} /> : <Icon name="search" className="mr-2 ml-1 mt-1" />}
            <Label className="has-text-weight-normal is-size-6">{label}</Label>
          </div>
        </Combobox.Option>
      )}
    </HeadlessCombobox.Option>
  );
};

const useDebounce = (value: string, delay: number) => {
  const [debouncedValue, setDebouncedValue] = useState(value);
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);
  return debouncedValue;
};

const useSearchParams = () => {
  const location = useLocation();
  const pathname = location.pathname;

  let searchType = "repository";
  let initialQuery = "";
  if (pathname.startsWith("/search/")) {
    const path = pathname.substring("/search/".length);
    const index = path.indexOf("/");
    if (index > 0) {
      searchType = path.substring(0, index);
    } else {
      searchType = path;
    }

    const queryParams = queryString.parse(location.search);
    const q = queryParams.q;
    if (Array.isArray(q)) {
      initialQuery = q[0] || "";
    } else {
      initialQuery = q || "";
    }
  }

  return {
    searchType,
    initialQuery,
  };
};

const OmniSearch: FC<Props> = ({ shouldClear, nextFocusRef }) => {
  const [t] = useTranslation("commons");
  const { initialQuery } = useSearchParams();
  const [query, setQuery] = useState(initialQuery);
  const [value, setValue] = useState<Option<(() => void) | undefined> | undefined>({ label: query, value: query });
  const searchInputRef = useRef<HTMLInputElement>(null);
  const debouncedQuery = useDebounce(query, 250);
  const [showDropdown, setDropdown] = useState(true);
  const context = useNamespaceAndNameContext();
  const { data, isLoading } = useOmniSearch(debouncedQuery, {
    type: "repository",
    pageSize: 5,
  });
  const [showHelp, setShowHelp] = useState(false);
  const handleChange = useCallback((value: Option<(() => void) | undefined>) => {
    setValue(value);
    value.value?.();
    setDropdown(true);
  }, []);

  useEffect(() => {
    setQuery(shouldClear ? "" : initialQuery);
    setValue(shouldClear ? { label: "", value: undefined } : { label: initialQuery, value: undefined });
  }, [shouldClear, initialQuery]);

  const clearInput = () => {
    shouldClear = true;
    setValue({ label: "", value: undefined });
    setQuery("");
    setDropdown(false);
  };

  const openHelp = () => setShowHelp(true);

  const closeHelp = () => setShowHelp(false);

  const hits = data?._embedded?.hits || [];
  const searchTypes = useSearchTypes({
    type: "",
    namespaceContext: context.namespace || "",
    repositoryNameContext: context.name || "",
  });
  searchTypes.sort(orderTypes(t));

  const id = useCallback(namespaceAndName, []);
  useShortcut("/", () => searchInputRef.current?.focus(), {
    description: t("shortcuts.search"),
  });

  const entries = useMemo(() => {
    const newEntries = [];

    if (context.namespace && context.name && searchTypes.length > 0) {
      newEntries.push(
        <HitEntry
          key="search.quickSearch.searchRepo"
          label={t("search.quickSearch.searchRepo")}
          link={`/search/${searchTypes[0]}/?q=${encodeURIComponent(query)}&namespace=${context.namespace}&name=${
            context.name
          }`}
          query={query}
        />
      );
    }
    if (context.namespace) {
      newEntries.push(
        <HitEntry
          key="search.quickSearch.searchNamespace"
          label={t("search.quickSearch.searchNamespace")}
          link={`/search/repository/?q=${encodeURIComponent(query)}&namespace=${context.namespace}`}
          query={query}
        />
      );
    }
    newEntries.push(
      <HitEntry
        key="search.quickSearch.searchEverywhere"
        label={t("search.quickSearch.searchEverywhere")}
        link={`/search/repository/?q=${encodeURIComponent(query)}`}
        query={query}
      />
    );
    hits?.forEach((hit, idx) => {
      newEntries.push(
        <HitEntry
          key={`search.quickSearch.hit${idx}`}
          label={id(hit)}
          link={`/repo/${id(hit)}`}
          repository={hit._embedded?.repository}
          query={query}
        />
      );
    });
    return newEntries;
  }, [context.name, context.namespace, hits, id, query, searchTypes, t]);
  return (
    <div className={classNames("navbar-item", "field", "mb-0")}>
      {showHelp ? <SyntaxModal close={closeHelp} /> : null}
      <div
        className={classNames("control", "has-icons-right", {
          "is-loading": isLoading,
        })}
      >
        <Combobox
          className="input is-small"
          placeholder={t("search.placeholder")}
          value={value}
          onChange={handleChange}
          ref={searchInputRef}
          onQueryChange={setQuery}
          onKeyDown={(e) => {
            // This is hacky but it seems to be one of the only solutions right now
            if (e.key === "Tab") {
              nextFocusRef?.current?.focus();
              e.preventDefault();
              clearInput();
              searchInputRef.current.value = "";
            } else {
              setDropdown(true);
            }
          }}
        >
          {showDropdown ? entries : null}
          {showDropdown ? (
            <HeadlessCombobox.Option
              value={{ label: query, value: openHelp, displayValue: query }}
              key={query}
              as={Fragment}
            >
              {({ active }) => (
                <ResultHeading>
                  <Combobox.Option isActive={active}>
                    <div className=" is-flex">
                      <Icon name="question-circle" color="blue-light" className="pt-1 pl-1"></Icon>
                      <Label className="has-text-weight-normal pl-3">{t("search.quickSearch.resultHeading")}</Label>
                    </div>
                  </Combobox.Option>
                </ResultHeading>
              )}
            </HeadlessCombobox.Option>
          ) : null}
        </Combobox>
      </div>
    </div>
  );
};

const OmniSearchGuard: FC<GuardProps> = ({ links, ...props }) => {
  if (!links.search) {
    return null;
  }
  return <OmniSearch {...props} />;
};

export default OmniSearchGuard;
