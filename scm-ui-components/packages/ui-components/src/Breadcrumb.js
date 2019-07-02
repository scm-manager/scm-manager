//@flow
import React from "react";
import { Link } from "react-router-dom";
import injectSheet from "react-jss";

type Props = {
  revision: string,
  path: string,
  baseUrl: string,
  classes: any
};

const styles = {
  noMargin: {
    margin: "0"
  }
};

class Breadcrumb extends React.Component<Props> {
  renderPath() {
    const { revision, path, baseUrl } = this.props;

    if (path) {
      const paths = path.split("/");
      const map = paths.map((path, index) => {
        const currPath = paths.slice(0, index + 1).join("/");
        if (paths.length - 1 === index) {
          return (
            <li className="is-active" key={index}>
              <Link to={"#"} aria-current="page">
                {path}
              </Link>
            </li>
          );
        }
        return (
          <li key={index}>
            <Link to={baseUrl + "/" + revision + "/" + currPath}>{path}</Link>
          </li>
        );
      });
      return map;
    }
    return <li />;
  }

  render() {
    const { classes } = this.props;

    return (
      <>
        <nav className="breadcrumb sources-breadcrumb" aria-label="breadcrumbs">
          <ul>{this.renderPath()}</ul>
        </nav>
        <hr className={classes.noMargin} />
      </>
    );
  }
}

export default injectSheet(styles)(Breadcrumb);
