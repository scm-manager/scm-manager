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

export default `diff --git a/Main.java b/Main.java
index 9b5ca13..7ced845 100644
--- a/Main.java
+++ b/Main.java
@@ -1,5 +1,5 @@
  class Main {
  -    public static void main(String[] args) {
  +    public static void main(String[] arguments) {
    System.out.println("Expect nothing more to happen.");
  }
}
diff --git a/conflict.png b/conflict.png
new file mode 100644
index 0000000..7c77c7f
--- /dev/null
+++ b/conflict.png
Binary files differ
`;
