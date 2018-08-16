# ARG REPO_ROOT=testcases
# TODO add a REPO_FROM_GIT to pull repo from a gitserver
FROM jkarlos/git-server-docker
COPY sshd_config /etc/ssh/sshd_config
COPY keys/id_rsa.pub /git-server/keys/id_rsa.pub
COPY testcases /git-server/repo-source
COPY shared-libs /git-server/shared-libs

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

# Add separate repo for shared libs
RUN cd /git-server/shared-libs \
   && git init && git add . && git commit -am "Initial content" \
    && cd /git-server/repos/ \
    && git clone --bare /git-server/shared-libs \
    && rm -rf /git-server/shared-libs

COPY repos-to-clone.txt /git-server/repos

# Clone remote repos into local gitserver for test use
RUN cd /git-server/repos && for repo in $(cat repos-to-clone.txt | tr -d '[ ]' | grep -v '^#' ); do git clone --bare $repo; done
