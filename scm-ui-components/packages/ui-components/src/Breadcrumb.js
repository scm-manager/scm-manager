//@flow
import React from "react";
import { Link } from "react-router-dom";
import injectSheet from "react-jss";
import classNames from "classnames";

type Props = {
  path: string,
  baseUrl: string,
  classes: any
};

const styles = {
  margin: {
    margin: "1rem 1.25rem 0rem"
  }
};

class Breadcrumb extends React.Component<Props> {
  render() {
    const { path, baseUrl, classes } = this.props;

    if (path) {
      const paths = path.split("/");

      return (
        <>
          <nav
            className={classNames("breadcrumb", classes.margin)}
            aria-label="breadcrumbs"
          >
            <ul>
              {paths.map((path, index) => {
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
                    <Link to={baseUrl + "/" + path}>{path}</Link>
                  </li>
                );
              })}
            </ul>
          </nav>
          <hr />
        </>
      );
    }
    return null;
  }
}

export default injectSheet(styles)(Breadcrumb);
