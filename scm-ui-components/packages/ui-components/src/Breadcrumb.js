//@flow
import React from "react";
import { Link } from "react-router-dom";

type Props = {
  path: string,
  baseUrl: string
};

class Breadcrumb extends React.Component<Props> {
  render() {
    const { path, baseUrl } = this.props;

    if (path) {
      const paths = path.split("/");

      return (
        <nav className="breadcrumb" aria-label="breadcrumbs">
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
      );
    }
    return null;
  }
}

export default Breadcrumb;
