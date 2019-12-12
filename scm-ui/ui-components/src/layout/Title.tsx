import React, { FC, useEffect } from "react";
import classNames from "classnames";

type Props = {
  title?: string;
  customPageTitle?: string;
  preventRefreshingPageTitle?: boolean;
  className?: string;
};

const Title: FC<Props> = ({ title, preventRefreshingPageTitle, customPageTitle, className }) => {
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
    return <h1 className={classNames("title", className)}>{title}</h1>;
  }
  return null;
};

Title.defaultProps = {
  preventRefreshingPageTitle: false
};

export default Title;
