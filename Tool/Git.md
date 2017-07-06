# Basic

## Introduction
```sh
# help
git help config 

# Global username
git config --global user.name "Your Name Here"

# set up git repo
git init
git status

# add file to staging area
git add index.html
git add css/*.*
git add --all

# commit
git commit -m "comment"

# show log
git log
```



## Staging & Commit
```sh
# show changes
git diff

# show staged changes
git diff --staged

# unstage file, HEAD for last commit
git reset HEAD index.html

# update local change
git add <file> <file>

# discard local change
git checkout -- <file> <file>

# skip staging and commit
# update all tracked files, but no new file
git commit -a -m "Message"

# undo commit, back to staging
git reset --soft HEAD^

# undo commit and all changes
git reset --hard HEAD^
git reset --hard HEAD^^ # last 2 commits

# undo and add to last commit
git add todo.txt
git commit --amend -m "New Message"

# DO NOT UNDO AFTER REMOTE PUSH
```

##  Push & Remotes
hosted service: github or bitbucket
self managed: gitosis, gitorious

[Password caching](https://help.github.com/articles/set-up-git)

-f force the push

```sh
# add remote as name origin
git remote add origin https://github.com/user/name.git

# show remote repos
git remote -v  

# remove remotes
git remote rm <name>

# push to remote
# -u to make it default for next time
# just run `git push` next time
git push -u <name> <branch> 

# pull from remote
git pull

```

##  Clone & Branching

```sh
# clone a repo
git clone <URL> [<name>]

# branch
git branch cat # branch out
git branch # show branches
git checkout cat # switch branch
echo "Schrodinger" > cat.txt
git add cat.txt
git commit -m "quantum cat"

# merge - fast forward
git checkout master
ls # no cat.txt under master branch
git merge cat
ls # cat.txt

# clean up
git branch -d cat

# merge - non fast forward
git checkout -b admin # create & checkout
git add admin/dashboard.html
git commit -m "Add admin"

# when admin and master is different
git merge admin

# auto enters vi, edit and save
jkhl, i insert, ESC leave,
:wq savequit, :q! cancel quit
```


## Collaboration
``` sh
# push master->master
git push
# rejected

# should merge remote change before pushing again
git pull


# git pull create remote one as origin/master
git merge origin/master
# pop up vi editor, and then save
git push

# merge conflicts
git status
# both modified: README.txt
# edit file
# add to staged when fixed
git add <conflict file>
# commit
git commit -m "Message"

```


## Branching
``` sh
# list all remote branch
git branch -r

# push local branch to remote
git push origin hamsters

# pull remote branch
# pull will auto merge branches
# use fetch when only want to update
git fetch

# delete remote branch
git push -delete origin weasel

# show all branch of remote and tracking status
git remote show origin

# clean up remote branch at local
git remote prune origin

# commit tagging / version
git tag
git tag -a v0.0.3 -m "version 0.0.3"
git push --tags
git checkout v0.0.1

```


## Rebase
merge commits are bad, fetch and rebase instead

```sh
# pull down all changes, not merge
git fetch

# move changes to master to a temp area
# run all origin/master commits
# run all commits in temp area
# just one aftr another, no merge commits here
git rebase

# merge kennel to master
git checkout kennel
git rebase master
#rebasing...
git checkout master
git merge kennel
# retrieve repo when online change
git fetch
# latest changes fetched to origin/master
git rebase
# edit html when conflict
git add <fixed file>
# continue rebase
git rebase --continue

```


## History & Configuration
### log

|placeholder|replacing|
|-----|-----|
|%ad|author date|
|%an|author name|
|%h|SHA hash|
|%s|subject|
|%d|ref names|

### exclude
put exclude patter in file `.git/info/exclude`
tutorial.mp4
*.mp4
experiments/

exclude from all copies
.gitignore
logs/*.log


```sh
# color the log
git config --global color.ui true

# oneline commit
git log --pretty=oneline
git log --pretty=format:"%h %ad- %s [%an]"
git log --oneline --graph
git log --oneline --status

# diff last commit
git diff HEAD
git diff HEAD^^
git diff HEAD~5

# show diff included in log
git log -p

# diff two branch
git diff master elephant

# BLAME who did it!?
git blame index.html --date short

# remove file
git rm readme.txt
git rm --cached development.log


# alias when commit
git config --global alias.beholdmyamazingcode commit
```


### git change user
Just use --local instead of --global. 
In fact, local is the default so you can just do

```
git config user.email personal@example.org
git config user.name "whatf hobbyist"
```

in one repo, and
```
git config user.email work@example.com
git config user.name "whatf at work"
```
in another repo

The values will then be stored in in the .git/config for that repo rather than your global configuration file.

[‎2/‎28/‎2017 1:54 PM] Narra, Sai (Contractor): 
DWELL_TYP_ROLLUP1 
DWELL_TYP_ROLLUP2
