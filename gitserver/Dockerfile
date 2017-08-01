# ARG REPO_ROOT=testcases
# TODO add a REPO_FROM_GIT to pull repo from a gitserver
FROM jkarlos/git-server-docker
COPY testcases /git-server/repo-source
# Set up git basics
RUN git config --global user.email "demouser@example.com" \
    && git config --global user.name "Demo User" \
    && cd /git-server/repo-source \
    && git init && echo '.gitignore' > .gitignore \
    && git add -f .gitignore && git commit -am "Initial commit"
# Convert each subdirectory in testcases to a branch
RUN cd /git-server/repo-source \
    && for folder in $(ls); do \
        git checkout -b $folder && cd $folder \
        && git add . && git commit -m "Start branch" && cd ..; \
    done
RUN mkdir -p /git-server/repos/
RUN cd /git-server/repos/ \
    && mkdir testcases \
    && git clone --bare /git-server/repo-source