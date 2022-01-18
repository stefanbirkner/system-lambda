# Changelog

Status: Accepted


## Context

Sometimes I (Stefan) publish a new release of System Lambda.

Users find out about new releases, e.g. because I tweet about it or their
dependency management tool tells them about a new version. They want to know
what changed in the new version (or maybe several versions). Some are curious
about new features or bugfixes. Others need this information in order to decide
whether they change their software to use a newer version of System Lambda or
not.


## Decision

I provide a changelog that is stored in the file CHANGELOG.md.

A curated list of the changes is the fastest way for users to find out about
changes in System Lambda.

CHANGELOG.md is a common filename for changelogs. Users who are used to this
convention may look at this file without further guideline. For other users
there is a link to the changelog in the readme file.

The file is a text file that uses Markdown, because it provides lightweight text
formatting that is nicely rendered by some viewers (e.g. GitHub which hosts
this project) while it still can be viewed and edited by standard text editors.


## Consequences

Developers and project stakeholders can see the changes without viewing the git
log.

The changelog must be updated before a new release is published.

Information may be duplicated between docs, changelog and git log.
