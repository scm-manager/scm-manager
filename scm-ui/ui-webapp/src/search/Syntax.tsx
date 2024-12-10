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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { parse } from "date-fns";
import styled from "styled-components";
import { useSearchableTypes, useSearchSyntaxContent } from "@scm-manager/ui-api";
import { copyToClipboard, InputField, MarkdownView, Page } from "@scm-manager/ui-components";
import { ErrorNotification, Icon, Loading, Tooltip, Button, useDocumentTitle } from "@scm-manager/ui-core";
import { SearchableType } from "@scm-manager/ui-types";

const StyledTooltip = styled(Tooltip)`
  height: 40px;
`;

const HeaderButton = styled.button`
  color: currentColor;
  width: 100%;
  border: none;
`;

type ExpandableProps = {
  header: React.ReactNode;
  className?: string;
};

const Expandable: FC<ExpandableProps> = ({ header, children, className }) => {
  const [t] = useTranslation("commons");
  useDocumentTitle(t("search.syntax.title"));
  const [expanded, setExpanded] = useState(false);

  return (
    <div className={classNames("card search-syntax-accordion", className)}>
      <header>
        <HeaderButton onClick={() => setExpanded(!expanded)} className="card-header is-clickable ">
          <span className="card-header-title">{header}</span>
          <span className="card-header-icon">
            {expanded ? (
              <Icon alt={t("search.syntax.expandable.hideMore")}>{"chevron-down"}</Icon>
            ) : (
              <Icon alt={t("search.syntax.expandable.showMore")}>{"chevron-left"}</Icon>
            )}
          </span>
        </HeaderButton>
      </header>
      {expanded ? <div className="card-content">{children}</div> : null}
    </div>
  );
};

type Example = {
  description: string;
  query: string;
  explanation: string;
};

type ExampleProps = {
  searchableType: SearchableType;
};

const Examples: FC<ExampleProps> = ({ searchableType }) => {
  const [t] = useTranslation(["commons", "plugins"]);
  // @ts-ignore the generic Example[] seems to not get applied for the return type
  const examples = t<Example[]>(`plugins:search.types.${searchableType.name}.examples`, {
    returnObjects: true,
    defaultValue: [],
  }) as unknown as Example[];

  if (examples.length === 0) {
    return null;
  }

  return (
    <>
      <h5 className="title mt-5">{t("search.syntax.exampleQueries.title")}</h5>
      <div className="mb-2">{t("search.syntax.exampleQueries.description")}</div>
      <table>
        <tbody>
          <tr>
            <th>{t("search.syntax.exampleQueries.table.description")}</th>
            <th>{t("search.syntax.exampleQueries.table.query")}</th>
            <th>{t("search.syntax.exampleQueries.table.explanation")}</th>
          </tr>
          {examples.map((example) => (
            <tr key={example.description}>
              <td>{example.description}</td>
              <td>{example.query}</td>
              <td>{example.explanation}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
};

const SearchableTypes: FC = () => {
  const [t] = useTranslation(["commons", "plugins"]);
  const { isLoading, error, data } = useSearchableTypes();

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || !data) {
    return <Loading />;
  }

  return (
    <>
      {data.map((searchableType) => (
        <Expandable
          key={searchableType.name}
          className="mb-1"
          header={t(`plugins:search.types.${searchableType.name}.title`, searchableType.name)}
        >
          <table>
            <tbody>
              <tr>
                <th>{t("search.syntax.fields.name")}</th>
                <th>{t("search.syntax.fields.type")}</th>
                <th>{t("search.syntax.fields.exampleValue")}</th>
                <th>{t("search.syntax.fields.hints")}</th>
              </tr>
              {searchableType.fields.map((searchableField) => (
                <tr key={searchableField.name}>
                  <th>{searchableField.name}</th>
                  <td>{searchableField.type}</td>
                  <td>
                    {t(`plugins:search.types.${searchableType.name}.fields.${searchableField.name}.exampleValue`, {
                      defaultValue: "",
                    })}
                  </td>
                  <td>
                    {t(`plugins:search.types.${searchableType.name}.fields.${searchableField.name}.hints`, {
                      defaultValue: "",
                    })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Examples searchableType={searchableType} />
        </Expandable>
      ))}
    </>
  );
};

const TimestampConverter: FC = () => {
  const [t] = useTranslation("commons");
  const [datetime, setDatetime] = useState("");
  const [timestamp, setTimestamp] = useState("");
  const [copying, setCopying] = useState(false);

  const convert = () => {
    const format = "yyyy-MM-dd HH:mm:ss";
    const date = parse(datetime, format, new Date());
    const newTimestamp = date.getTime();
    setTimestamp(String(newTimestamp));
  };
  const copyTimestamp = () => {
    setCopying(true);
    copyToClipboard(timestamp).finally(() => setCopying(false));
  };

  return (
    <div className="is-flex">
      <span className="is-flex mr-5">
        <InputField
          className="m-0"
          value={datetime}
          onChange={setDatetime}
          placeholder={t("search.syntax.utilities.datetime.format")}
        />
        <Button variant="primary" onClick={convert} className="ml-2">
          {t("search.syntax.utilities.datetime.convertButtonLabel")}
        </Button>
      </span>
      <span className="is-flex">
        <InputField
          className="m-0 mr-4"
          value={timestamp}
          readOnly={true}
          placeholder={t("search.syntax.utilities.timestampPlaceholder")}
        />
        <div className="is-flex is-align-items-center">
          <StyledTooltip message={t("search.syntax.utilities.copyTimestampTooltip")}>
            {copying ? (
              <span className="small-loading-spinner" />
            ) : (
              <Icon
                name="clipboard"
                color="inherit"
                className="is-size-4 fa-fw is-clickable"
                onClick={copyTimestamp}
                alt={t("search.syntax.utilities.copyTimestampTooltip")}
              />
            )}
          </StyledTooltip>
        </div>
      </span>
    </div>
  );
};

const Syntax: FC = () => {
  const { t, i18n } = useTranslation("commons");
  const { isLoading, data, error } = useSearchSyntaxContent(i18n.languages[0]);
  return (
    <Page title={t("search.syntax.title")} subtitle={t("search.syntax.subtitle")} loading={isLoading} error={error}>
      {data ? (
        <>
          <div className="content">
            <h3 className="title">{t("search.syntax.queryTypes.title")}</h3>
            <p>{t("search.syntax.queryTypes.description")}</p>
          </div>
          <div className="content">
            <h3 className="title">{t("search.syntax.exampleQueriesAndFields.title")}</h3>
            <p>{t("search.syntax.exampleQueriesAndFields.description")}</p>
            <SearchableTypes />
          </div>
          <MarkdownView content={data} basePath="/" />
          <h3 className="title">{t("search.syntax.utilities.title")}</h3>
          <p>{t("search.syntax.utilities.description")}</p>
          <h6 className="title is-6 mt-4">{t("search.syntax.utilities.datetime.label")}</h6>
          <TimestampConverter />
        </>
      ) : null}
    </Page>
  );
};

export default Syntax;
