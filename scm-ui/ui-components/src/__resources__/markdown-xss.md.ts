/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

export default `# XSS should prevented

You should not see an alert onload.
<div onload=alert("onload alert")></div>
<script>
  alert("Hello alter from script tag");
</script>

If you hover over <b onmouseover=alert("onmouseover alert")>me</b> nothing should happen.

Not every html <strong>html</strong> <i>should</i> be removed.

<IFRAME SRC="javascript:alert('iframe');"></IFRAME>

<FRAMESET><FRAME SRC="javascript:alert('frameset');"></FRAMESET>

<OBJECT TYPE="text/x-scriptlet" DATA="http://xss.rocks/scriptlet.html"></OBJECT>
`;
