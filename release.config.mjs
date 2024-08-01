import config from 'semantic-release-preconfigured-conventional-commits' with { type: "json" };
config.plugins = [
    "@semantic-release/commit-analyzer",
    "@semantic-release/release-notes-generator",
    "@semantic-release/github",
];
export default config;
