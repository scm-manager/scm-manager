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

export default `diff --git a/src/main/java/com/cloudogu/scm/review/importantevents/EventListener.java b/src/main/java/com/cloudogu/scm/review/importantevents/EventListener.java
index f889f9c..95e3b10 100644
--- a/src/main/java/com/cloudogu/scm/review/importantevents/EventListener.java
+++ b/src/main/java/com/cloudogu/scm/review/importantevents/EventListener.java
@@ -1,20 +1,12 @@
 package com.cloudogu.scm.review.events;
 
-import com.cloudogu.scm.review.comment.service.BasicComment;
-import com.cloudogu.scm.review.comment.service.BasicCommentEvent;
-import com.cloudogu.scm.review.comment.service.CommentEvent;
-import com.cloudogu.scm.review.comment.service.ReplyEvent;
 import com.cloudogu.scm.review.pullrequest.service.BasicPullRequestEvent;
 import com.cloudogu.scm.review.pullrequest.service.PullRequest;
-import com.cloudogu.scm.review.pullrequest.service.PullRequestEvent;
 import com.github.legman.Subscribe;
-import lombok.Data;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.PrincipalCollection;
 import org.apache.shiro.subject.Subject;
 import sonia.scm.EagerSingleton;
-import sonia.scm.HandlerEventType;
-import sonia.scm.event.HandlerEvent;
 import sonia.scm.plugin.Extension;
 import sonia.scm.repository.Repository;
 import sonia.scm.security.SessionId;
diff --git a/src/main/js/ChangeNotification.tsx b/src/main/js/ChangeNotification.tsx
index f6d61a9..5f371e4 100644
--- a/src/main/js/ChangeNotification.tsx
+++ b/src/main/js/ChangeNotification.tsx
@@ -2,6 +2,7 @@ import React, { FC, useEffect, useState } from "react";
 import { Link } from "@scm-manager/ui-types";
 import { apiClient, Toast, ToastButtons, ToastButton } from "@scm-manager/ui-components";
 import { PullRequest } from "./types/PullRequest";
+import { useTranslation } from "react-i18next";
 
 type HandlerProps = {
   url: string;
@@ -15,14 +16,19 @@ const EventNotificationHandler: FC<HandlerProps> = ({ url, reload }) => {
       pullRequest: setEvent
     });
   }, [url]);
+  const { t } = useTranslation("plugins");
   if (event) {
     return (
-      <Toast type="warning" title="New Changes">
-        <p>The underlying Pull-Request has changed. Press reload to see the changes.</p>
-        <p>Warning: Non saved modification will be lost.</p>
+      <Toast type="warning" title={t("scm-review-plugin.changeNotification.title")}>
+        <p>{t("scm-review-plugin.changeNotification.description")}</p>
+        <p>{t("scm-review-plugin.changeNotification.modificationWarning")}</p>
         <ToastButtons>
-          <ToastButton icon="redo" onClick={reload}>Reload</ToastButton>
-          <ToastButton icon="times" onClick={() => setEvent(undefined)}>Ignore</ToastButton>
+          <ToastButton icon="redo" onClick={reload}>
+            {t("scm-review-plugin.changeNotification.buttons.reload")}
+          </ToastButton>
+          <ToastButton icon="times" onClick={() => setEvent(undefined)}>
+            {t("scm-review-plugin.changeNotification.buttons.ignore")}
+          </ToastButton>
         </ToastButtons>
       </Toast>
     );
diff --git a/src/main/resources/locales/de/plugins.json b/src/main/resources/locales/de/plugins.json
index 80f84a1..2c63ab3 100644
--- a/src/main/resources/locales/de/plugins.json
+++ b/src/main/resources/locales/de/plugins.json
@@ -181,6 +181,15 @@
           "titleClickable": "Der Kommentar bezieht sich auf eine ältere Version des Source- oder Target-Branches. Klicken Sie hier, um den ursprünglichen Kontext zu sehen."
         }
       }
+    },
+    "changeNotification": {
+      "title": "Neue Änderungen",
+      "description": "An diesem Pull Request wurden Änderungen vorgenommen. Laden Sie die Seite neu um diese anzuzeigen.",
+      "modificationWarning": "Warnung: Nicht gespeicherte Eingaben gehen verloren.",
+      "buttons": {
+        "reload": "Neu laden",
+        "ignore": "Ignorieren"
+      }
     }
   },
   "permissions": {
diff --git a/src/main/resources/locales/en/plugins.json b/src/main/resources/locales/en/plugins.json
index d020fcd..e3c1630 100644
--- a/src/main/resources/locales/en/plugins.json
+++ b/src/main/resources/locales/en/plugins.json
@@ -181,6 +181,15 @@
           "titleClickable": "The comment is related to an older of the source or target branch. Click here to see the original context."
         }
       }
+    },
+    "changeNotification": {
+      "title": "New Changes",
+      "description": "The underlying Pull-Request has changed. Press reload to see the changes.",
+      "modificationWarning": "Warning: Non saved modification will be lost.",
+      "buttons": {
+        "reload": "Reload",
+        "ignore": "Ignore"
+      }
     }
   },
   "permissions": {
diff --git a/src/test/java/com/cloudogu/scm/review/events/ClientTest.java b/src/test/java/com/cloudogu/scm/review/events/ClientTest.java
index 889cc49..d5a4811 100644
--- a/src/test/java/com/cloudogu/scm/review/events/ClientTest.java
+++ b/src/test/java/com/cloudogu/scm/review/events/ClientTest.java
@@ -7,19 +7,16 @@ import org.mockito.Answers;
 import org.mockito.Mock;
 import org.mockito.junit.jupiter.MockitoExtension;
 import sonia.scm.security.SessionId;
+
 import jakarta.ws.rs.sse.OutboundSseEvent;
 import jakarta.ws.rs.sse.SseEventSink;
-
 import java.time.Clock;
 import java.time.Instant;
 import java.time.LocalDateTime;
 import java.time.ZoneOffset;
 import java.time.temporal.ChronoField;
-import java.time.temporal.ChronoUnit;
-import java.time.temporal.TemporalField;
 import java.util.concurrent.CompletableFuture;
 import java.util.concurrent.CompletionStage;
-import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicReference;
 
 import static java.time.temporal.ChronoUnit.MINUTES;
@@ -83,7 +80,7 @@ class ClientTest {
 
   @Test
   @SuppressWarnings("unchecked")
-  void shouldCloseEventSinkOnFailure() throws InterruptedException {
+  void shouldCloseEventSinkOnFailure() {
     CompletionStage future = CompletableFuture.supplyAsync(() -> {
       throw new RuntimeException("failed to send message");
     });
@@ -91,9 +88,7 @@ class ClientTest {
 
     client.send(message);
 
-    Thread.sleep(50L);
-
-    verify(eventSink).close();
+    verify(eventSink, timeout(50L)).close();
   }
 
   @Test
diff --git a/Main.java b/Main.java
index e77e6da..f183b7c 100644
--- a/Main.java
+++ b/Main.java
@@ -1,9 +1,18 @@
+import java.io.PrintStream;
 import java.util.Arrays;
 
 class Main {
+    private static final PrintStream OUT = System.out;
+
     public static void main(String[] args) {
+<<<<<<< HEAD
         System.out.println("Expect nothing more to happen.");
         System.out.println("The command line parameters are:");
         Arrays.stream(args).map(arg -> "- " + arg).forEach(System.out::println);
+=======
+        OUT.println("Expect nothing more to happen.");
+        OUT.println("Parameters:");
+        Arrays.stream(args).map(arg -> "- " + arg).forEach(OUT::println);
+>>>>>>> feature/use_constant
     }
 }
`;
