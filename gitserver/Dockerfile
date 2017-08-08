# ARG REPO_ROOT=testcases
# TODO add a REPO_FROM_GIT to pull repo from a gitserver
FROM jkarlos/git-server-docker
COPY testcases /git-server/repo-source

# Convert CONTENTS of each subfolder of testcases to a new branch of new repo /git-server/repos/testcases.git
#  This does initial git setup and creates an initial repo that we will eventually bare clone so it can be served
#  Most of the bloat is path handling and stripping out the extra subfolder
RUN git config --global user.email "demouser@example.com" \
    && git config --global user.name "Demo User" \
    && mkdir -p /git-server/testcases \
    && cd /git-server/testcases \
    && git init && echo '.gitignore' > .gitignore \
    && git add -f .gitignore && git commit -am "Initial commit" \
    && cd /git-server/repo-source \
    && for folder in $(ls -d */); do \
        cp -r "/git-server/repo-source/$folder." /git-server/testcases/ \
        && cd /git-server/testcases \
        && git checkout -b $(echo $folder | sed 's/\/$//')  \
        && git add . && git commit -m "Start branch" \
        && git checkout master; \
    done \
    && mkdir -p /git-server/repos/ \
    && cd /git-server/repos/ \
    && git clone --bare /git-server/testcases \
    && rm -rf /git-server/testcases