
```shell
$ var="interesting/bla/blablabal/important"

$ # everything after the first slash:
$ echo "${var#*/}"
bla/blablabal/important

$ # everything after the last slash:
$ echo "${var##*/}"
important

$ # everything before the first slash:
$ echo "${var%%/*}"
interesting

$ # everything before the last slash:
$ echo "${var%/*}"
interesting/bla/blablabal
```
