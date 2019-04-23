// @flow
import React from "react";
import type { History } from "history";
import { withRouter } from "react-router-dom";
import classNames from "classnames";
import injectSheet from "react-jss";
import { PageActions } from "./layout";
import { FilterInput } from "./forms";
import { Button, urls } from "./index";

type Props = {
  showCreateButton: boolean,
  link: string,
  label?: string,

  // context props
  classes: Object,
  history: History,
  location: any
};

const styles = {
  button: {
    float: "right",
    marginTop: "1.25rem",
    marginLeft: "1.25rem"
  }
};

class OverviewPageActions extends React.Component<Props> {
  render() {
    const { history, location, link } = this.props;
    let directory = link.substring(0, link.indexOf("/"));
    return (
      <PageActions>
        <FilterInput
          value={urls.getQueryStringFromLocation(location)}
          filter={filter => {
            history.push(`/${directory}/?q=${filter}`);
          }}
        />
        {this.renderCreateButton()}
      </PageActions>
    );
  }

  renderCreateButton() {
    const { showCreateButton, classes, link, label } = this.props;
    if (showCreateButton) {
      return (
        <div className={classNames(classes.button, "input-button control")}>
          <Button label={label} link={`/${link}`} color="primary" />
        </div>
      );
    }
    return null;
  }
}

export default injectSheet(styles)(withRouter(OverviewPageActions));
