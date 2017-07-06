\# Bash


get command from pid
```
ps -p [PID] -o args
ps -p $(pidof dhcpcd) -o args --no-headers

ps -eo args | grep dhcpcd | head -n -1

ps -eaf | grep 1234
```

## 1. Preview

Linux shells

- inside folder `/etc/shell`
    + popular ones: bash, tcsh, ksh
- standard UNIX shell
- Bash: GNU Bourne Again Shell
    + enhanced bourne shell
    + prompt is the dollar sign: $
- TC shell
    + prompt is greater than sign: >
- Korn shell


Process and shell

- View
    - ps: display list process current running
        + `PID   TTY      TIME CMD`
        + `27628 pts/7    0:00 sleep`
    - pstree/ptree: tree with child process
- Create
    + fork: create duplicate
    + wait: suspend parent until one of children terminate
    + exec
    + exit
- Kill Process
    + kill <PID>

## 2. Quickstart

Bash shell
```sh
#!/bin/bash`
# comment by #

Wildcard
rm *; ls ??; cat file[1-3];

Display
echo "How are you?"

Local Variable
variable_name = value
name = "John Doe"

Global Variable
export VARIABLE_NAME = value
export PATH = /bin:/usr/bin:.

Extract Value
echo $variable_name
echo $PATH

Arguments
echo $1 $2 $3     # scriptname arg1 arg2 arg3
```

Example
```sh
#!/bin/bash
# GNU bash versions 2.x
# The Party Program––Invitations to friends from the "guest" file

guestfile=~/shell/guests
if [[ ! –e "$guestfile" ]]
then
    printf "${guestfile##*/} non–existent"
    exit 1
fi

export PLACE="Sarotinis"
(( Time=$(date +%H) + 1 ))
declare -a foods=(cheese crackers shrimp drinks `"hot dogs"` sandwiches)
declare -i  n=0

for person in $(cat $guestfile)
do
    if  [[ $person == root ]]
    then
          continue
    else
          # Start of here document
          mail –v –s "Party" $person <<- FINIS
          Hi $person! Please join me at $PLACE fore a party!
          Meet me at $Time oclock.
          I will bring the ice cream. Would you please bring
          ${foods[$n] and anything else you would like to eat?
          Let me know if you can make it.
                 Hope to see you soon.
                      Your pal,
                      ellie@$(hostname)
          FINIS
          n=n+1
          if (( ${#foods[*]} ==  $n ))
          then
             declare -a foods=(cheese crackers shrimp drinks `"hot dogs"` sandwiches)
          n=0
          fi
    fi
done
```

## 3. Regular Expression

Example: change name tom or Tom to David
```
:1,$s/\<[Tt]om\>/David/g
```

```
/^love/      line begin with love
/love$/      line end with love
/l..e/       . match one character
/l*e/        * match zero or more character
/[Ll]ove/     match one in set
/[A-Z]love/   match range
/[^A-Z]/     ^ match not in set
/3\.14/      \ escape
/\<love/     line contain word begin with love
/love\>/     line contain word end with love          
```


## 4. Grep

- g/RE/p
    + g: all lines in the file
    + p: print
    + g/pattern/p: print first line containing pattern
    + g/RE/p: globally search for Regular expression and print
- Format
    + grep word filename filename
    + `grep Tom /etc/passwd`
    + grep -n \`^jack:\` /etc/passwd
- options
    + c: display count of lines rather than lines
    + h: hide filenames
    + i: ignore cases
    + l: list only file name
    + n: procede line by relative line number
    + v: invert search to unmatch lines
- can work with pipes
    + `ps -ef | grep root`
    + `ls -l | grep '^d'`

## 5. Interactive bourne shell

- Environment
    + Initialization
        *  `/etc/profile`: systemwide init by admin
        *  `.profile`: user-define in home directory
    + Prompt
        * primary $, secondary >
    + Search Path
        * PATH variable
        * `echo $PATH`
        * `PATH = $HOME:/usr:/usr/bin:/usr/local/bin`
        * `export PATH`
    + dot command
        * take script name as argument
        * execute in current shell, child process will not be start

- Shell wildcards
    + * zero or more character
    + ? exactly one
    + [abc] one in the set
    + [a-z] one in the range
    + [!a-z] not in range

local variable scope
```
echo $$         # 1313
$ round=world
$ echo $round   # world

$ sh            # Start a subshell
$ echo $$       # 1326
$ echo $round
$ exit          # Exits this shell, returns to parent shell

$ echo $$       # 1313
$ echo $round   # world
```


## 6. Useful utilities

| command | usage | function |
|---|---|---|
| apropos | apropos bash | what is this keyword
| at,batch | at 6:30am Dec 12 < program | scheduler
| | at now+3 hours < program 
| cat | cat -n file1 file2 >> file3 | display/concat file
| chmod | chmod u+x,g-x file | Turns on execute permission for user, and removes it from group on file
|| chomod 751 | rwx for user, rx for group, x others (111,101,001)
| chown | chown -R john file | change ownship with all files inside
| clear ||clear screen|
| cmp | cmp file.new file.old | announce byte and line number where diff
| cp | cp chapter1 book | copy file overwrite or to directory
| cron | |clock daemon
| diff | diff file1 file2 | line by line compare
| echo | echo "Hello World"| output
| find | find . -name \\*.c -print | find all .c file print path
||  find .. type d -print | find all directory from parent path
||  find ~ -perm 644 -print | find permission from home folder
|| find / -size 0 - exec rm "{}" \; | find and remove empty from root folder
| fmt |fmt -c -w45 letter | text formatter 
| groups | groups user | print membership
| head | head file | first 10 lines
| id | /usr/bin/id | print username id group name id
| kill || kill by number
| killall || kill by name
| line | | read one line
| ls | ls -alF | -l show attribute, -a show hidden, -F show link/script/folder
| make | make -f makefile ... | update file
| mkdir | mkdir -p dirname | directory
| mv | mv -i test1 test2 train | move files, ask before (interactive)
|| mv file1 newname | rename files
|nice| nice command | run a command at low priority
| passwd | passwd -s name | change login pw
| ping | ping -dfnqrvR | reach remote system
| ps| ps -aux | grep '^linda' | show processs running, print owned by linda
| rcp | rcp -p filename1 filename2 | remote file copy
| rlogin | rlogin -l username | remote login
| rm | rm -f -i filename | remove interactive, ignore error
|| rm -r dirname | remove folder
|sleep | sleep 60 | suspend execution |
| sort | sort -r filename | reverse sort lines
|| sort +1 -2 | sort field 1-2 (separate by whitespace)
| split | split -500 filea [prefix] | split into  xaa or prefix.aa
| tail | tail -20 filex | last 20 lines
|| tail +50 filex | display starting at line 50
| wc | wc -lwc filex | count lines words and characters
| what | what -s filename | SCCS version info
| which | which filename | locate command
| who | who | who logged on|
