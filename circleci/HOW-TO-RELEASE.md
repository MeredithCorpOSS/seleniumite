# How to perform a release

These are mainly notes for myself to remember the steps of performing a release on CircleCI

* Make sure everything is committed, pushed, and a regular build worked on CircleCI
* Add a 'release' tag, like:

git tag -a release-{number} (eg git tag -a release-1.1.2)
git push origin master --tags

* Log in to Sonatype and work through their release process after CircleCI finished deploying


# Skipping GPG
To skip GPG locally, use -Dgpg.skip

# To Release

1. Commit all changes
2. Tag the current location with a tag of the form "release-xxx" where xxx is the version number, eg "git tag -a release-1.1.1"
3. Push the branch and tags to master, CircleCI will take it from there, eg "git push origin master --tags"
4. Login to Sonatype and close/release
