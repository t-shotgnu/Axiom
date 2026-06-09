open module Axiom {
    requires jakarta.persistence;
    requires jakarta.validation;
    requires jjwt.api;
    requires static lombok;
    requires org.apache.tomcat.embed.core;
    requires org.jspecify;
    requires org.slf4j;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.security.crypto;
    requires spring.security.web;
    requires spring.tx;
    requires spring.web;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.s3;
}
