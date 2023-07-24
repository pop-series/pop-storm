package io.pop.storm.recipes.hooks.http;

public record ServerConfig(int port, int backlog, int stopWaitSecs) {}
