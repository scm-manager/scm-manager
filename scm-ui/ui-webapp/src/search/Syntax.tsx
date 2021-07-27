import React, {FC} from "react";
import {useSearchSyntaxContent} from "@scm-manager/ui-api";
import {useTranslation} from "react-i18next";
import {ErrorNotification, Loading, MarkdownView, Page} from "@scm-manager/ui-components";

const Syntax: FC = () => {
  const {t, i18n} = useTranslation("commons");
  const {isLoading, error, data: helpModalContent} = useSearchSyntaxContent(i18n.languages[0]);

  let staticLoadedContent;
  if (isLoading) {
    staticLoadedContent = <Loading />;
  } else if (error) {
    staticLoadedContent = <ErrorNotification error={error} />;
  } else {
    staticLoadedContent = <MarkdownView content={helpModalContent!} basePath="/" />;
  }

  return <Page
    title={t("help.search.syntax.title")}
    subtitle={t("help.search.syntax.subtitle")}
    loading={isLoading}
    error={error}
  >
    <h4 className="title">{t("help.search.syntax.exampleQueriesAndFields.title")}</h4>
    <p>{t("help.search.syntax.exampleQueriesAndFields.description")}</p>
    {staticLoadedContent}
  </Page>;
};

export default Syntax;
