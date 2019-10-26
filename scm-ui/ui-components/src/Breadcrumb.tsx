import React from "react";
import { Link } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { Branch, Repository } from "@scm-manager/ui-types";
import Icon from "./Icon";

type Props = WithTranslation & {
  repository: Repository;
  branch: Branch;
  defaultBranch: Branch;
  branches: Branch[];
  revision: string;
  path: string;
  baseUrl: string;
};

const FlexStartNav = styled.nav`
  flex: 1;
`;

const HomeIcon = styled(Icon)`
  line-height: 1.5rem;
`;

const ActionWrapper = styled.div`
  align-self: center;
  padding-right: 1rem;
`;

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
              <Link to="#" aria-current="page">
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
    return null;
  }

  render() {
    const { baseUrl, branch, defaultBranch, branches, revision, path, repository, t } = this.props;

    let homeUrl = baseUrl + "/";
    if (revision) {
      homeUrl += encodeURIComponent(revision) + "/";
    }

    return (
      <>
        <div className="is-flex">
          <FlexStartNav className={classNames("breadcrumb", "sources-breadcrumb")} aria-label="breadcrumbs">
            <ul>
              <li>
                <Link to={homeUrl}>
                  <HomeIcon title={t("breadcrumb.home")} name="home" color="inherit" />
                </Link>
              </li>
              {this.renderPath()}
            </ul>
          </FlexStartNav>
          {binder.hasExtension("repos.sources.actionbar") && (
            <ActionWrapper>
              <ExtensionPoint
                name="repos.sources.actionbar"
                props={{
                  baseUrl,
                  branch: branch ? branch : defaultBranch,
                  path,
                  isBranchUrl: branches
                    ? branches.filter(b => b.name.replace("/", "%2F") === revision).length > 0
                    : true,
                  repository
                }}
                renderAll={true}
              />
            </ActionWrapper>
          )}
        </div>
        <hr className="is-marginless" />
      </>
    );
  }
}

export default withTranslation("commons")(Breadcrumb);
