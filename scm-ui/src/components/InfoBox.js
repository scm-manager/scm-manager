//@flow
import * as React from "react";
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
    marginLeft: "1.5em",
    minHeight: "10.5rem"
  },
  link: {
    display: "block",
    marginBottom: "1.5rem"
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
        <h4 className="has-text-link">{title}</h4>
        <p>{summary}</p>
      </div>
    );

  };

  render() {
    const { item, type, classes, t } = this.props;
    const icon = type === "plugin" ? "puzzle-piece" : "star";
    return (
      <a href={item._links.self.href} className={classes.link}>
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
      </a>
    );
  }

}

export default injectSheet(styles)(translate("commons")(InfoBox));


