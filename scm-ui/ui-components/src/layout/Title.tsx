import React, { FC, useEffect } from "react";

type Props = {
  title?: string;
  customPageTitle?: string;
  preventRefreshingPageTitle?: boolean;
};

const Title: FC<Props> = ({ title, preventRefreshingPageTitle, customPageTitle }) => {
  useEffect(() => {
    if (!preventRefreshingPageTitle) {
      if (customPageTitle) {
        document.title = customPageTitle;
      } else if (title) {
        document.title = title;
      }
    }
  });

  if (title) {
    return <h1 className="title">{title}</h1>;
  }
  return null;
};

Title.defaultProps = {
  preventRefreshingPageTitle: false
};

export default Title;
