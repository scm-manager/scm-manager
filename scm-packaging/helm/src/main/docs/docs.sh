#!/bin/bash
#
# Copyright (c) 2020 - present Cloudogu GmbH
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, version 3.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see https://www.gnu.org/licenses/.
#

set -euo pipefail
IFS=$'\n\t'

if helm-docs --version > /dev/null 2>&1; then
  SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
  MODULE_DIR="$( cd "${SCRIPT_DIR}/../../../" >/dev/null 2>&1 && pwd )"
  CHARTDIR="${MODULE_DIR}/build/helm/charts/scm-manager"
  PAGE="docs/en/installation/k8s.md"
  TEMPLATE="${MODULE_DIR}/src/main/docs/helm.md.gotmpl"
  README="${MODULE_DIR}/../../${PAGE}"

  if [ ! -d "${CHARTDIR}" ]; then
    echo "chart directory ${CHARTDIR} does not exists"
    echo "please run maven package before"
    echo "https://github.com/norwoodj/helm-docs"
    exit 1
  fi

  cd "${CHARTDIR}"
  
  helm-docs -t "${MODULE_DIR}/src/main/docs/helm.md.gotmpl" -l trace
  cp "${CHARTDIR}/README.md" "${README}"
  echo "The helm documentation was successfully generated"
  echo "Please check the results at ${PAGE}"
  echo "And don't forget to commit ;)"

else
  echo "helm-docs not found"
  echo "please install helm-docs, before running this script"
  echo "https://github.com/norwoodj/helm-docs"
  exit 1
fi
