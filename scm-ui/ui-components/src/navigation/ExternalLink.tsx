import React, { FC } from "react";
import classNames from "classnames";

type Props = {
  to: string;
  icon?: string;
  label: string;
};

const ExternalLink: FC<Props> = ({ to, icon, label }) => {
  let showIcon;
  if (icon) {
    showIcon = (
      <>
        <i className={classNames(icon, "fa-fw")} />{" "}
      </>
    );
  }

  return (
    <li>
      <a target="_blank" href={to}>
        {showIcon}
        {label}
      </a>
    </li>
  );
};

export default ExternalLink;
