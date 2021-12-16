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
import React, { FC, useState } from "react";
import { useSearchableTypes, useSearchSyntaxContent } from "@scm-manager/ui-api";
import { useTranslation } from "react-i18next";
import {
  Button,
  copyToClipboard,
  ErrorNotification,
  Icon,
  InputField,
  Loading,
  MarkdownView,
  Page,
  Tooltip
} from "@scm-manager/ui-components";
import { parse } from "date-fns";
import styled from "styled-components";
import classNames from "classnames";
import { SearchableType } from "@scm-manager/ui-types";

const StyledTooltip = styled(Tooltip)`
  height: 40px;
`;

type ExpandableProps = {
  header: React.ReactNode;
  className?: string;
};

const Expandable: FC<ExpandableProps> = ({ header, children, className }) => {
  const [t] = useTranslation("commons");
  const [expanded, setExpanded] = useState(false);
  return (
    <div className={classNames("card search-syntax-accordion", className)}>
      <header onClick={() => setExpanded(!expanded)} className="card-header is-clickable">
        <span className="card-header-title">{header}</span>
        <span className="card-header-icon">
          {expanded ? (
            <Icon name="chevron-down" alt={t("search.syntax.expandable.hideMore")} />
          ) : (
            <Icon name="chevron-left" alt={t("search.syntax.expandable.showMore")} />
          )}
        </span>
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
  const examples = t<Example[]>(`plugins:search.types.${searchableType.name}.examples`, {
    returnObjects: true,
    defaultValue: []
  });

  if (examples.length === 0) {
    return null;
  }

  return (
    <>
      <h5 className="title mt-5">{t("search.syntax.exampleQueries.title")}</h5>
      <div className="mb-2">{t("search.syntax.exampleQueries.description")}</div>
      <table>
        <tr>
          <th>{t("search.syntax.exampleQueries.table.description")}</th>
          <th>{t("search.syntax.exampleQueries.table.query")}</th>
          <th>{t("search.syntax.exampleQueries.table.explanation")}</th>
        </tr>
        {examples.map((example, index) => (
          <tr key={index}>
            <td>{example.description}</td>
            <td>{example.query}</td>
            <td>{example.explanation}</td>
          </tr>
        ))}
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
      {data.map(searchableType => (
        <Expandable
          key={searchableType.name}
          className="mb-1"
          header={t(`plugins:search.types.${searchableType.name}.title`, searchableType.name)}
        >
          <table>
            <tr>
              <th>{t("search.syntax.fields.name")}</th>
              <th>{t("search.syntax.fields.type")}</th>
              <th>{t("search.syntax.fields.exampleValue")}</th>
              <th>{t("search.syntax.fields.hints")}</th>
            </tr>
            {searchableType.fields.map(searchableField => (
              <tr>
                <th>{searchableField.name}</th>
                <td>{searchableField.type}</td>
                <td>
                  {t(`plugins:search.types.${searchableType.name}.fields.${searchableField.name}.exampleValue`, {
                    defaultValue: ""
                  })}
                </td>
                <td>
                  {t(`plugins:search.types.${searchableType.name}.fields.${searchableField.name}.hints`, {
                    defaultValue: ""
                  })}
                </td>
              </tr>
            ))}
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
          value={datetime}
          onChange={setDatetime}
          placeholder={t("search.syntax.utilities.datetime.format")}
        />
        <Button color="primary" action={convert} className="ml-2">
          {t("search.syntax.utilities.datetime.convertButtonLabel")}
        </Button>
      </span>
      <span className="is-flex">
        <InputField
          className="mr-4"
          value={timestamp}
          readOnly={true}
          placeholder={t("search.syntax.utilities.timestampPlaceholder")}
        />
        <StyledTooltip
          message={t("search.syntax.utilities.copyTimestampTooltip")}
          className="is-flex is-align-items-center"
        >
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
            <h4 className="title">{t("search.syntax.exampleQueriesAndFields.title")}</h4>
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
