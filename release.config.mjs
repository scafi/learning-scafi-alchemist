import config from 'semantic-release-preconfigured-conventional-commits' assert { type: "json" };
config.plugins = [
    "@semantic-release/commit-analyzer",
    "@semantic-release/release-notes-generator",
    "@semantic-release/github",
    "@semantic-release/git",
];
