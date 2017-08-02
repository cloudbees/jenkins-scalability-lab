# ARG REPO_ROOT=testcases
# TODO add a REPO_FROM_GIT to pull repo from a gitserver
FROM jkarlos/git-server-docker
COPY testcases /git-server/repo-source
# Set up git basics including a repo for testcases
RUN git config --global user.email "demouser@example.com" \
    && git config --global user.name "Demo User" \
    && mkdir -p /git-server/testcases \
    && cd /git-server/testcases \
    && git init && echo '.gitignore' > .gitignore \
    && git add -f .gitignore && git commit -am "Initial commit"
# Convert contents of each subdirectory in repo-sources to a testcases branch
# This strips out the subfolder from the level and copies in the contents for this branch & commits
# While doing the needful to handle paths
# And finally creates bare repositories so this git server can serve the content from the repo
RUN cd /git-server/repo-source \
    && for folder in $(ls -d */); do \
        cp -r "/git-server/repo-source/$folder." /git-server/testcases/ \
        && cd /git-server/testcases \
        && git checkout -b $(echo $folder | sed 's/\/$//')  \
        && git add . && git commit -m "Start branch" \
        && git checkout master; \
    done \
    && mkdir -p /git-server/repos/ \
    && cd /git-server/repos/ \
    && git clone --bare /git-server/testcases