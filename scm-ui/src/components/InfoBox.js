//@flow
import * as React from "react";
import { ErrorNotification } from "@scm-manager/ui-components";
import injectSheet from "react-jss";
import classNames from "classnames";
import { translate } from "react-i18next";
import type { InfoItem } from "./InfoItem";

const styles = {
  image: {
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    flexDirection: "column",
    width: 160,
    height: 160
  },
  icon: {
    color: "#bff1e6"
  },
  label: {
    marginTop: "0.5em"
  },
  content: {
    marginLeft: "1.5em"
  },
  link: {
    width: "60%",
    height: "200px",
    position: "absolute",
    cursor: "pointer",
    zIndex: 20
  },
  "@media (max-width: 768px)": {
    link: {
      width: "100%"
    }
  }
};

type Props = {
  type: "plugin" | "feature",
  item: InfoItem,

  // context props
  classes: any,
  t: string => string
};

class InfoBox extends React.Component<Props> {

  renderBody = () => {
    const { item, t } = this.props;

    const bodyClasses = classNames("media-content", "content", this.props.classes.content);
    const title = item ? item.title : t("login.loading");
    const summary = item ? item.summary : t("login.loading");

    return (
      <div className={bodyClasses}>
        <h4>
          <a href={item._links.self.href}>{title}</a>
        </h4>
        <p>{summary}</p>
      </div>
    );

  };


  createLink = () => {
    const { item, classes } = this.props;
    // eslint-disable-next-line jsx-a11y/anchor-has-content
    return <a href={item._links.self.href} className={classes.link}/>;
  };

  render() {
    const { type, classes, t } = this.props;
    const icon = type === "plugin" ? "puzzle-piece" : "star";
    return (
      <>
        {this.createLink()}
        <div className="box media">
          <figure className="media-left">
            <div
              className={classNames("image", "box", "has-background-info", "has-text-white", "has-text-weight-bold", classes.image)}>
              <i className={classNames("fas", "fa-" + icon, "fa-2x", classes.icon)}/>
              <div className={classNames("is-size-4", classes.label)}>{t("login." + type)}</div>
              <div className={classNames("is-size-4")}>{t("login.tip")}</div>
            </div>
          </figure>
          {this.renderBody()}
        </div>
      </>
    );
  }

}

export default injectSheet(styles)(translate("commons")(InfoBox));


