module top.pigest.dialogeditor {
    requires javafx.controls;

//    requires org.controlsfx.controls;
//    requires com.dlsc.formsfx;
    requires com.jfoenix;
    requires java.desktop;
    requires java.net.http;
    requires java.security.jgss;
    requires jdk.crypto.ec;
    requires com.google.gson;
    requires jdk.jsobject;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;
    requires javafx.graphics;
    requires javafx.base;
    requires jdk.jshell;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;

    exports top.pigest.dialogeditor;
    exports top.pigest.dialogeditor.util;
    exports top.pigest.dialogeditor.control;
    exports top.pigest.dialogeditor.main;
    exports top.pigest.dialogeditor.resource;
    opens top.pigest.dialogeditor.main to com.google.gson;
    opens top.pigest.dialogeditor.util to com.google.gson;
    opens top.pigest.dialogeditor.control to com.google.gson;
    exports top.pigest.dialogeditor.util.gi;
    exports top.pigest.dialogeditor.dialog;
    opens top.pigest.dialogeditor.dialog to com.google.gson;
    opens top.pigest.dialogeditor.util.gi to com.google.gson;
    exports top.pigest.dialogeditor.dialog.ui;
    opens top.pigest.dialogeditor.dialog.ui to com.google.gson;
    opens top.pigest.dialogeditor to com.google.gson;
}