# ARG REPO_ROOT=testcases
# TODO add a REPO_FROM_GIT to pull repo from a gitserver
FROM jkarlos/git-server-docker
COPY testcases /git-server/repo-source
RUN git config --global user.email "demouser@example.com" \
    && git config --global user.name "Demo User" \
    && cd /git-server/repo-source \
    && git init \
    && git add . \
    && git commit -am "Initial commit"
RUN mkdir -p /git-server/repos/
RUN cd /git-server/repos/ \
    && mkdir testcases \
    && git clone --bare /git-server/repo-source