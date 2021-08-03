import React, { FC, useState } from "react";
import { useSearchableTypes, useSearchSyntaxContent } from "@scm-manager/ui-api";
import { useTranslation } from "react-i18next";
import { Button, copyToClipboard, Icon, InputField, Loading, MarkdownView, Page } from "@scm-manager/ui-components";
import { parse } from "date-fns";
import styled from "styled-components";

const IconContainer = styled.div`
  position: relative;
  width: 1.5rem;
  height: 1.5rem;
`;

const Syntax: FC = () => {
  const { t, i18n } = useTranslation("commons");
  const { loading: isLoading, data: helpModalContent } = useSearchSyntaxContent(i18n.languages[0]);
  const [datetime, setDatetime] = useState("");
  const [timestamp, setTimestamp] = useState("");
  const [copying, setCopying] = useState(false);
  const { isLoading: isLoadingSearchableTypes, data: searchableTypes } = useSearchableTypes();

  // useEffect(() => console.log("searchableTypes", searchableTypes), [searchableTypes]);

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

  let staticLoadedContent;
  if (isLoading || isLoadingSearchableTypes) {
    staticLoadedContent = <Loading />;
  } else {
    staticLoadedContent = <MarkdownView content={helpModalContent!} basePath="/" />;
  }

  return (
    <Page title={t("search.syntax.title")} subtitle={t("search.syntax.subtitle")} loading={isLoading}>
      <div className="content">
        <h4 className="title">{t("search.syntax.exampleQueriesAndFields.title")}</h4>
        <p>{t("search.syntax.exampleQueriesAndFields.description")}</p>
        <h5 className="title">Repository</h5>
        <table>
          <tr>
            <th>Field</th>
            <th>Type</th>
            <th>Example Value</th>
            <th>Hints</th>
          </tr>
          <tr>
            <th>nameSpace</th>
            <td>String</td>
            <td>SCM-Manager</td>
            <td>Query repositories in a namespace</td>
          </tr>
        </table>
        <h5 className="title">Example Queries</h5>
        <div>Combine Fields with Modifiers and Operators to find your repositories.</div>
        <table>
          <tr>
            <th>What you search for</th>
            <th>Query</th>
            <th>Explanation</th>
          </tr>
          <tr>
            <td>
              a repository called ultimate-Repository or ultimate_repository. You are not sure about the connecting
              character.
            </td>
            <td>name:ultimate?Repository</td>
            <td>queries "ultimate" and "repository" with a single character between in the field "name".</td>
          </tr>
        </table>
      </div>
      {staticLoadedContent}
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
          <Button color="primary" action={convert}>
            {t("search.syntax.utilities.datetime.convertButtonLabel")}
          </Button>
        </span>
        <InputField
          className="mr-4"
          value={timestamp}
          readOnly={true}
          placeholder={t("search.syntax.utilities.timestampPlaceholder")}
        />
        {/*<Tooltip message={t("search.syntax.utilities.copyTimestampTooltip")}>*/}
        <IconContainer className="is-flex is-justify-content-center is-align-content-center">
          {copying ? (
            <span className="small-loading-spinner" />
          ) : (
            <Icon name="clipboard" color="inherit" className="is-size-4 fa-fw is-clickable" onClick={copyTimestamp} />
          )}
        </IconContainer>
        {/*</Tooltip>*/}
      </div>
    </Page>
  );
};

export default Syntax;
