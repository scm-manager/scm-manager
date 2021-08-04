import React, { FC, useState } from "react";
import { useSearchableTypes, useSearchSyntaxContent } from "@scm-manager/ui-api";
import { useTranslation } from "react-i18next";
import {
  Button,
  copyToClipboard,
  Icon,
  InputField,
  Loading,
  MarkdownView,
  Page,
  Tooltip,
} from "@scm-manager/ui-components";
import { parse } from "date-fns";
import styled from "styled-components";
import classNames from "classnames";

const StyledTooltip = styled(Tooltip)`
  height: 40px;
`;

type ExpandableProps = {
  header: React.ReactNode;
  className?: string;
};

const Expandable: FC<ExpandableProps> = ({ header, children, className }) => {
  const [expanded, setExpanded] = useState(false);
  return (
    <div className={classNames("card", className)}>
      <header onClick={() => setExpanded(!expanded)} className="card-header is-clickable">
        <span className="card-header-title">{header}</span>
        <span className="card-header-icon">
          <Icon name={expanded ? "chevron-down" : "chevron-left"} />
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

const Syntax: FC = () => {
  const { t, i18n } = useTranslation(["commons", "plugins"]);
  const { loading: isLoading, data: helpModalContent } = useSearchSyntaxContent(i18n.languages[0]);
  const [datetime, setDatetime] = useState("");
  const [timestamp, setTimestamp] = useState("");
  const [copying, setCopying] = useState(false);
  const { isLoading: isLoadingSearchableTypes, data: searchableTypes } = useSearchableTypes();

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

  if (isLoading || isLoadingSearchableTypes) {
    return <Loading />;
  }

  const searchableTypesContent = searchableTypes!.map((searchableType) => {
    const examples = t<Example[]>(`plugins:search.types.${searchableType.name}.examples`, {
      returnObjects: true,
      defaultValue: [],
    });
    return (
      <Expandable className="mb-1" header={t(`plugins:search.types.${searchableType.name}.title`)}>
        <table>
          <tr>
            <th>{t("search.syntax.fields.name")}</th>
            <th>{t("search.syntax.fields.type")}</th>
            <th>{t("search.syntax.fields.exampleValue")}</th>
            <th>{t("search.syntax.fields.hints")}</th>
          </tr>
          {searchableType.fields.map((searchableField) => (
            <tr>
              <th>{t(`plugins:search.types.${searchableType.name}.fields.${searchableField.name}.name`)}</th>
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
        </table>
        {examples.length > 0 ? (
          <>
            <h5 className="title mt-5">{t("search.syntax.exampleQueries.title")}</h5>
            <div className="mb-2">{t("search.syntax.exampleQueries.description")}</div>
            <table>
              <tr>
                <th>{t("search.syntax.exampleQueries.table.description")}</th>
                <th>{t("search.syntax.exampleQueries.table.query")}</th>
                <th>{t("search.syntax.exampleQueries.table.explanation")}</th>
              </tr>
              {examples.map((example) => (
                <tr>
                  <td>{example.description}</td>
                  <td>{example.query}</td>
                  <td>{example.explanation}</td>
                </tr>
              ))}
            </table>
          </>
        ) : null}
      </Expandable>
    );
  });

  return (
    <Page title={t("search.syntax.title")} subtitle={t("search.syntax.subtitle")} loading={isLoading}>
      <div className="content">
        <h4 className="title">{t("search.syntax.exampleQueriesAndFields.title")}</h4>
        <p>{t("search.syntax.exampleQueriesAndFields.description")}</p>
        {searchableTypesContent}
      </div>
      <MarkdownView content={helpModalContent!} basePath="/" />
      <h3 className="title">{t("search.syntax.utilities.title")}</h3>
      <p>{t("search.syntax.utilities.description")}</p>
      <h6 className="title is-6 mt-4">{t("search.syntax.utilities.datetime.label")}</h6>
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
              <Icon name="clipboard" color="inherit" className="is-size-4 fa-fw is-clickable" onClick={copyTimestamp} />
            )}
          </StyledTooltip>
        </span>
      </div>
    </Page>
  );
};

export default Syntax;
